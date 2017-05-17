package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * @æuthor Daniele Paolini
 * Text Twist project.
 * Date 16/05/17.
 * Controller for the Sign up view.
 */

public class SignupController {

  private MainApp mainApp;

  @FXML
  private TextField usernameField;

  @FXML
  private PasswordField passwordField;

  @FXML
  private PasswordField repeatedField;

  @FXML
  private Label infoLabel;

  void setMainApp(MainApp mainApp) {
    this.mainApp = mainApp;
  }

  /**
   * Handles the Sign Up button, when pressed initializes the sign up procedure.
   */
  @FXML
  private void handleSignupButton() {
    if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty() || repeatedField.getText().isEmpty()) {
      infoLabel.setTextFill(Color.RED);
      infoLabel.setText("Username and Password fields can't be empty!");
    } else if (!passwordField.getText().equals(repeatedField.getText())) {
      infoLabel.setTextFill(Color.RED);
      infoLabel.setText("Password fields does not correspond!");
    } else {
      // TODO : check if username is already picked up
      boolean status = mainApp.signUp(usernameField.getText(), passwordField.getText());
      if (status) {
        infoLabel.setTextFill(Color.GREEN);
        infoLabel.setText("Well done \"" + usernameField.getText() + "\"! Go back and login to start playing!");
      } else {
        infoLabel.setTextFill(Color.RED);
        infoLabel.setText(":( Ow! Maybe \"" + usernameField.getText() + "\" was already chosen, try with a different one.");
      }
    }

  }

  /**
   * Handles the Cancel button, when pressed shows the Index view.
   */
  @FXML
  private void handleCancelButton() {
    mainApp.showIndexView();
  }


}
