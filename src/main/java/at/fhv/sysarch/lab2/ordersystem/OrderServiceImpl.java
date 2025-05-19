package at.fhv.sysarch.lab2.ordersystem;

import akka.actor.typed.ActorRef;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderReply;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderRequest;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class OrderServiceImpl implements OrderService {

    ActorRef<String> orderProcessor;

    public OrderServiceImpl(ActorRef<String> orderProcessor) {this.orderProcessor = orderProcessor;}

    //Implementation of when an Order is Reveiced -> tell orderProcessor to do smth.
    @Override
    public CompletionStage<OrderReply> order(OrderRequest in) {
        System.out.println("Order received: " + in.getProduct());
        orderProcessor.tell(in.getProduct());

        //TODO: always returns true; which is weird for now, so change it to the actual implementation.
        return CompletableFuture.completedFuture(OrderReply.newBuilder().setSuccessful(true).build());
    }
}
