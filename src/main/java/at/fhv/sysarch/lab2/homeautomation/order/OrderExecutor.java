package at.fhv.sysarch.lab2.homeautomation.order;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.grpc.GrpcClientSettings;
import at.fhv.sysarch.lab2.homeautomation.devices.SmartFridge;
import at.fhv.sysarch.lab2.homeautomation.devices.SpaceSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.WeightSensor;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderReply;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderRequest;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderServiceClient;

import java.util.concurrent.CompletionStage;

public class OrderExecutor extends AbstractBehavior<OrderExecutor.OrderCommand> {
    public interface OrderCommand {}
    private OrderServiceClient client;
    private final ActorRef<WeightSensor> weightSensor;
    private final ActorRef<SmartFridge> fridge;
    private final ActorRef<SpaceSensor> spaceSensor;
    private final Product product;
    /// Messages

    public static final class Order implements OrderCommand{
        public final Product product;
        public Order(Product product) {
            this.product = product;
        }
    }


    //Create Sachen:
    public static Behavior<OrderCommand> create(
            Product product,
            ActorRef<WeightSensor> weightSensor,
            ActorRef<SpaceSensor> spaceSensor,
            ActorRef<SmartFridge> fridge) {
        return Behaviors.setup(context -> {
            GrpcClientSettings settings = GrpcClientSettings
                    .connectToServiceAt("localhost", 8080, context.getSystem())
                    .withTls(false);
            OrderServiceClient client = OrderServiceClient.create(settings, context.getSystem());
            return new OrderExecutor(context, client, product, weightSensor, spaceSensor, fridge);
        });


    }
    private OrderExecutor(
            ActorContext<OrderCommand> context,
            OrderServiceClient client,
            Product product,
            ActorRef<WeightSensor> weightSensor,
            ActorRef<SpaceSensor> spaceSensor,
            ActorRef<SmartFridge> fridge){
        super(context);
        this.client = client;
        this.weightSensor = weightSensor;
        this.fridge = fridge;
        this.spaceSensor = spaceSensor;
        this.product = product;
        getContext().getLog().info("OrderExecutor started");
        //TODO: Send message to self to start OrderProcess.
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
