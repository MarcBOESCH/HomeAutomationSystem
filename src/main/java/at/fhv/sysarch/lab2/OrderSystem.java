package at.fhv.sysarch.lab2;

import akka.actor.typed.ActorSystem;
import at.fhv.sysarch.lab2.homeautomation.HomeAutomationController;
import at.fhv.sysarch.lab2.ordersystem.OrderHandler;

public class OrderSystem {

    public static void main(String[] args) {
        ActorSystem<Void> ordersystem = ActorSystem.create(OrderHandler.create(), "OrderSystem");

    }
}