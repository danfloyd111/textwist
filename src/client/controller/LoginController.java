package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;


/**
 * @author Daniele Paolini
 * Text Twist Project
 * Date 15/05/17.
 * Controller for the Login view.
 */

public class LoginController {

  private MainApp mainApp;

  @FXML
  private TextField usernameField;

  @FXML
  private PasswordField passwordField;

  @FXML
  private Label infoLabel;

  void setMainApp(MainApp mainApp) {
    this.mainApp = mainApp;
  }

  /**
   * Handles the login button, when pressed initializes the login procedure.
   */
  @FXML
  private void handleLoginButton() {
    if (usernameField.getText().isEmpty()|| passwordField.getText().isEmpty()) {
      infoLabel.setTextFill(Color.RED);
      infoLabel.setText("Username and Password fields can't be empty!");
    } else {
      boolean status = mainApp.login(usernameField.getText(), passwordField.getText());
      if (status) {
        mainApp.showUserView(usernameField.getText());
      } else {
        infoLabel.setTextFill(Color.RED);
        infoLabel.setText(":( Ow! Username or password are incorrect!");
      }
    }
  }

  /**
   * Handles the sign in button, when pressed shows the Index view.
   */
  @FXML
  private void handleCancelButton() {
    mainApp.showIndexView();
  }


}
