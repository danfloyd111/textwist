package client.controller;

import model.Invitation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 16/05/17.
 * Implementation of the InvitationNotifier interface.
 */

public class InvitationNotifier implements InvitationNotifierInterface {

  private List<Invitation> invitationsList;

  InvitationNotifier(List<Invitation> invitationsList) {
    this.invitationsList = invitationsList;
  }

  @Override
  public void notifyInvitation(String ownerName, String matchId) throws RemoteException {
    System.out.println("[DEBUG] Got an invite for match " + matchId + " from " + ownerName);
    invitationsList.add(new Invitation(ownerName, matchId));
  }

  @Override
  public String heartbeat() throws RemoteException {
    return "I'm alive!";
  }

}

