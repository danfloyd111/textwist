package client.controller;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @æuthor Daniele Paolini
 * Text Twist project
 * Date 16/05/17.
 * Interface for the notification service exported by the client.
 */

public interface InvitationNotifierInterface extends Remote {

  /**
   * This method will be called by the server in order to notify an invitation to the client that exports the InvitationNotifier service.
   * @param ownerName is the name of the match owner.
   * @param matchId is the id of the match.
   * @throws RemoteException (See java.rmi.RemoteException)
   */
  void notifyInvitation(String ownerName, String matchId) throws RemoteException;

  /**
   * This method will be called periodically by the server to see if client crashed
   * @return a test string.
   * @throws RemoteException (See java.rmi.RemoteException)
   */
  String heartbeat() throws RemoteException;

}
