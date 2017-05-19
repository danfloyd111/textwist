package server;

import client.controller.InvitationNotifier;
import client.controller.InvitationNotifierInterface;
import model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 17/05/17.
 * This is the monitors that ensures the synchronization and thread safety on the shared User list.
 */

class UsersMonitor {

  private List<User> users;

  UsersMonitor() {
    users = Collections.synchronizedList(new ArrayList<User>());
  }

  /**
   * Check if the username is contained into the users list
   * @param username is the username to search for
   * @return True if username is contained in the list, False otherwise
   */
  synchronized boolean contains(String username) {
    boolean status = false;
    for (User user : users)
      if (user.getUsername().equals(username))
        status = true;
    return status;
  }

  /**
   * Adds an user to the list
   * @param user the user to add.
   */
  synchronized void addUser(User user) {
    users.add(user);
  }

  /**
   * Tries to set online an user
   * @param username is the username of the user
   * @param password is the password of the user
   * @return 0 if the credentials are valid, 1 otherwise, 2 if the user is online.
   */
  synchronized int login(String username, String password, InvitationNotifierInterface notifier) {
    int status = 1;
    for (User user : users) {
      if (user.getUsername().equals(username))
        if (user.getPassword().equals(password))
          if (user.isOnline()) {
            status = 2;
          } else {
            status = 0;
            user.setOnline(notifier);
          }
    }
    return status;
  }

  /**
   * Sets offline the user with the given username.
   * @param username is the username of the user.
   */
  synchronized void logout(String username) {
    for (User user : users)
      if (user.getUsername().equals(username))
        user.setOffline();
  }

  /**
   * Returns to the caller a list of online users represented with their username.
   * @return a list of online users represented with their username.
   */
  synchronized ArrayList<String> getOnlineUsers() {
    ArrayList<String> onlineList = new ArrayList<>();
    for (User user : users)
      if (user.isOnline())
        onlineList.add(user.getUsername());
    return onlineList;
  }

  /**
   * Returns to the caller a list that contains all the stubs of online users.
   * @return a list that contains all the stubs of online users.
   */
  synchronized ArrayList<InvitationNotifierInterface> getOnlineStubs() {
    ArrayList<InvitationNotifierInterface> stubList = new ArrayList<>();
    for (User user : users)
      if (user.isOnline())
        stubList.add(user.getNotifier());
    return stubList;
  }

  /**
   * Puts offline the user with the given stub.
   * @param stub is the InvitationNotifierInterface of the user.
   */
  synchronized void putOffline(InvitationNotifierInterface stub) {
    for (User user : users) {
      if (user.getNotifier().equals(stub))
        user.setOffline();
    }
  }

}
