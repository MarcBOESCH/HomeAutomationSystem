package at.fhv.sysarch.lab2.ordersystem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderRequest;

public class OrderHandler extends AbstractBehavior<Void> {

    public static Behavior<Void> create() {
        return Behaviors.setup(OrderHandler::new);
    }

    private OrderHandler(ActorContext<Void> context){
        super(context);

        ActorRef<OrderRequest> orderProcessor = getContext().spawn(OrderProcessor.create(), "OrderProcessor");

        OrderServiceImpl orderService = new OrderServiceImpl(orderProcessor);

        getContext().getLog().info("OrderHandler started");
    }


    @Override
    public Receive<Void> createReceive() {
        return null;
    }
}
