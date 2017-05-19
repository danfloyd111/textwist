package server;

import client.controller.InvitationNotifier;
import client.controller.InvitationNotifierInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author Daniele Paolini
 * Text Twist project
 * Date 19/05/17.
 * Clients heartbeat monitor
 */
public class HeartbeatMonitor implements Runnable {

  private volatile boolean keepAlive;
  private UsersMonitor usersMonitor;
  private final int PERIOD = 10000; // 10 seconds period

  HeartbeatMonitor(UsersMonitor usersMonitor) {
    keepAlive = true;
    this.usersMonitor = usersMonitor;
  }

  @Override
  public void run() {
    while (keepAlive) {
      InvitationNotifierInterface currentStub = null;
      try {
        ArrayList<InvitationNotifierInterface> stubs = usersMonitor.getOnlineStubs();
        for (InvitationNotifierInterface stub : stubs) {
          currentStub = stub;
          stub.heartbeat();
        }
        Thread.sleep(PERIOD);
      } catch (InterruptedException e) {
        keepAlive = false;
      } catch (RemoteException e) {
        // the user crashed
        usersMonitor.putOffline(currentStub);
        System.err.println("[WARNING] User crashed!");
      }
    }
  }

  void shutdown() {
    keepAlive = false;
    System.out.println("[LOG] Heartbeat Monitor going down.");
  }

}
