package at.fhv.sysarch.lab2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import at.fhv.sysarch.lab2.homeautomation.HomeAutomationController;
import at.fhv.sysarch.lab2.homeautomation.ui.UIHandler;

import java.util.Scanner;

public class HomeAutomationSystem {

    private static ActorRef<UIHandler.UICommand> uiHandler;
    public static void main(String[] args) {
        ActorSystem<Void> home = ActorSystem.create(HomeAutomationController.create(), "HomeAutomation");

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Home Automation CLI Ready:");
            while (true) {
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    if (uiHandler != null) {
                        uiHandler.tell(new UIHandler.UserInput(input));
                    }
                }
            }
        }).start();
    }

    public static void setUiHandler(ActorRef<UIHandler.UICommand> handler) {
        uiHandler = handler;
    }
}
