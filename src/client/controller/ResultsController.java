package client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.RankingEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Daniele Paolini
 * Text Twist project.
 * Date 30/05/17.
 * Controller for the Results view.
 */

public class ResultsController {

  private MainApp mainApp;
  private String username;
  private ObservableList<RankingEntry> entries;
  private ArrayList<RankingEntry> entriesList;

  @FXML
  private TableView<RankingEntry> rankingTable;

  @FXML
  private TableColumn<RankingEntry, String> usernameCol;

  @FXML
  private TableColumn<RankingEntry, String> pointsCol;


  public void setMainApp(MainApp mainApp, String username, String results) {
    this.mainApp = mainApp;
    this.username = username;
    entriesList = new ArrayList<>();
    String tokens[] = results.split(":");
    for (int i=0; i<tokens.length; i=i+2) {
      String uname = tokens[i];
      int pts = Integer.parseInt(tokens[i+1]);
      entriesList.add(new RankingEntry(uname, 0, pts)); // number of matches played not required in partial results
    }
    Collections.sort(entriesList, Comparator.comparingInt(RankingEntry::getPoints));
    Collections.reverse(entriesList);
    entries = FXCollections.observableArrayList(entriesList);
    usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
    pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
    rankingTable.setItems(entries);
  }

  /**
   * Handles the Back button, when pressed shows the user view.
   */
  @FXML
  public void handleBackButton() {
    mainApp.showUserView(username);
  }
}
