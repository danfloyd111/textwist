package server;

import model.Match;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 19/05/17.
 * This task receive invitations requests from client and forwards the invitations to the recipients.
 */

public class MatchWorker implements Runnable {

  private Socket userSocket;
  private UsersMonitor monitor;

  MatchWorker(Socket userSocket, UsersMonitor monitor) {
    this.userSocket = userSocket;
    this.monitor = monitor;
  }

  @Override
  public void run() {
    try {
      // this is a single run thread, no need of a keepAlive procedure.
      BufferedReader reader = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(userSocket.getOutputStream()));
      String line = reader.readLine();
      String tokens[] = line.split(":");
      String owner = tokens[0];
      String players[] = new String[tokens.length - 1];
      System.arraycopy(tokens, 1, players, 0, tokens.length - 1);
      String word = pickRandomWord();
      // match creation
      Match match = new Match(owner, players, word); // TODO: add the match to MatchMonitor and start the timeoutThread
      System.out.println("[DEBUG] Random word: " + word);
      // match notification
      boolean keepGoing = true;
      int i=1;
      while (keepGoing && i<tokens.length) {
        keepGoing = monitor.notifyUser(tokens[i], owner, "MATCH_ID");
        i++;
      }
      boolean ownerNotification = monitor.notifyUser(tokens[0], owner, "MATCH_ID");
      if (!keepGoing || !ownerNotification) // some users crashed
        writer.write("NO:Unfortunately some users crashed during the notification procedure.");
      else // all gone fine
        writer.write("OK");
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] MatchWorker has lost the connection with the match owner!");
      // TODO: invalidate the match!!!!
    } finally {
      try {
        userSocket.close();
      } catch (IOException e) {
        System.err.println(e.getMessage());
        System.err.println("[ERROR] MatchWorker got internal problems!");
        // TODO: invalidate the match!!!!
      }
    }
  }

  /**
   * This function choose a random word from the vocabulary
   * @return a random word.
   */
  private String pickRandomWord() throws IOException {
    ArrayList<String> words = new ArrayList<String>();
    try (Stream<String> stream = Files.lines(Paths.get("resources/dictionary.txt"))) {
      stream.forEach(s -> words.add(s));
    }
    Random generator = new Random(System.currentTimeMillis());
    return words.get(generator.nextInt(words.size()));
  }

}
