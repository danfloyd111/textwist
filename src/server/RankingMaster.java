package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 26/05/17.
 * This task accepts incoming requests from users.
 */

public class RankingMaster implements Runnable {

  private Connection database;
  private volatile boolean keepAlive;
  private ExecutorService workersPool;
  private ServerSocket socket;

  RankingMaster(int port, Connection database) {
    this.database = database;
    keepAlive = true;
    workersPool = Executors.newCachedThreadPool();
    try {
      socket = new ServerSocket(port);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] RankingMaster can't start!");
      System.exit(1);
    }
  }

  @Override
  public void run() {
    Socket clientSocket;
    while (keepAlive) {
      try {
        clientSocket = socket.accept();
        workersPool.submit(new RankingWorker(clientSocket,database));
      } catch (IOException e) {
        // do nothing, server going down
      }
    }
  }

  void shutdown() {
    try {
      socket.close();
      workersPool.shutdown();
      workersPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      System.out.println("[LOG] RankingMaster going down...");
    } catch (InterruptedException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] Ranking master can't close the socket.");
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] Ranking master can't await the termination of the workersPool.");
    }
    keepAlive = false;
  }

}
