package at.fhv.sysarch.lab2.ordersystem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderRequest;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderServiceHandlerFactory;

import java.util.concurrent.CompletionStage;

public class OrderHandler extends AbstractBehavior<Void> {

    public static Behavior<Void> create() {
        return Behaviors.setup(OrderHandler::new);
    }

    private OrderHandler(ActorContext<Void> context){
        super(context);

        ActorRef<OrderRequest> orderProcessor = getContext().spawn(OrderProcessor.create(), "OrderProcessor");

        OrderServiceImpl orderService = new OrderServiceImpl(orderProcessor);


        CompletionStage<ServerBinding> binding = Http.get(getContext().getSystem())
                        .newServerAt("localhost",8080)
                                .bind(OrderServiceHandlerFactory.create(orderService, getContext().getSystem()));
        getContext().getLog().info("OrderHandler started");
        binding.thenAccept(serverBinding -> {getContext().getLog().info("Server online at http://localhost:8080/");});

    }


    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().build();
    }
}
