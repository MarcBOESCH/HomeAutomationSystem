package at.fhv.sysarch.lab2.ordersystem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.util.Timeout;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderReply;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderRequest;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderService;
import at.fhv.sysarch.lab2.homeautomation.grpc.ProductWeightReply;

import javax.annotation.processing.Completion;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class OrderServiceImpl implements OrderService {

    ActorRef<OrderProcessor.ProcessOrderCommand> orderProcessor;
    ActorSystem<?> system;

    public OrderServiceImpl(ActorRef<OrderProcessor.ProcessOrderCommand> orderProcessor, ActorSystem<?> system) {
        this.orderProcessor = orderProcessor;
        this.system = system;
    }

    //Implementation of when an Order is Reveiced -> tell orderProcessor to do smth.
    @Override
    public CompletionStage<OrderReply> order(OrderRequest in) {
        System.out.println("OrderImpl: Order received: " + in.getProduct() + " " + in.getAmount());

        Timeout timeout = Timeout.create(Duration.ofSeconds(3));

        return AskPattern.ask(
                orderProcessor,
                replyTo -> new OrderProcessor.ProcessOrder(in, replyTo),
                Duration.ofSeconds(3),
                system.scheduler()

        );

    }

    @Override
    public CompletionStage<ProductWeightReply> checkWeight(OrderRequest in) {
        return AskPattern.ask(
                orderProcessor,
                replyTo -> new OrderProcessor.CheckForWeight(in.getProduct(), replyTo),
                Duration.ofSeconds(3),
                system.scheduler()
        );
    }


}
