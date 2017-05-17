package model;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 17/05/17.
 * This represent an user.
 */

public class User {

  private String username, password;
  private int matches, points;
  private boolean online;

  /**
   * Standard constructor.
   * @param username is the username of the user.
   * @param password is the password of the user.
   */
  public User(String username, String password) {
    this.username = username;
    this.password = password;
    matches = 0;
    points = 0;
    online = false;
  }

  /**
   * Constructor used while loading an user from the db.
   * @param username is the username of the user.
   * @param password is the password of the user.
   * @param matches is the number of matches played by the user.
   * @param points is the number of points earned by the user until now.
   */
  public User(String username, String password, int matches, int points) {
    this.username = username;
    this.password = password;
    this.matches = matches;
    this.points = points;
    online = false;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public int getMatches() {
    return matches;
  }

  public void setMatches(int matches) {
    this.matches = matches;
  }

  public int getPoints() {
    return points;
  }

  public void setPoints(int points) {
    this.points = points;
  }

  public void setOnline() {
    this.online = true;
  }

  public void setOffline() {
    this.online = false;
  }

  public boolean isOnline() {
    return online;
  }
}
