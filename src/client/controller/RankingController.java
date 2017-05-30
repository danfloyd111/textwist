package client.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.RankingEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 26/05/17.
 * Controller for the Ranking view.
 */

public class RankingController {

  private MainApp mainApp;
  private String username;
  private ObservableList<RankingEntry> entries;
  private ArrayList<RankingEntry> entriesList;

  @FXML
  private Label infoLabel;

  @FXML
  private TableColumn<RankingEntry, String> usernameCol;

  @FXML
  private TableColumn<RankingEntry, Integer> matchesCol;

  @FXML
  private TableColumn<RankingEntry, Integer> pointsCol;

  @FXML
  private TableView<RankingEntry> rankingTable;

  public void setMainApp(MainApp mainApp, String username) {
    this.mainApp = mainApp;
    this.username = username;
    entriesList = new ArrayList<>();
    infoLabel.setText("Waiting results...");
    getRanking();
  }

  /**
   * Handles the Back button, when pressed shows the user view.
   */
  @FXML
  public void handleBackButton() {
    mainApp.showUserView(username);
  }

  /**
   * Handles the Refresh button, when pressed refresh this view.
   */
  @FXML
  public void handleRefreshButton() {
    mainApp.showRankingView(username);
  }

  /**
   * Creates a thread that asks to server the ranking.
   */
  private void getRanking() {
    Thread rankingRetriever = new Thread(() -> {
      Socket socket = null;
      try {
        socket = new Socket(mainApp.SERVER_ADDRESS, mainApp.RANKING_PORT);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        while (!(line = reader.readLine()).equals("END")) {
          String tokens[] = line.split(":");
          String username = tokens[0];
          int matches = Integer.parseInt(tokens[1]);
          int points = Integer.parseInt(tokens[2]);
          entriesList.add(new RankingEntry(username, matches, points));
        }
        Platform.runLater(() -> {entries = FXCollections.observableArrayList(entriesList);
          usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
          matchesCol.setCellValueFactory(new PropertyValueFactory<>("matches"));
          pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
          rankingTable.setItems(entries);
          infoLabel.setText("Global Ranking");
        });
      } catch (IOException e) {
        System.err.println(e.getMessage());
        System.err.println("[DEBUG] Error in getRanking - RankingController");
        Platform.runLater(() -> {mainApp.showWaitingView("Connection problems!",true);});
      } finally {
        if (socket != null) try {
          socket.close();
        } catch (IOException e) {
          System.err.println(e.getMessage());
          System.err.println("[DEBUG] Error in getRanking - RankingController: can't close the socket.");
        }
      }
    });
    rankingRetriever.start();
  }

}