package at.fhv.sysarch.lab2.homeautomation.order;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.grpc.GrpcClientSettings;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderReply;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderRequest;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderServiceClient;

import java.util.concurrent.CompletionStage;

public class OrderExecutor extends AbstractBehavior<OrderExecutor.OrderCommand> {
    public interface OrderCommand {}
    private OrderServiceClient client;

    /// Messages

    public static final class Order implements OrderCommand{
        public final Product product;
        public Order(Product product) {
            this.product = product;
        }
    }


    //Create Sachen:
    public static Behavior<OrderCommand> create() {
        return Behaviors.setup(context -> {
            GrpcClientSettings settings = GrpcClientSettings
                    .connectToServiceAt("localhost", 8080, context.getSystem())
                    .withTls(false);
            OrderServiceClient client = OrderServiceClient.create(settings, context.getSystem());
            return new OrderExecutor(context, client);
        });


    }
    private OrderExecutor(ActorContext<OrderCommand> context, OrderServiceClient client){
        super(context);
        this.client = client;
        //TODO: ActorRef to WeightSensor and SpaceSensor. From Fridge.
        getContext().getLog().info("OrderExecutor started");
    }


    ///Behaviors

    private Behavior<OrderCommand> onOrder(Order order){
        //TODO: Start simple, just send a message to OrderProcessor through gRPC
        getContext().getLog().info("OrderExecutor received order for {}", order.product.getName());
        CompletionStage<OrderReply> request = this.client.order(OrderRequest.newBuilder().setProduct(order.product.getName()).build());
        request.thenAccept(reply -> getContext().getLog().info("Order processed {}", reply.getSuccessful()));

        return this;
    }


    @Override
    public Receive<OrderCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(Order.class, this::onOrder)
                .build();
    }
}
