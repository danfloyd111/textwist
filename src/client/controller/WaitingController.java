package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 22/05/17.
 * Controller for the waiting view.
 */

public class WaitingController {

  private MainApp mainApp;

  @FXML
  Label infoLabel;

  void setMainApp(MainApp mainApp) {
    this.mainApp = mainApp;
  }

  void setInfo(String info) {
    infoLabel.setText(info);
  }

}
