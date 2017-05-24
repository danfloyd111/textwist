package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 22/05/17.
 * Controller for the waiting view.
 */

public class WaitingController {

  private MainApp mainApp;
  private String username;

  @FXML
  private Label infoLabel;

  @FXML
  private Button backButton;

  void setMainApp(MainApp mainApp, String username, boolean showButton) {
    this.mainApp = mainApp;
    this.username = username;
    backButton.setVisible(showButton);
  }

  void setInfo(String info) {
    infoLabel.setTextFill(Color.RED);
    infoLabel.setText(info);
  }

  /**
   * Handles the back button, when triggered shows the user view.
   */
  @FXML
  void handleBackButton() {
    mainApp.showUserView(username);
  }

}
