/// Handles the incoming order requests over gRPC
package at.fhv.sysarch.lab2.ordersystem;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

//Vorerst <String>, später nochmal schauen ob eine andere Typisierung mehr Sinn macht.

public class OrderProcessor extends AbstractBehavior<OrderRequest> {
    public interface Order {}
    private static final HashMap<String, Float> productList = new HashMap<>();
    private static final HashMap<String, Float> priceList = new HashMap<>();
    private final float minimum = 0.5f;
    private final float maximum = 5.0f;


    public static Behavior<OrderRequest> create(){
        return  Behaviors.setup(OrderProcessor::new);
    }

    private OrderProcessor(ActorContext<OrderRequest> context){
        super(context);
        getContext().getLog().info("OrderProcessor started");
    }

    @Override
    public Receive<OrderRequest> createReceive() {
        return newReceiveBuilder()
                .onMessage(OrderRequest.class, this::onOrderReceived)
                .build();
    }

    private Behavior<OrderRequest> onOrderReceived(OrderRequest request){
        if(!productList.containsKey(request.getProduct().toLowerCase())){
            productList.put(request.getProduct().toLowerCase(), ThreadLocalRandom.current().nextFloat(minimum, maximum));
            priceList.put(request.getProduct().toLowerCase(), ThreadLocalRandom.current().nextFloat(minimum, maximum-2.0f));
        }

        getContext().getLog().info("Order received: {}", request.getProduct());
        //TODO: process order, generate return value. -> OrderReply i guess
        return this;
    }
}
