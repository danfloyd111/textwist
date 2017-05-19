package server;

import java.io.*;
import java.net.Socket;

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
      // TODO: start the match in this thread
      // this is a single run thread, no need of a keepAlive procedure.
      BufferedReader reader = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(userSocket.getOutputStream()));
      String line = reader.readLine();
      String tokens[] = line.split(":");
      String owner = tokens[0];
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
        System.err.println("[ERROR] MatchWorker got problems with his socket!");
        // TODO: invalidate the match!!!!
      }
    }
  }

}
