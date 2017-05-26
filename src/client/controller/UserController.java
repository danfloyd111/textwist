package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 17/05/17.
 * Controller for the User view.
 */

public class UserController {

  private MainApp mainApp;
  private String username;

  @FXML
  private Label infoLabel;

  void setMainApp(MainApp mainApp, String username) {
    this.mainApp = mainApp;
    this.username = username;
    infoLabel.setTextFill(Color.GREEN);
    infoLabel.setText("Hello " + username + ", welcome back!");
  }
  
  /**
   * Handles the Online Users button, when pressed shows the Online Users view.
   */
  @FXML
  private void handleOnlineButton() {
    mainApp.showOnlineView(username);
  }

  /**
   * Handles the View Invitations button, when pressed shows the Invitations view.
   */
  @FXML
  private void handleInvitationsButton() {
    mainApp.showInvitationsView(username);
  }

  /**
   * Handles the Ranking button, when pressed shows the Ranking view.
   */
  @FXML
  private void handleRankingButton() {
    mainApp.showRankingView(username);
  }

  /**
   * Handles the Logout button, when pressed starts the logout procedure.
   */
  @FXML
  private void handleLogoutButton() {
    mainApp.logout(username);
  }


}
