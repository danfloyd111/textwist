package client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.paint.Color;
import model.Invitation;

import java.util.Iterator;
import java.util.List;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 19/05/17.
 * Controller for the Invitations view.
 */

public class InvitationsController {

  private MainApp mainApp;
  private String username;

  @FXML
  private Label infoLabel;

  @FXML
  private ListView<Invitation> invitationsList;

  void setMainApp(MainApp mainApp, String username) {
    this.mainApp = mainApp;
    this.username = username;
    invitationsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    ObservableList<Invitation> invitations = FXCollections.observableArrayList(mainApp.getInvitations());
    invitationsList.setItems(invitations);
  }

  /**
   * Handles the Accept button, when pressed triggers the "accept invite button".
   */
  @FXML
  void handleAcceptButton() {
    ObservableList<Invitation> selected = invitationsList.getSelectionModel().getSelectedItems();
    if (selected.isEmpty()) {
      infoLabel.setTextFill(Color.RED);
      infoLabel.setText(":( Ow! It seems that your selection is empty, choose an invitation!");
    } else {
      // TODO: accept the match invitation and remove it from "invitations" and go to waiting room
      List<Invitation> invitations = mainApp.getInvitations();
      for (Invitation inv : invitations) {
        String matchId = inv.getMatchId(); // TODO: decline this match invitation
      }
      invitations.clear();
    }
  }

  /**
   * Handles the Decline button, when pressed triggers the "decline invite procedure".
   */
  @FXML
  void handleDeclineButton() {
    ObservableList<Invitation> selected = invitationsList.getSelectionModel().getSelectedItems();
    if (selected.isEmpty()) {
      infoLabel.setTextFill(Color.RED);
      infoLabel.setText(":( Ow! It seems that your selection is empty, choose an invitation!");
    } else {
      // TODO: decline this match invitation
      Invitation inv = selected.get(0); // the selection mode is SINGLE
      mainApp.getInvitations().remove(inv);
      mainApp.showInvitationsView(username); // refresh the view
    }
  }

  /**
   * Handles the Back button, when pressed it shows the User view.
   */
  @FXML
  void handleBackButton() {
    mainApp.showUserView(username);
  }

}
