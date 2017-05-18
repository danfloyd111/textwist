package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 18/05/17.
 * This task accept incoming match requests from the users.
 */

public class MatchMaster implements Runnable {

  private ServerSocket socket;
  private volatile boolean keepRunning;

  MatchMaster(int port) {
    keepRunning = true;
    try {
      socket = new ServerSocket(port);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] Match master can't start.");
    }
  }

  @Override
  public void run() {
    Socket userSocket = null;
    while (keepRunning) {
      try {
        userSocket = socket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(userSocket.getOutputStream()));
        String line = reader.readLine();
        System.out.println("[LOG] MatchMaster got a request, MESSAGE: " + line);
        writer.write("NO:service unavailable," + line);
        writer.newLine();
        writer.flush();
      } catch (IOException e) {
        System.err.println("[WARNING] MatchMaster caught an I/O exception.");
      }
    }

  }

  void shutdown() {
    try {
      socket.close();
      System.out.println("[LOG] MatchMaster going down...");
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] Match master can't close the socket.");
    }
    keepRunning = false;
  }

}
