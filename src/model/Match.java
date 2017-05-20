package model;

import java.util.UUID;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 20/05/17.
 * This data model represent a match.
 */

public class Match {

  private String owner;
  private String[] players;
  private String word;
  private UUID id;

  public Match(String owner, String[] players, String word) {
    this.owner = owner;
    this.players = players;
    this.word = word;
    id = UUID.randomUUID();
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String[] getPlayers() {
    return players;
  }

  public void setPlayers(String[] players) {
    this.players = players;
  }

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }

  public UUID getId() {
    return id;
  }
}
