package server;

import model.Match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @æuthor Daniele Paolini
 * Text Twist project
 * Date 21/05/17.
 * This is the monitor that ensures the synchronization and thread safety on the shared Matches list.
 */

public class MatchesMonitor {

  private List<Match> matches;

  MatchesMonitor() {
    matches = Collections.synchronizedList(new ArrayList<Match>());
  }

  /**
   * Adds the given match to the list.
   * @param match is the Match to add.
   */
  public void addMatch(Match match) {
    matches.add(match);
  }

  /**
   * Removes the match with the given id if it's contained in the list.
   * @param id is the id of the match to be removed.
   */
  public void deleteMatch(UUID id) {
    matches.removeIf(match -> match.getId().equals(id));
  }

}
