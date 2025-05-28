package at.fhv.sysarch.lab2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import at.fhv.sysarch.lab2.homeautomation.HomeAutomationController;
import at.fhv.sysarch.lab2.homeautomation.ui.UIHandler;
import at.fhv.sysarch.lab2.homeautomation.ui.UIWindow;

import javax.swing.*;


public class HomeAutomationSystem {

    private static ActorRef<UIHandler.UICommand> uiHandler;
    public static void main(String[] args) {
        ActorSystem<Void> home = ActorSystem.create(HomeAutomationController.create(), "HomeAutomation");

        // Start input UI after handler is ready
        new Thread(() -> {
            while (uiHandler == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Start UI
            SwingUtilities.invokeLater(() -> {
                UIWindow ui = new UIWindow(uiHandler);
                ui.appendLog("Home Automation UI ready");
            });
        }).start();
    }

    public static void setUiHandler(ActorRef<UIHandler.UICommand> handler) {
        uiHandler = handler;
    }
}
