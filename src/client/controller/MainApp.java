package client.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import server.LoginServiceInterface;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

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

  private final int REGISTRY_PORT = 8888;
  private final int HEIGHT = 600, WIDTH = 750;
  private final String SERVER_NAME = "TEXTWISTSERVER";

  public static void main(String args[]) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    this.primaryStage = primaryStage;
    primaryStage.setTitle("Text Twist !");
    initRootView();
    showIndexView();
    initRemoteServices();
  }

  /**
   * Initializes the remote services.
   */
  private void initRemoteServices() {
    try {
      Registry registry = LocateRegistry.getRegistry(REGISTRY_PORT);
      loginService = (LoginServiceInterface) registry.lookup(SERVER_NAME);
    } catch (RemoteException e) {
      System.err.println(e.getMessage());
      System.err.println("[DEBUG] Error in initRemoteServices.");
      System.exit(1);
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
  boolean login(String username, String password) {
    boolean status = false;
    try {
      notifierService = new InvitationNotifier();
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
      loginService.logout(stub, username);
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

}