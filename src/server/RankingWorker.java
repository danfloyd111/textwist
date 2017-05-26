package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 26/05/17.
 * This task submit a query to the database in order to retrieve the actual ranking.
 */

public class RankingWorker implements Runnable {

  private Connection database;
  private Socket clientSocket;

  RankingWorker(Socket clientSocket, Connection database) {
    this.database = database;
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    try {
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
      String query = "SELECT * FROM users ORDER BY points DESC;";
      Statement statement = database.createStatement();
      ResultSet results = statement.executeQuery(query);
      while (results.next()) {
        String username = results.getString("username");
        int matches = results.getInt("matches");
        int points = results.getInt("points");
        String message = username + ":" + matches + ":" + points;
        writer.write(message);
        writer.newLine();
        writer.flush();
      }
    } catch (SQLException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] RankingWorker can't establish a connection with the db.");
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] RankingWorker can't establish a connection with client.");
    } finally {
      try {
        clientSocket.close();
      } catch (IOException e) {
        System.err.println(e.getMessage());
        System.err.println("[WARNING] RankingWorker can't close the socket.");
      }
    }
  }

}
