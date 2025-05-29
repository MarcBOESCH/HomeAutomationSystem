package at.fhv.sysarch.lab2.ordersystem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.grpc.javadsl.ServerReflection;
import akka.grpc.javadsl.ServiceHandler;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import at.fhv.sysarch.lab2.homeautomation.grpc.*;

import java.util.concurrent.CompletionStage;

public class OrderHandler extends AbstractBehavior<Void> {

    public static Behavior<Void> create() {
        return Behaviors.setup(OrderHandler::new);
    }

    private OrderHandler(ActorContext<Void> context){
        super(context);

        ActorRef<OrderProcessor.ProcessOrderCommand> orderProcessor = getContext().spawn(OrderProcessor.create(), "OrderProcessor");
        ActorRef<WeightCheckProcessor.WeightCommand> weightProcessor = getContext().spawn(WeightCheckProcessor.create(orderProcessor), "WeightCheckProcessor");

        OrderServiceImpl orderService = new OrderServiceImpl(orderProcessor, getContext().getSystem());
        WeightCheckServiceImpl weightCheckServiceImpl = new WeightCheckServiceImpl(weightProcessor, getContext().getSystem());

        CompletionStage<ServerBinding> binding = Http.get(getContext().getSystem())
                        .newServerAt("localhost",8080)
                        .bind(
                                ServiceHandler.concatOrNotFound(
                                        OrderServiceHandlerFactory.create(orderService, getContext().getSystem()),
                                        weightCheckServiceHandlerFactory.create(weightCheckServiceImpl,getContext().getSystem()),
                                        ServerReflection.create(java.util.List.of(
                                                OrderService.description,
                                                weightCheckService.description
                                        ), getContext().getSystem())));

        getContext().getLog().info("OrderHandler started");
        binding.thenAccept(serverBinding -> {getContext().getLog().info("Server online at http://localhost:8080/");});

    }


    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().build();
    }
}
