package client.controller;

import java.rmi.RemoteException;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 16/05/17.
 * Implementation of the InvitationNotifier interface.
 */

public class InvitationNotifier implements InvitationNotifierInterface {

  @Override
  public void notifyInvitation(String ownerName, String matchId) throws RemoteException {
    System.out.println("[DEBUG] Got an invite for match " + matchId + " from " + ownerName);
  }

}

