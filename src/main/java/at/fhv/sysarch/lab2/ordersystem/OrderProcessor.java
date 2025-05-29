/// Handles the incoming order requests over gRPC
package at.fhv.sysarch.lab2.ordersystem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderReply;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderRequest;
import at.fhv.sysarch.lab2.homeautomation.grpc.ProductWeightReply;
import at.fhv.sysarch.lab2.homeautomation.grpc.ProductWeightReplyOrBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

//Vorerst <String>, später nochmal schauen ob eine andere Typisierung mehr Sinn macht.

public class OrderProcessor extends AbstractBehavior<OrderProcessor.ProcessOrderCommand> {
    public interface ProcessOrderCommand{}
    public interface Reply{}
    private static final HashMap<String, Float> productList = new HashMap<>();
    private static final HashMap<String, Float> priceList = new HashMap<>();
    private final float minimum = 0.5f;
    private final float maximum = 5.0f;

    public static final class ProcessOrder implements ProcessOrderCommand {
        private final OrderRequest order;
        private final ActorRef<OrderReply> replyTo;
        public ProcessOrder(OrderRequest order, ActorRef<OrderReply> replyTo){
            this.order = order;
            this.replyTo = replyTo;
        }
    }

    public static class ProcessOrderReply implements Reply {
        private final OrderReply reply;
        public ProcessOrderReply(OrderReply reply){
            this.reply = reply;
        }
    }

    public static class CheckForWeight implements ProcessOrderCommand{
        private final String productName;
        private final ActorRef<ProductWeightReply> replyTo;
        //private final ActorRef<ProductWeightReply> originalSender;
        public CheckForWeight(String productName, ActorRef<ProductWeightReply> replyTo){
            this.productName = productName;
            this.replyTo = replyTo;
            //this.originalSender = originalSender;
        }
    }

    public static Behavior<ProcessOrderCommand> create(){
        return  Behaviors.setup(OrderProcessor::new);
    }

    private OrderProcessor(ActorContext<ProcessOrderCommand> context){
        super(context);
        getContext().getLog().info("OrderProcessor started");
    }

    @Override
    public Receive<ProcessOrderCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ProcessOrder.class, this::onOrderReceived)
                .onMessage(CheckForWeight.class, this::onCheckForWeight)
                .build();
    }

    private Behavior<ProcessOrderCommand> onOrderReceived(ProcessOrder request){
        if(!productList.containsKey(request.order.getProduct().toLowerCase())){
            productList.put(request.order.getProduct().toLowerCase(), ThreadLocalRandom.current().nextFloat(minimum, maximum));
            priceList.put(request.order.getProduct().toLowerCase(), ThreadLocalRandom.current().nextFloat(minimum, maximum-2.0f));
        }

        getContext().getLog().info("Order received: {}", request.order.getProduct());
        //TODO: process order, generate return value. -> OrderReply i guess
        OrderReply result = OrderReply.newBuilder()
                .setSuccessful(true)
                .setWeight(productList.get(request.order.getProduct()))
                .setAmount(request.order.getAmount())
                .setPrice(priceList.get(request.order.getProduct()))
                .build();
        request.replyTo.tell(result);
        return this;
    }

    private Behavior<ProcessOrderCommand> onCheckForWeight(CheckForWeight msg){
        float weight = -1f;
        if(!productList.isEmpty()){
            if(productList.containsKey(msg.productName.toLowerCase())){
                weight = productList.get(msg.productName.toLowerCase());
            }
        }
        if(weight == -1f){
            weight = ThreadLocalRandom.current().nextFloat(minimum, maximum);
            productList.put(msg.productName.toLowerCase(), weight);
            priceList.put(msg.productName.toLowerCase(), ThreadLocalRandom.current().nextFloat(minimum, maximum-2.0f));
        }
        ProductWeightReply result = ProductWeightReply.newBuilder()
                        .setSuccessful(true)
                        .setWeight(weight)
                        .build();
        msg.replyTo.tell(result);
        getContext().getLog().info("WEight check complete");

        return this;
    }
}
