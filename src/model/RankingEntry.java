package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 26/05/17.
 * Helper class for the ranking management.
 */

@SuppressWarnings("unused")
public class RankingEntry {

  private SimpleStringProperty username;
  private SimpleIntegerProperty matches;
  private SimpleIntegerProperty points;

  public RankingEntry(String username, int matches, int points) {
    this.username = new SimpleStringProperty(username);
    this.matches = new SimpleIntegerProperty(matches);
    this.points = new SimpleIntegerProperty(points);
  }

  public String getUsername() {
    return username.get();
  }

  public void setUsername(String username) {
    this.username.set(username);
  }

  public int getMatches() {
    return matches.get();
  }

  public void setMatches(int matches) {
    this.matches.set(matches);
  }

  public int getPoints() {
    return points.get();
  }

  public void setPoints(int points) {
    this.points.set(points);
  }
}
