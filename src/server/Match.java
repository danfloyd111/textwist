package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

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
  volatile boolean timeoutFlag;
  private final List<Match> matches;
  private DatagramSocket wordsSocket = null;


  Match(String word, List<Match> matches) {
    this.word = word;
    players = new HashMap<>();
    playerCount = 0;
    monitor = new Object();
    id = UUID.randomUUID();
    sockets = new ArrayList<>();
    keepGoing = true;
    timeoutFlag = false;
    this.matches = matches;
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
          matches.remove(this);
          if (timeout.isAlive()) timeout.interrupt();
          sockets.forEach(socket -> {
            try {
              BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
              if (timeoutFlag)
                writer.write("NO:timeout");
              else
                writer.write("NO:refused");
              writer.newLine();
              writer.flush();
              socket.close();
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
      try {
        wordsSocket = new DatagramSocket();
      } catch (SocketException e) {
        System.err.println("[ERROR] Can't create the datagram socket!");
      }
      Thread wordsListener = new Thread(() -> {
        while(!Thread.currentThread().isInterrupted()) {
          try {
            byte[] bytes = new byte[200];
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
            wordsSocket.receive(packet);
            String message = new String(packet.getData());
            String tokens[] = message.split(":");
            // TODO: create a getPoints functions and update players map
            System.out.println("[DEBUG] tokens -> " + message);
          } catch (InterruptedIOException e) {
            // shutting down
          } catch (IOException e) {
            Thread.currentThread().interrupt();
          }
        }
      });
      wordsListener.start();
      sockets.forEach(socket -> {
        try {
          BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
          writer.write("OK:" + word + ":" + wordsSocket.getLocalPort());
          writer.newLine();
          writer.flush();
          socket.close();
        } catch (IOException e) {
          System.err.println("[WARNING] A client crashed while receiving letters.");
        }
      });
      try {
        Thread.sleep(30000); // TODO: sleep 2 min
      } catch (InterruptedException e) {
        System.err.println("[ERROR] Match interrupted while sleeping!");
      }
      wordsListener.interrupt();
      wordsSocket.close();
      // TODO: here broadcast the results, they are in players map
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
  }

  public UUID getId() {
    return id;
  }
}
