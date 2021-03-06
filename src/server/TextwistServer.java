package server;

import model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * @author Daniele Paolini
 * Twist Text Project
 * Date 16/05/17.
 * The server.
 */

public class TextwistServer {

  private static final int REGISTRY_PORT = 8888;
  private static final int SERVICE_PORT = 9999;
  private static final String SERVER_NAME = "TEXTWISTSERVER";
  private static final int MATCH_PORT = 8686;
  private static final int RANKING_PORT = 8787;

  private static Connection database;

  private static UsersMonitor usersMonitor;

  private static MatchMaster matchMaster;

  private static HeartbeatMonitor heartbeatMonitor;

  @SuppressWarnings("FieldCanBeLocal")
  private static ArrayList<String> dictionary;

  private static RankingMaster rankingMaster;

  public static void main(String args[]) {

    System.out.println("[LOG] Textwist server going up!");

    // Initializing the users monitor

    System.out.println("[LOG] Initializing the users monitor...");
    usersMonitor = new UsersMonitor();
    System.out.println("[LOG] Monitors initialized.");

    // Shutdown hook installation

    final Thread mainThread = Thread.currentThread();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      matchMaster.shutdown();
      heartbeatMonitor.shutdown();
      rankingMaster.shutdown();
      try {
        mainThread.join();
      } catch (InterruptedException e) {
        System.err.println("[WARNING] The shutdown hook can't join the main thread.");
      }
      System.out.println("[LOG] Shutdown hook triggered, the server is going down.");
    }));

    // Reading the dictionary

    System.out.println("[LOG] Reading the dictionary...");
    dictionary = new ArrayList<>();
    try (Stream<String> stream = Files.lines(Paths.get("resources/dictionary.txt"))) {
      stream.forEach(dictionary::add);
    } catch (IOException e) {
      System.err.println("[ERROR] Can't read the dictionary");
      System.exit(1);
    }
    System.out.println("[LOG] Dictionary loaded.");

    // Database initialization

    initDatabase();
    loadUsers();

    // Initializing RMI services

    System.out.println("[LOG] Initializing RMI services...");
    initRMI();
    System.out.println("[LOG] RMI services are up and running.");

    // Initializing MatchMaster thread

    System.out.println("[LOG] Initializing MatchMaster...");
    matchMaster = new MatchMaster(MATCH_PORT, usersMonitor, dictionary, database);
    Thread matchMasterThread = new Thread(matchMaster);
    matchMasterThread.start();
    System.out.println("[LOG] MatchMaster is up and running.");

    // Initializing HeartbeatMonitor thread

    System.out.println("[LOG] Initializing HeartbeatMonitor...");
    heartbeatMonitor = new HeartbeatMonitor(usersMonitor);
    Thread heartbeatMonitorThread = new Thread(heartbeatMonitor);
    heartbeatMonitorThread.start();
    System.out.println("[LOG] HeartbeatMonitor is up and running.");

    // Initializing RankingMaster thread

    System.out.println("[LOG] Initializing RankingMaster...");
    rankingMaster = new RankingMaster(RANKING_PORT, database);
    Thread rankingMasterThread = new Thread(rankingMaster);
    rankingMasterThread.start();
    System.out.println("[LOG] Ranking master up and running.");

  }

  /**
   * Initializes the remote services.
   */
  private static void initRMI() {
    try {
      LoginService loginService = new LoginService(usersMonitor,database);
      LoginServiceInterface stub = (LoginServiceInterface) UnicastRemoteObject.exportObject(loginService, SERVICE_PORT);
      LocateRegistry.createRegistry(REGISTRY_PORT);
      Registry registry = LocateRegistry.getRegistry(REGISTRY_PORT);
      registry.bind(SERVER_NAME, stub);
    } catch (RemoteException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] Error in initRMI");
      // TODO: kill all threads
      System.exit(1);
    } catch (AlreadyBoundException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] Error in initRMI - Server name already bound.");
      // TODO: kill all threads
      System.exit(1);
    }
  }

  @SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
  private static void loadUsers() {
    try {
      String query = "SELECT username, password, matches, points FROM users;";
      Statement statement = database.createStatement();
      ResultSet results = statement.executeQuery(query);
      while (results.next()) {
        String username = results.getString("username");
        String password = results.getString("password");
        int matches = results.getInt("matches");
        int points = results.getInt("points");
        User u = new User(username, password, matches, points);
        u.setOffline();
        usersMonitor.addUser(u);
      }
      System.out.println("[LOG] Users table loaded.");
    } catch (SQLException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] Can't load the users from the database");
      System.exit(1);
    }
  }

  @SuppressWarnings("SqlNoDataSourceInspection")
  private static void initDatabase() {
    System.out.println("[LOG] Initializing database...");

    Properties properties = new Properties();
    properties.setProperty("PRAGMA foreign_keys", "ON");
    database = null;

    try {
      database = DriverManager.getConnection("jdbc:sqlite:resources/textwist.db", properties);
      database.setAutoCommit(true);
      System.out.println("[LOG] Database is up and running.");
      Statement statement = database.createStatement();
      statement.execute("PRAGMA foreign_keys = ON;");
      // If not exists, create "users" table
      String users = "CREATE TABLE IF NOT EXISTS users (\n"
          + "username TEXT PRIMARY KEY NOT NULL,\n"
          + "password TEXT NOT NULL,\n"
          + "matches INTEGER NOT NULL,\n"
          + "points INTEGER NOT NULL);";
      statement.execute(users);
    } catch (SQLException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] Can't initialize the database.");
      System.exit(1);
    }
  }

}
