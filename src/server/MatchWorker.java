package server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 19/05/17.
 * This task do two different jobs
 * 1) receives invitations requests from client and forwards the invitations to the recipients.
 * 2) collects invitations responses and starts the match.
 */

public class MatchWorker implements Runnable {

  private Socket userSocket;
  private UsersMonitor usersMonitor;
  private final List<Match> matches;
  private final ArrayList<String> words;
  private final Connection database;

  MatchWorker(Socket userSocket, UsersMonitor usersMonitor, List<Match> matches, ArrayList<String> words, Connection database) {
    this.userSocket = userSocket;
    this.usersMonitor = usersMonitor;
    this.matches = matches;
    this.words = words;
    this.database = database;
  }

  @Override
  public void run() {
    int operation = 0;
    try {
      // this is a single run thread, no need of a keepAlive procedure.
      BufferedReader reader = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(userSocket.getOutputStream()));
      String line = reader.readLine();
      String tokens[] = line.split(":");
      operation = Integer.parseInt(tokens[0]);
      if (operation == 1) {
        System.out.println("[DEBUG] matchworker op 1");
        // op 1: invitation
        String owner = tokens[1];
        String players[] = new String[tokens.length - 2];
        System.arraycopy(tokens, 2, players, 0, tokens.length - 2);
        String word = "";
        while (word.length() < 7) word = pickRandomWord(words); // choosing a word that's long enough
        word = shuffle(word); // shuffling the letters
        // match creation
        Match match = new Match(word,matches,words, database);
        match.addPlayer(owner);
        for (String player : players) match.addPlayer(player);
        matches.add(match);
        UUID id = match.getId();
        match.initialize();
        // match notification
        boolean keepGoing = true;
        int i = 0;
        while (keepGoing && i < players.length) {
          keepGoing = usersMonitor.notifyUser(players[i], owner, id.toString());
          i++;
        }
        boolean ownerNotification = usersMonitor.notifyUser(owner, owner, id.toString());
        if (!keepGoing || !ownerNotification) {
          // some user crashed during notification procedure
          writer.write("NO:Unfortunately some users crashed during the notification procedure.");
          // TODO : kill the match
        } else // all gone fine
          writer.write("OK");
        writer.newLine();
        writer.flush();
      } else {
        System.out.println("[DEBUG] matchWorker op 2: " + line);
        // op 2: response to an invitation
        UUID id = UUID.fromString(tokens[1]);
        String answer = tokens[2];
        synchronized (matches) {
          boolean found = false;
          for (Match match : matches) {
            if (match.getId().equals(id)) {
              System.out.println("[DEBUG] found match, answer: " + answer);
              found = true;
              if (answer.equals("OK")) {
                match.confirm(userSocket);
                System.out.println("[DEBUG] answer was ok");
              } else {
                System.out.println("[DEBUG] answer was no");
                match.kill();
                operation = 1; // little workaround to close this socket in case of refuse
              }
            }
          }
          if (!found) {
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(userSocket.getOutputStream()));
            wr.write("NO:expired");
            wr.newLine();
            wr.flush();
            operation = 1; // little workaround to close this socket in case of expiration
            System.out.println("[DEBUG] match not found");
          }
        }
      }
    } catch(NumberFormatException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] The user broke the protocol!");
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] MatchWorker has lost the connection with the match owner!");
      // TODO : kill the match
    } finally {
      try {
        if (operation == 1) userSocket.close(); // if operation is 2, the socket will be closed by the Match
      } catch (IOException e) {
        System.err.println(e.getMessage());
        System.err.println("[ERROR] MatchWorker got internal problems!");
        // TODO : kill the match
      }
    }
  }

  /**
   * This function choose a random word from the vocabulary
   * @return a random word.
   */
  @SuppressWarnings("Convert2MethodRef")
  private String pickRandomWord(ArrayList<String> words) {
    Random generator = new Random(System.currentTimeMillis());
    return words.get(generator.nextInt(words.size()));
  }

  /**
   * Generates a random permutation of the given string
   * @param string is the string to be permuted
   * @return the permutated string
   */
  private String shuffle(String string) {
    char[] s = string.toCharArray();
    char[] permutation = new char[s.length];
    Random generator = new Random(System.currentTimeMillis());
    for (int i=0; i<s.length; i++) {
      int index = generator.nextInt(s.length);
      while (s[index] == '.') index = generator.nextInt(s.length);
      permutation[i] = s[index];
      s[index] = '.';
    }
    return new String(permutation);
  }

}
