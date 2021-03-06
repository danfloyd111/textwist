package client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 17/05/17.
 * Controller for the Online Users view.
 */

public class OnlineController {

  private MainApp mainApp;
  private String username;

  @FXML
  private Label infoLabel;

  @FXML
  private ListView<String> usersList;

  void setMainApp(MainApp mainApp, final String username) {
    this.mainApp = mainApp;
    this.username = username;
    usersList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    ArrayList<String> stringList = mainApp.getOnlineUsers();
    stringList.removeIf((u) -> u.equals(username));
    ObservableList<String> list = FXCollections.observableArrayList(stringList);
    if (!list.isEmpty()) usersList.setItems(list);
  }

  /**
   * Handles the Back button, when pressed shows the User view.
   */
  @FXML
  void handleBackButton() {
    mainApp.showUserView(username);
  }

  /**
   * Handles the Match button, when pressed starts the "match procedure".
   */
  @FXML
  void handleMatchButton() {
    ObservableList<String> selected = usersList.getSelectionModel().getSelectedItems();
    if (selected.isEmpty()) {
      infoLabel.setTextFill(Color.RED);
      infoLabel.setText(":( Ow! It seems that your selection is empty, choose at least one friend!");
    } else {
      mainApp.startMatch(username, selected);
    }
  }

  /**
   * Handles the Refresh button, when pressed refreshes this view.
   */
  @FXML
  void handleRefreshButton() {
    mainApp.showOnlineView(username);
  }

}
