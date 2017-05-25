package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.*;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 24/05/17.
 * Controller for the Game view.
 */

public class GameController {

  private MainApp mainApp;
  private int wordsPort;
  private DatagramSocket sender;
  private InetAddress listener;

  @FXML
  private Label lettersLabel;

  @FXML
  private TextField wordField;

  void setMainApp(MainApp mainApp, String string, int wordsPort) {
    this.mainApp = mainApp;
    this.wordsPort = wordsPort;
    char[] letters = string.toCharArray();
    StringBuilder builder = new StringBuilder();
    for (char l : letters) {
      builder.append(l);
      builder.append(",");
    }
    String s = builder.toString();
    lettersLabel.setTextFill(Color.GREEN);
    lettersLabel.setText(s.substring(0, s.length()-1));
    try {
      sender = new DatagramSocket();
      listener = InetAddress.getByName(mainApp.SERVER_ADDRESS);
    } catch (SocketException | UnknownHostException e) {
      System.err.println("[ERROR] Can't send any word to server!");
      mainApp.showWaitingView("There are configuration problems.", false);
    }
  }

  /**
   * Handles Send button, when pressed sends the word to the server and clears the input field.
   */
  @FXML
  void handleSendButton() {
    if (wordField.getText().length() != 0) {
      byte[] bytes = (mainApp.currentUser + ":" + wordField.getText()).getBytes();
      DatagramPacket packet = new DatagramPacket(bytes, bytes.length, listener, wordsPort);
      try {
        sender.send(packet);
      } catch (IOException e) {
        System.err.println(e.getMessage());
        System.err.println("[DEBUG] Can't send the word to the server!");
      }
      wordField.clear();
    }
  }

}
