package client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

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
    stringList.removeIf((String u) -> u.equals(username));
    ObservableList<String> list = FXCollections.observableArrayList(mainApp.getOnlineUsers());
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
    System.out.println("[DEBUG] Match button pressed");
  }

  /**
   * Handles the Refresh button, when pressed refreshes this view.
   */
  @FXML
  void handleRefreshButton() {
    mainApp.showOnlineView(username);
  }

}
