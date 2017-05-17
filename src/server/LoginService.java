package server;

import client.controller.InvitationNotifierInterface;
import model.User;

import java.rmi.RemoteException;
import java.sql.*;

/**
 * @author Daniele Paolini
 * Text Twist project.
 * Date 16/05/17.
 * Implementation of the login service interface.
 */

@SuppressWarnings("ALL")
public class LoginService implements LoginServiceInterface {

  private UsersMonitor usersMonitor;
  private Connection database;

  LoginService(UsersMonitor usersMonitor, Connection database) {
    this.usersMonitor = usersMonitor;
    this.database = database;
  }

  @Override
  public boolean login(InvitationNotifierInterface client, String username, String password) throws RemoteException {
    boolean status = usersMonitor.login(username, password);
    // TODO: add client to the online list
    if (status) {
      System.out.println("[LOG] User: " + username + " has logged in.");
      return true;
    }
    System.out.println("[LOG] User: " + username + " tried to login with invalid credentials.");
    return false;
  }

  @Override
  public void logout(InvitationNotifierInterface client, String username) throws RemoteException {
    // TODO: remove client from the online list
    System.out.println("[LOG] User: " + username + " has logged out.");
  }

  @Override
  public boolean signup(String username, String password) throws RemoteException {
    if (usersMonitor.contains(username)) return false; // checking if the given username was already chosen
    else {
      usersMonitor.addUser(new User(username, password)); // adding the user to the shared list
      try {
        // adding the user to the database
        Statement statement = database.createStatement();
        String query = "INSERT INTO users VALUES ('" + username +"', '" + password + "', '0', '0');";
        statement.execute(query);
      } catch (SQLException e) {
        System.err.println(e.getMessage());
        System.err.println("[WARNING] Can't add the user to the database.");
      }
      System.out.println("[LOG] User: " + username + " has signed up.");
      return true;
    }
  }

}
