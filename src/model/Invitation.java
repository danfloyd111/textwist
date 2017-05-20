package model;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 20/05/17.
 * This data model represent an invitation as seen by a generic user.
 */

public class Invitation {

  private String owner;
  private String matchId;

  public Invitation(String owner, String matchId) {
    this.owner = owner;
    this.matchId = matchId;
  }

  public String getMatchId() {
    return matchId;
  }

  public void setMatchId(String matchId) {
    this.matchId = matchId;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  @Override
  public String toString() {
    return "Got an invitation from " + owner + " !";
  }
}
