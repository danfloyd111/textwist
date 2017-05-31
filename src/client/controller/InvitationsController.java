package client.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import model.Invitation;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 19/05/17.
 * Controller for the Invitations view.
 */

public class InvitationsController {

  private MainApp mainApp;
  private String username;

  @FXML
  private Label infoLabel;

  @FXML
  private ListView<Invitation> invitationsList;

  void setMainApp(MainApp mainApp, String username) {
    this.mainApp = mainApp;
    this.username = username;
    invitationsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    ObservableList<Invitation> invitations = FXCollections.observableArrayList(mainApp.getInvitations());
    invitationsList.setItems(invitations);
  }

  /**
   * Handles the Accept button, when pressed triggers the "accept invite procedure".
   */
  @FXML
  void handleAcceptButton() {
    ObservableList<Invitation> selected = invitationsList.getSelectionModel().getSelectedItems();
    if (selected.isEmpty()) {
      infoLabel.setTextFill(Color.RED);
      infoLabel.setText(":( Ow! It seems that your selection is empty, choose an invitation!");
    } else {
      mainApp.showWaitingView("Waiting the other players...", false);
      Thread matchListener = new Thread(() -> {
        String message = "2:" + selected.get(0).getMatchId() + ":OK";
        Socket socket = null;
        try {
          socket = new Socket(mainApp.SERVER_ADDRESS, mainApp.MATCH_PORT);
          BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
          writer.write(message);
          writer.newLine();
          writer.flush();
          String response = reader.readLine();
          String[] tokens = response.split(":");
          if (tokens[0].equals("OK"))
            Platform.runLater(() -> mainApp.showGameView(tokens[1],Integer.parseInt(tokens[2]), tokens[3]));
          else
            if (tokens[1].equals("timeout"))
              Platform.runLater(() -> mainApp.showWaitingView("The waiting time is over!", true));
            else
              if (tokens[1].equals("refused"))
                Platform.runLater(() -> mainApp.showWaitingView("Someone refused the challenge.", true));
              else
                Platform.runLater(() -> mainApp.showWaitingView("This match is expired!", true));
        } catch (UnknownHostException e) {
          System.err.println("[ERROR] Unknown host - configuration error.");
          System.exit(1);
        } catch (IOException e) {
          System.err.println("[ERROR] I/O exception");
        } finally {
          if (socket != null) try {
            socket.close();
          } catch (IOException e) {
            System.err.println("[ERROR] Cant' close the socket in handleAccept RunLater Thread");
          }
        }
      });
      matchListener.start();
      // Declining all the other matches using a dedicated thread
      String sel = selected.get(0).getMatchId(); // the selection mode is SINGLE
      Thread matchDecliner = new Thread(() -> {
        Socket socket;
        try {
          socket = new Socket(mainApp.SERVER_ADDRESS, mainApp.MATCH_PORT);
          BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
          socket.setSoTimeout(1000);
          for (Invitation inv : mainApp.getInvitations()) {
            if (!inv.getMatchId().equals(sel)) {
              String message = "2:" + inv.getMatchId() + ":NO";
              writer.write(message);
              writer.newLine();
              writer.flush();
              socket.close();
            }
          }
        } catch (IOException e) {
          System.err.println(e.getMessage());
          System.err.println("[ERROR] InvitationController can't decline the other matches: I/O Error!");
        } finally {
          mainApp.getInvitations().clear();
        }
      });
      matchDecliner.start();
    }
  }

  /**
   * Handles the Decline button, when pressed triggers the "decline invite procedure".
   */
  @FXML
  void handleDeclineButton() {
    ObservableList<Invitation> selected = invitationsList.getSelectionModel().getSelectedItems();
    if (selected.isEmpty()) {
      infoLabel.setTextFill(Color.RED);
      infoLabel.setText(":( Ow! It seems that your selection is empty, choose an invitation!");
    } else {
      Thread decliner = new Thread(() -> {
        try {
          Socket socket = new Socket(mainApp.SERVER_ADDRESS, mainApp.MATCH_PORT);
          socket.setSoTimeout(1000);
          BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
          writer.write("2:" + selected.get(0).getMatchId() + ":NO");
          writer.newLine();
          writer.flush();
          socket.close();
        } catch (IOException e) {
          System.err.println("[ERROR] Cant' close the socket in handleAccept RunLater Thread");
        }
      });
      decliner.start();
      Invitation inv = selected.get(0); // the selection mode is SINGLE
      mainApp.getInvitations().remove(inv);
      mainApp.showInvitationsView(username); // refresh the view
    }
  }

  /**
   * Handles the Back button, when pressed it shows the User view.
   */
  @FXML
  void handleBackButton() {
    mainApp.showUserView(username);
  }

  /**
   * Handles the Refresh button, when pressed it refresh the current view.
   */
  @FXML
  void handleRefreshButton() {
    mainApp.showInvitationsView(username);
  }

}
