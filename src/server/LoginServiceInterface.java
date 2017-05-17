package server;

import client.controller.InvitationNotifierInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author Daniele Paolini
 * Text Twist project.
 * Date 16/05/17.
 * Interface for the login service exported by the server.
 */

public interface LoginServiceInterface extends Remote {

  /**
   * Called from the client when he wants to login and register his invitation callback.
   * @param client is the object that exports the callback method.
   * @param username is the username of the client.
   * @param password is the password of the client.
   * @return True if the operation succeeds, False if not (username or password are not correct).
   * @throws RemoteException (see java.rmi.RemoteException)
   */
  boolean login(InvitationNotifierInterface client, String username, String password) throws RemoteException;

  /**
   * Called from the client when he wants to logout and deregister his invitation callback.
   * @param client is the object that exports the callback method.
   * @param username is the username of the client.
   * @throws RemoteException (see java.rmi.RemoteException)
   */
  void logout(InvitationNotifierInterface client, String username) throws RemoteException;

  /**
   * Called from the client when he wants to sign up.
   * @param username is the username chosen by the client.
   * @param password is the password chosen by the client.
   * @return True if the operation succeeds, False if not (username already picked by another user).
   * @throws RemoteException (see java.rmi.RemoteException)
   */
  boolean signup(String username, String password) throws RemoteException;

  /**
   * Called from the client when he wants to get the list of online users.
   * @return The list of online users represented with their username.
   * @throws RemoteException (see java.rmi.RemoteException)
   */
  ArrayList<String> getOnlineUsers() throws RemoteException;

}
