package client.controller;

import javafx.application.Platform;
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

  void setMainApp(MainApp mainApp, String string, int wordsPort, String multicastAddress) {
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
    // Launching the timeout thread
    Thread timeout = new Thread(() -> {
      try {
        InetAddress group = InetAddress.getByName(multicastAddress);
        MulticastSocket mcSocket = new MulticastSocket(9000);
        mcSocket.joinGroup(group);
        byte[] buffer = new byte[8192]; // 8KB should be enough
        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
        Thread.sleep(60000 * 2); // match time is 2 min
        Platform.runLater(() -> mainApp.showWaitingView("Waiting for the results...", false));
        mcSocket.receive(packet);
        String results = new String(packet.getData());
        Platform.runLater(() -> mainApp.showResultsView(results.trim()));
        mcSocket.leaveGroup(group);
        mcSocket.close();
      } catch (InterruptedException e) {
        System.err.println("[ERROR] Timeout thread shouldn't be interrupted!");
      } catch (IOException e) {
        System.err.println("[ERROR] Can't get the partial results.");
      }
    });
    timeout.start();
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
      }
      wordField.clear();
    }
  }

}
