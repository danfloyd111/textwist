package client.controller;

import javafx.fxml.FXML;

/**
 * @author Daniele Paolini
 * Text Twist Project
 * Date 15/05/17.
 * Controller for the Index view.
 */

public class IndexController {

  private MainApp mainApp;

  void setMainApp(MainApp mainApp) {
    this.mainApp = mainApp;
  }

  /**
   * Handles the login button, when pressed shows the Login view.
   */
  @FXML
  private void handleLoginButton() {
    mainApp.showLoginView();
  }

  /**
   * Handles the sign in button, when pressed shows the Sign in view.
   */
  @FXML
  private void handleSigninButton() {
    mainApp.showSignupView();
  }

}
