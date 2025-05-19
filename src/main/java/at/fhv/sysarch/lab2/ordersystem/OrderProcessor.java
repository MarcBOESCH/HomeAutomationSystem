/// Handles the incoming order requests over gRPC
package at.fhv.sysarch.lab2.ordersystem;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

//Vorerst <String>, später nochmal schauen ob eine andere Typisierung mehr Sinn macht.

public class OrderProcessor extends AbstractBehavior<String> {
    public interface Order {}

    public static Behavior<String> create(){
        return  Behaviors.setup(OrderProcessor::new);
    }

    private OrderProcessor(ActorContext<String> context){
        super(context);
        getContext().getLog().info("OrderProcessor started");
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .build();
    }
}
