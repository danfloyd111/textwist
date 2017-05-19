package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

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

  void setMainApp(MainApp mainApp, String username) {
    this.mainApp = mainApp;
    this.username = username;
  }

  /**
   * Handles the Accept button, when pressed triggers the "accept invite button".
   */
  @FXML
  void handleAcceptButton() {
    System.out.println("[DEBUG] Accept button pressed.");
  }

  /**
   * Handles the Decline button, when pressed triggers the "decline invite procedure".
   */
  @FXML
  void handleDeclineButton() {
    System.out.println("[DEBUG] Decline button pressed.");
  }

  /**
   * Handles the Back button, when pressed it shows the User view.
   */
  @FXML
  void handleBackButton() {
    mainApp.showUserView(username);
  }

}
