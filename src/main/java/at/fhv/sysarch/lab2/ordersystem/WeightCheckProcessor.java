package at.fhv.sysarch.lab2.ordersystem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderRequest;
import at.fhv.sysarch.lab2.homeautomation.grpc.ProductWeightReply;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class WeightCheckProcessor extends AbstractBehavior<WeightCheckProcessor.WeightCommand> {
    public interface WeightCommand{}
    private final ActorRef<OrderProcessor.ProcessOrderCommand> orderProcessor;

    public static Behavior<WeightCheckProcessor.WeightCommand> create(ActorRef<OrderProcessor.ProcessOrderCommand> orderProcessor){
        return Behaviors.setup(context -> new WeightCheckProcessor(context, orderProcessor));
    }
    private WeightCheckProcessor(ActorContext<WeightCheckProcessor.WeightCommand> context,
                                 ActorRef<OrderProcessor.ProcessOrderCommand> orderProcessor){
        super(context);
        this.orderProcessor = orderProcessor;
        getContext().getLog().info("WeightCheckProcessor started");
    }
    public static final class WeightCheck implements WeightCommand {
        private final OrderRequest order;
        private final ActorRef<ProductWeightReply> replyTo;
        public WeightCheck(OrderRequest order, ActorRef<ProductWeightReply> replyTo){
            this.order = order;
            this. replyTo = replyTo;
        }
    }

    public static final class WeightResult implements WeightCommand {
        private final float weight;
        private final String productName;
        private final ActorRef<ProductWeightReply> originalSender;
        public WeightResult(float weight, String productName, ActorRef<ProductWeightReply> originalSender){
            this.weight = weight;
            this.productName = productName;
            this.originalSender = originalSender;
        }
    }

    private Behavior<WeightCommand> onWeightCheck(WeightCheck msg){
        orderProcessor.tell(new OrderProcessor.CheckForWeight(msg.order.getProduct(), getContext().getSelf(), msg.replyTo));
        return this;
    }

    private Behavior<WeightCommand> onWeightResult(WeightResult msg){
        ProductWeightReply result = ProductWeightReply.newBuilder()
                        .setSuccessful(true)
                        .setWeight(msg.weight)
                        .build();

        msg.originalSender.tell(result);
        return this;
    }


    @Override
    public Receive<WeightCommand> createReceive() {
        return newReceiveBuilder()
                .build();
    }
}
