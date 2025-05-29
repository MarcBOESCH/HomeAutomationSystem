package at.fhv.sysarch.lab2.ordersystem;

import akka.actor.typed.ActorRef;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderReply;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderRequest;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class OrderServiceImpl implements OrderService {

    ActorRef<OrderRequest> orderProcessor;

    public OrderServiceImpl(ActorRef<OrderRequest> orderProcessor) {this.orderProcessor = orderProcessor;}

    //Implementation of when an Order is Reveiced -> tell orderProcessor to do smth.
    @Override
    public CompletionStage<OrderReply> order(OrderRequest in) {
        //TODO: Build OrderReply in the Actor and in this Impl just ask for the OrderReply from the Actor.
        System.out.println("OrderImpl: Order received: " + in.getProduct() + " " + in.getAmount());
        orderProcessor.tell(in);

        //TODO: always returns true; which is weird for now, so change it to the actual implementation.
        return CompletableFuture.completedFuture(OrderReply.newBuilder().setSuccessful(true).build());
    }
}
