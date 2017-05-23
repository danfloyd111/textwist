package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 21/05/17.
 * This runnable task represent a match.
 */

public class Match implements Runnable {

  private Map<String, Integer> players;
  private UUID id;
  private String word;
  private volatile int playerCount;
  private volatile boolean keepGoing;
  private final Object monitor;
  private ArrayList<Socket> sockets;
  private Thread currentThread;

  Match(String word) {
    this.word = word;
    players = new HashMap<>();
    playerCount = 0;
    monitor = new Object();
    id = UUID.randomUUID();
    sockets = new ArrayList<>();
    keepGoing = true;
  }

  void initialize() {
    currentThread = new Thread(this);
    currentThread.start();
  }

  @Override
  public void run() {
    MatchTimeout matchTimeout = new MatchTimeout(this);
    Thread timeout = new Thread(matchTimeout);
    timeout.start();
    synchronized (monitor) {
      while (playerCount < players.size() && keepGoing)
        try {
          monitor.wait();
        } catch (InterruptedException e) {
          System.out.println("[DEBUG] Match invalidated.");
          if (timeout.isAlive()) timeout.interrupt();
          sockets.forEach(socket -> {
            try {
              socket.close(); // TODO: send NO
            } catch (IOException e1) {
              System.err.println("[WARNING] Can't close a client's socket.");
            }
          });
          keepGoing = false;
          Thread.currentThread().interrupt();
          return;
        }
    }
    if (keepGoing) {
      System.out.println("[DEBUG] OK! Match started.");
      timeout.interrupt();
      sockets.forEach(socket -> {
        try {
          BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
          writer.write("OK:Queste sono le lettere");
          writer.newLine();
          writer.flush();
          socket.close();
        } catch (IOException e) {
          System.err.println("[WARNING] A client crashed while receiving letters.");
        }
      });
    } else {
      if (timeout.isAlive()) timeout.interrupt();
    }
  }

  synchronized void addPlayer(String player) {
    players.put(player, 0);
  }

  void confirm(Socket s) {
    synchronized (monitor) {
      sockets.add(s);
      playerCount++;
      monitor.notify();
    }
  }

  synchronized void kill() {
    currentThread.interrupt();
    System.out.println("[DEBUG] in interrupt of Match");
  }

  public UUID getId() {
    return id;
  }
}
