package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 18/05/17.
 * This task accept incoming match requests from the users.
 */

public class MatchMaster implements Runnable {

  private ServerSocket socket;
  private volatile boolean keepRunning;
  private ExecutorService workersPool;
  private UsersMonitor usersMonitor;

  MatchMaster(int port, UsersMonitor usersMonitor) {
    keepRunning = true;
    workersPool = Executors.newCachedThreadPool();
    this.usersMonitor = usersMonitor;
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
        workersPool.submit(new MatchWorker(userSocket, usersMonitor));
      } catch (IOException e) {
        System.err.println("[WARNING] MatchMaster caught an I/O exception.");
      }
    }

  }

  void shutdown() {
    try {
      socket.close();
      workersPool.shutdown();
      workersPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      System.out.println("[LOG] MatchMaster going down...");
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] Match master can't close the socket.");
    } catch (InterruptedException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] Match master can't await the termination of the workersPool.");
    }
    keepRunning = false;
  }

}
