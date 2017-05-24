package server;

import client.controller.InvitationNotifierInterface;
import model.User;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;

/**
 * @author Daniele Paolini
 * Text Twist project.
 * Date 16/05/17.
 * Implementation of the login service interface.
 */

@SuppressWarnings("ALL")
public class LoginService implements LoginServiceInterface {

  private UsersMonitor usersMonitor;
  private Connection database;

  LoginService(UsersMonitor usersMonitor, Connection database) {
    this.usersMonitor = usersMonitor;
    this.database = database;
  }

  @Override
  public int login(InvitationNotifierInterface notifier, String username, String password) throws RemoteException {
    int status = usersMonitor.login(username, password, notifier);
    switch (status) {
      case 0: System.out.println("[LOG] User: " + username + " has logged in."); break;
      case 1: System.out.println("[LOG] User: " + username + " tried to login with invalid credentials."); break;
      case 2: System.out.println("[LOG] User: " + username + " tried to login multiple times."); break;
      default: System.out.println("[WARNING] User monitor is broken.");
    }
    return status;
  }

  @Override
  public void logout(String username) throws RemoteException {
    if (usersMonitor.isOnline(username)) {
      usersMonitor.logout(username);
      System.out.println("[LOG] User: " + username + " has logged out.");
    }
  }

  @Override
  public boolean signup(String username, String password) throws RemoteException {
    if (usersMonitor.contains(username)) return false; // checking if the given username was already chosen
    else {
      usersMonitor.addUser(new User(username, password)); // adding the user to the shared list
      try {
        // adding the user to the database
        Statement statement = database.createStatement();
        String query = "INSERT INTO users VALUES ('" + username +"', '" + password + "', '0', '0');";
        statement.execute(query);
      } catch (SQLException e) {
        System.err.println(e.getMessage());
        System.err.println("[WARNING] Can't add the user to the database.");
      }
      System.out.println("[LOG] User: " + username + " has signed up.");
      return true;
    }
  }

  @Override
  public ArrayList<String> getOnlineUsers() {
    return usersMonitor.getOnlineUsers();
  }

  @Override
  public String heartbeat() throws RemoteException {
    return "I'm alive";
  }

}
