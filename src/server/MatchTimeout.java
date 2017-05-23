package server;

/**
 * @author Daniele Paolini
 * Text Twist project.
 * Date 23/05/17.
 * This task kills the Match when it reaches the timeout.
 */

public class MatchTimeout implements Runnable {

  private Match match;

  MatchTimeout(Match match) {
    this.match = match;
  }


  @Override
  public void run() {
    try {
      Thread.sleep(1000 * 60 * 7); // waiting time: 7 minutes
      match.timeoutFlag = true;
      match.kill();
    } catch (InterruptedException e) {
      // do nothing, all the users joined the match
    }
  }

}
