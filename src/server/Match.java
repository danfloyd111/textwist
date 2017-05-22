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

  Match(String word) {
    this.word = word;
    players = new HashMap<>();
    playerCount = 0;
    monitor = new Object();
    id = UUID.randomUUID();
    sockets = new ArrayList<>();
    keepGoing = true;
  }

  @Override
  public void run() {
    System.out.println("[DEBUG] in run");
    while (keepGoing) {
      System.out.println("[DEBUG] in while");
      try {
        System.out.println("[DEBUG] in try");
        synchronized (monitor) {
          System.out.println("[DEBUG] in sync");
          while (playerCount < players.size()) {
            System.out.println("[DEBUG] prepare to wait");
            monitor.wait();
          }
          System.out.println("[DEBUG] OK! Match started.");
          // PROVVISORIO
          sockets.forEach(socket -> {
            try {
              BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
              writer.write("[DEBUG] OK");
              writer.newLine();
              writer.flush();
              socket.close();
            } catch (IOException e) {
              e.printStackTrace(); // exc handling
            }
          });
          keepGoing = false;
        }
      } catch (InterruptedException e) {
        System.out.println("[DEBUG] OK ! Match invalidated."); // TODO : se qualcuno rifiuta il match interrupt me!!!
        sockets.forEach(socket -> {
          try {
            socket.close(); // questo dovrebbe lanciare un ecc anche sul client
          } catch (IOException e1) {
            e1.printStackTrace(); // exc handling
          }
        });
        // PROVVISORIO
        keepGoing = false;
      }
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

  synchronized void interrupt() {
    Thread.currentThread().interrupt();
  }

  public UUID getId() {
    return id;
  }
}
