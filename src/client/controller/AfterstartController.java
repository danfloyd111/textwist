package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * @author Daniele Paolini
 * Text Twist Project
 * Date 18/05/17.
 * Controller for the "after start" view.
 */

public class AfterstartController {

  private MainApp mainApp;
  private String username;

  @FXML
  private Label statusLabel;

  @FXML
  private Label infoLabel;

  void setMainApp (MainApp mainApp, String username, boolean status, String info) {
    this.mainApp = mainApp;
    this.username = username;
    if (status) {
      statusLabel.setTextFill(Color.GREEN);
      infoLabel.setText("Prepare for the match!");
      infoLabel.setText(info);
    } else {
      statusLabel.setTextFill(Color.RED);
      statusLabel.setText(":( Ow! Error!");
      infoLabel.setText(info);
    }
  }

  /**
   * Handles the Back button, when pressed shows the User view.
   */
  @FXML
  void handleBackButton() {
    mainApp.showUserView(username);
  }

}
