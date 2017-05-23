package client.controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Invitation;
import server.LoginServiceInterface;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniele Paolini
 * Text Twist Project
 * Date 15/05/17.
 * Client application.
 */

@SuppressWarnings("FieldCanBeLocal")

public class MainApp extends Application {

  private Stage primaryStage;
  private BorderPane rootView;

  private LoginServiceInterface loginService;
  private InvitationNotifierInterface notifierService;
  private InvitationNotifierInterface stub;
  private String currentUser;
  private List<Invitation> invitationsList;

  private final int REGISTRY_PORT = 8888;
  private final int HEIGHT = 600, WIDTH = 750;
  private final String SERVER_NAME = "TEXTWISTSERVER";
  public final int MATCH_PORT = 8686;
  public final String SERVER_ADDRESS = "localhost";
  private Thread heartMonitor;

  public static void main(String args[]) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    invitationsList = Collections.synchronizedList(new ArrayList<Invitation>());
    this.primaryStage = primaryStage;
    primaryStage.setTitle("Text Twist !");
    primaryStage.getIcons().add(new Image("file:resources/icon.png"));
    initRootView();
    showIndexView();
    initRemoteServices();
    // launching server's heartbeat monitoring thread
    heartMonitor = new Thread(() -> {
      while(!Thread.currentThread().isInterrupted()) {
        try {
          loginService.heartbeat();
          Thread.sleep(1000); // TODO: 1 sec maybe is too short? ask to IPA
        } catch (RemoteException e) {
          // the server is crashed
          Platform.runLater(() -> showWaitingView("The server crashed!",false));
          Thread.currentThread().interrupt();
        } catch (InterruptedException e) {
          // shutting down
        }
      }
    });
    heartMonitor.start();
  }

  @Override
  public void stop() {
    // TODO: do here all of your cleanings
    if (currentUser != null) logout(currentUser);
    if (heartMonitor != null) heartMonitor.interrupt();
    System.exit(0);
  }

  /**
   * Initializes the remote services.
   */
  private void initRemoteServices() {
    try {
      Registry registry = LocateRegistry.getRegistry(REGISTRY_PORT);
      loginService = (LoginServiceInterface) registry.lookup(SERVER_NAME);
    } catch (RemoteException e) {
      showWaitingView("The server is down, please retry later.", false);
      //System.exit(1);
    } catch (NotBoundException e) {
      System.err.println(e.getMessage());
      System.err.println("[DEBUG] Error in initRemoteServices - Can't find the remote service.");
      System.exit(1);
    }
  }

  /**
   * Initializes the scene and shows the root view.
   */
  private void initRootView() {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(MainApp.class.getResource("/client/view/root.fxml"));
      this.rootView = loader.load();
      Scene scene = new Scene(rootView);
      scene.getStylesheets().add("/res/theme.css");
      primaryStage.setScene(scene);
      primaryStage.setMaxHeight(HEIGHT);  // height and width are fixed (in this version) in order to reduce the developing process
      primaryStage.setMinHeight(HEIGHT);
      primaryStage.setMaxWidth(WIDTH);
      primaryStage.setMinWidth(WIDTH);
      primaryStage.show();
      RootController controller = loader.getController();
      controller.setMainApp(this);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[DEBUG] Error in initRootView.");
      System.exit(1);
    }
  }

  /**
   * Starts the sign up procedure on the remote service.
   * @param username is the username of the user.
   * @param password is the password of the user.
   * @return True if the procedure succeeds, False if not (username already picked by another user).
   */
  boolean signUp(String username, String password) {
    boolean status = false;
    try {
      status = loginService.signup(username, password);
    } catch (RemoteException e) {
      System.err.println(e.getMessage());
      System.err.println("[WARNING] Remote exception in signUp.");
    }
    return status;
  }

  /**
   * Starts the login procedure on the remote service.
   * @param username is the username of the user.
   * @param password is the password of the user.
   * @return True if the procedure succeeds, False if not (username or password are wrong).
   */
  int login(String username, String password) {
    int status = 3;
    currentUser = username;
    try {
      notifierService = new InvitationNotifier(invitationsList);
      stub = (InvitationNotifierInterface) UnicastRemoteObject.exportObject(notifierService, 0);
      status = loginService.login(stub, username, password);
    } catch (RemoteException e) {
      System.err.println(e.getMessage());
      System.err.println("[WARNING] Remote exception in login.");
    }
    return status;
  }

  /**
   * Starts the logout procedure on the remote service.
   * @param username is the username of the user.
   */
  void logout(String username) {
    try {
      loginService.logout(username);
    } catch (RemoteException e) {
      System.err.println(e.getMessage());
      System.err.println("[WARNING] Remote exception in login.");
    } finally {
      showIndexView();
    }
  }

  /**
   * Get the list of online users from the remote service.
   * @return A list of the users that are online represented with their username.
   */
  ArrayList<String> getOnlineUsers() {
    ArrayList<String> list = null;
    try {
      list = loginService.getOnlineUsers();
    } catch (RemoteException e) {
      System.err.println(e.getMessage());
      System.err.println("[WARNING] Remote exception in getOnlineUsers.");
    }
    return list;
  }

  /**
   * Starts the procedure for a new match
   * @param username is the username of the owner of the match.
   * @param users is the list of users invited by the owner to join the match.
   */
  void startMatch(String username, ObservableList<String> users) {
    String op = ("1:" + username); // operation 1 means : start the match
    StringBuilder message = new StringBuilder(op);
    for (String user : users) message.append(":").append(user);
    System.out.println(String.valueOf(message));
    Socket socket = null;
    try {
      socket = new Socket(SERVER_ADDRESS, MATCH_PORT);
      socket.setSoTimeout(1500);
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      System.out.println("[DEBUG] Sending list " + String.valueOf(message));
      writer.write(String.valueOf(message));
      writer.newLine();
      writer.flush();
      System.out.println("[DEBUG] List sent.");
      String response = reader.readLine();
      System.out.println("[DEBUG] Response received.");
      String[] tokens = response.split(":");
      String status = tokens[0];
      if (status.equals("OK")) {
        showAfterstartView(username,true,"Are you ready? Go back, and check your invitation list!");
      } else {
        String errorMessage = tokens[1];
        System.out.println("[ERROR] " + errorMessage);
        showAfterstartView(username,false, errorMessage);
      }
    } catch (SocketException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] Socket timeout - startMatch");
      showAfterstartView(username,false,"The server is down!");
    } catch (UnknownHostException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] Unknown host - startMatch");
      showAfterstartView(username,false,"Check your configuration!");
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[ERROR] I/O error - startMatch");
      showAfterstartView(username,false,"Internal I/O error!");
    } finally {
      if (socket != null) try {
        socket.close();
      } catch (IOException e) {
        System.err.println(e.getMessage());
        System.err.println("[ERROR] Can't close the socket - startMatch");
      }
    }
  }

  /**
   * Returns the list of invitations.
   * @return the list of invitations.
   */
  List<Invitation> getInvitations() {
    return invitationsList;
  }

  /**
   * Shows the Index view.
   */
  void showIndexView() {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(MainApp.class.getResource("/client/view/index.fxml"));
      AnchorPane indexView = loader.load();
      rootView.setCenter(indexView);
      String wallPath = MainApp.class.getResource("/res/crop1.jpg").toExternalForm();
      indexView.setStyle("-fx-background-image: url('" + wallPath + "'); -fx-background-position: center center; -fx-background-repeat: stretch; ");
      IndexController controller = loader.getController();
      controller.setMainApp(this);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[DEBUG] Error in showIndexView.");
      System.exit(1);
    }
  }

  /**
   * Shows the Login view.
   */
  void showLoginView() {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(MainApp.class.getResource("/client/view/login.fxml"));
      AnchorPane loginView = loader.load();
      String wallPath = MainApp.class.getResource("/res/crop1.jpg").toExternalForm();
      loginView.setStyle("-fx-background-image: url('" + wallPath + "'); -fx-background-position: center center; -fx-background-repeat: stretch");
      rootView.setCenter(loginView);
      LoginController controller = loader.getController();
      controller.setMainApp(this);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[DEBUG] Error in showLoginView.");
      System.exit(1);
    }

  }

  /**
   * Shows the Sign Up view.
   */
  void showSignupView() {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(MainApp.class.getResource("/client/view/signup.fxml"));
      AnchorPane signupView = loader.load();
      String wallPath = MainApp.class.getResource("/res/crop1.jpg").toExternalForm();
      signupView.setStyle("-fx-background-image: url('" + wallPath + "'); -fx-background-position: center center; -fx-background-repeat: stretch");
      rootView.setCenter(signupView);
      SignupController controller = loader.getController();
      controller.setMainApp(this);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[DEBUG] Error in showSignupView.");
      System.exit(1);
    }
  }

  /**
   * Shows the User view.
   * @param username is the username of the user to show.
   */
  void showUserView(String username) {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(MainApp.class.getResource("/client/view/user.fxml"));
      AnchorPane userView = loader.load();
      String wallPath = MainApp.class.getResource("/res/crop1.jpg").toExternalForm();
      userView.setStyle("-fx-background-image: url('" + wallPath + "'); -fx-background-position: center center; -fx-background-repeat: stretch");
      rootView.setCenter(userView);
      UserController controller = loader.getController();
      controller.setMainApp(this, username);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[DEBUG] Error in showUserView.");
      System.exit(1);
    }
  }

  /**
   * Shows the Online view.
   * @param username is the username of the user to show.
   */
  void showOnlineView(String username) {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(MainApp.class.getResource("/client/view/online.fxml"));
      AnchorPane onlineView = loader.load();
      String wallPath = MainApp.class.getResource("/res/crop1.jpg").toExternalForm();
      onlineView.setStyle("-fx-background-image: url('" + wallPath + "'); -fx-background-position: center center; -fx-background-repeat: stretch");
      rootView.setCenter(onlineView);
      OnlineController controller = loader.getController();
      controller.setMainApp(this, username);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[DEBUG] Error in showOnlineView.");
      System.exit(1);
    }
  }

  /**
   * Shows the "After start" view.
   * @param username is the username of the user that tries to start the match.
   * @param status is the response status got from the server.
   * @param info is the response string that explicate the status.
   */
  private void showAfterstartView(String username, boolean status, String info) {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(MainApp.class.getResource("/client/view/afterstart.fxml"));
      AnchorPane afterstartView = loader.load();
      String wallPath = MainApp.class.getResource("/res/crop1.jpg").toExternalForm();
      rootView.setCenter(afterstartView);
      afterstartView.setStyle("-fx-background-image: url('" + wallPath + "'); -fx-background-position: center center; -fx-background-repeat: stretch");
      AfterstartController controller = loader.getController();
      controller.setMainApp(this, username, status, info);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[DEBUG] Error in showAfterstartView.");
      System.exit(1);
    }
  }

  /**
   * Shows the Invitations view.
   * @param username is the username of the user that wants to view his invitations.
   */
  void showInvitationsView(String username) {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(MainApp.class.getResource("/client/view/invitations.fxml"));
      AnchorPane invitationsView = loader.load();
      String wallPath = MainApp.class.getResource("/res/crop1.jpg").toExternalForm();
      invitationsView.setStyle("-fx-background-image: url('" + wallPath + "'); -fx-background-position: center center; -fx-background-repeat: stretch");
      rootView.setCenter(invitationsView);
      InvitationsController controller = loader.getController();
      controller.setMainApp(this, username);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[DEBUG] Error in showInvitationsView.");
      System.exit(1);
    }
  }

  /**
   * Shows the Waiting view.
   * @param info is the string to be shown in the view.
   */
  void showWaitingView(String info, boolean showButton) {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(MainApp.class.getResource("/client/view/waiting.fxml"));
      AnchorPane waitingView = loader.load();
      String wallPath = MainApp.class.getResource("/res/crop1.jpg").toExternalForm();
      waitingView.setStyle("-fx-background-image: url('" + wallPath + "'); -fx-background-position: center center; -fx-background-repeat: stretch");
      rootView.setCenter(waitingView);
      WaitingController controller = loader.getController();
      controller.setMainApp(this, currentUser, showButton);
      controller.setInfo(info);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("[DEBUG] Error in showWaitingView.");
      System.exit(1);
    }
  }

}