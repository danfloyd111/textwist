package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
  private ArrayList<String> dictionary;
  private final Connection database;


  Match(String word, List<Match> matches, ArrayList<String> dictionary, Connection database) {
    this.word = word;
    players = new HashMap<>();
    playerCount = 0;
    monitor = new Object();
    id = UUID.randomUUID();
    sockets = new ArrayList<>();
    keepGoing = true;
    timeoutFlag = false;
    this.matches = matches;
    this.dictionary = dictionary;
    this.database = database;
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
            int points = getPoints(createWordMap(word), tokens[1].trim());
            players.put(tokens[0], players.get(tokens[0]) + points);
            System.out.println("[DEBUG] received -> " + tokens[1].trim() + " from " + tokens[0] + " / points: " + points);
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
        Thread.sleep(60000); // TODO: sleep 2 an half min
      } catch (InterruptedException e) {
        System.err.println("[ERROR] Match interrupted while sleeping!");
      }
      wordsListener.interrupt();
      wordsSocket.close();
      // Updating the database
      for (Map.Entry<String,Integer> entry : players.entrySet()) {
        try {
          String query = "SELECT points, matches FROM users WHERE username = '" + entry.getKey() + "';";
          Statement statement = database.createStatement();
          ResultSet results = statement.executeQuery(query);
          int oldPoints = 0;
          int oldMatches = 0;
          // only one result
          while (results.next()) {
            oldPoints = results.getInt("points");
            oldMatches = results.getInt("matches");
          }
          int points = oldPoints + entry.getValue();
          oldMatches++;
          query = "UPDATE users SET points = "+ points + " , matches = " +  oldMatches + " WHERE username = '" + entry.getKey() + "';";
          statement.execute(query);
        } catch (SQLException e) {
          System.err.println(e.getMessage());
          System.err.println("[ERROR] Can't update the database.");
        }
      }
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
      monitor.notifyAll();
    }
  }

  synchronized void kill() {
    currentThread.interrupt();
  }

  public UUID getId() {
    return id;
  }

  private Map<Character,Integer> createWordMap(String word) {
    Map<Character,Integer> map = new HashMap<>();
    for (int i=0; i<word.length(); i++) {
      if (!map.containsKey(word.charAt(i)))
        map.put(word.charAt(i), 1);
      else
        map.put(word.charAt(i), map.get(word.charAt(i)) + 1);
    }
    return map;
  }

  private int getPoints(Map<Character,Integer> wordMap, String userWord) {
    boolean valid = true;
    int i = 0;
    // checking chars validity && length of the word
    while (i < userWord.length() && valid) {
      if (!wordMap.containsKey(userWord.charAt(i))) {
        valid = false;
      } else {
        wordMap.put(userWord.charAt(i), wordMap.get(userWord.charAt(i)) - 1);
        if (wordMap.get(userWord.charAt(i)) < 0)
          valid = false;
      }
      i++;
    }
    // checking word validity
    if (valid && Collections.binarySearch(dictionary, userWord) >= 0) return userWord.length();
    else
      return 0;
  }

}
