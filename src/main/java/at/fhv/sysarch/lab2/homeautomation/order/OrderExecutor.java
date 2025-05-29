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
import at.fhv.sysarch.lab2.homeautomation.grpc.*;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.concurrent.CompletionStage;

public class OrderExecutor extends AbstractBehavior<OrderExecutor.OrderCommand> {
    public interface OrderCommand {}

    private OrderServiceClient client;
    private final int amount;
    private final ActorRef<WeightSensor.WeightCommand> weightSensor;
    private final ActorRef<SmartFridge.FridgeCommand> fridge;
    private final ActorRef<SpaceSensor.SpaceCommand> spaceSensor;
    private final String productName;

    //Uninitialized HashMap to use as checks, whether space/weight is good or not.
    private final HashMap<String, Boolean> spaceProduct = new HashMap<>();
    private final HashMap<String, Float> weightProduct = new HashMap<>();

    /// Messages



    public static final class Order implements OrderCommand{
        public Order() {
        }
    }

    public static final class SpaceSensorAnswer implements OrderCommand{
        private final Boolean spaceAnswer;
        public SpaceSensorAnswer(Boolean spaceAnswer) {
            this.spaceAnswer = spaceAnswer;
        }
    }

    public static final class WeightSensorAnswer implements OrderCommand{
        private final Boolean weightAnswer;
        private final Float weight;
        public WeightSensorAnswer(Boolean weightAnswer, float weight) {
            this.weightAnswer = weightAnswer;
            this.weight = weight;
        }
    }


    //Create Sachen:
    public static Behavior<OrderCommand> create(
            String product,
            ActorRef<WeightSensor.WeightCommand> weightSensor,
            ActorRef<SpaceSensor.SpaceCommand> spaceSensor,
            ActorRef<SmartFridge.FridgeCommand> fridge,
            int amount) {
        return Behaviors.setup(context -> {
            GrpcClientSettings settings = GrpcClientSettings
                    .connectToServiceAt("localhost", 8080, context.getSystem())
                    .withTls(false);
            OrderServiceClient client = OrderServiceClient.create(settings, context.getSystem());
            return new OrderExecutor(context, client, product, weightSensor, spaceSensor, fridge, amount);
        });


    }
    private OrderExecutor(
            ActorContext<OrderCommand> context,
            OrderServiceClient client,
            String product,
            ActorRef<WeightSensor.WeightCommand> weightSensor,
            ActorRef<SpaceSensor.SpaceCommand> spaceSensor,
            ActorRef<SmartFridge.FridgeCommand> fridge,
            int amount){
        super(context);
        this.client = client;
        this.weightSensor = weightSensor;
        this.fridge = fridge;
        this.spaceSensor = spaceSensor;
        this.productName = product;
        this.amount = amount;
        getContext().getLog().info("OrderExecutor started");
        getContext().getSelf().tell(new Order());
    }


    ///Behaviors

    private Behavior<OrderCommand> onOrder(Order order){
        getContext().getLog().info("OrderExecutor received order for {}", this.productName);

        this.spaceSensor.tell(new SpaceSensor.SpaceCheck(getContext().getSelf()));

        //TODO: 2. Check for WeightLimit
        Logger logger = getContext().getLog();
        CompletionStage<ProductWeightReply> weightRequest = this.client.checkWeight(OrderRequest.newBuilder().setProduct(this.productName)
                .setAmount(this.amount)
                .build());
        weightRequest.whenComplete((reply, throwable) -> {
            if(throwable != null){
                logger.error("Error while checking for Weight", throwable);
                this.fridge.tell(new SmartFridge.OrderUnsuccessful(throwable.toString()));
            } else {
                logger.info("WeightCheck Successful: {}", reply.getWeight());
                this.weightSensor.tell(new WeightSensor.WeightCheck(reply.getWeight(), getContext().getSelf()));
            }

        });

        return this;
    }

    private Behavior<OrderCommand> onOrderExecution(){
        getContext().getLog().info("OrderExecution started for order {}", this.productName);
        CompletionStage<OrderReply> request = this.client.order(OrderRequest.newBuilder().setProduct(this.productName)
                .setAmount(this.amount)
                .build());
        Logger logger = getContext().getLog();
        //request.thenAccept(reply -> getContext().getLog().info("Order processed {}", reply.getSuccessful()));
        request.whenComplete((reply, throwable) -> {
            if(throwable != null){
                logger.error("Error while processing order", throwable);
                this.fridge.tell(new SmartFridge.OrderUnsuccessful(throwable.toString()));
            } else {
                logger.info("Order processed: {}, {}, {}, {}", reply.getSuccessful(), reply.getAmount(), reply.getPrice(), reply.getWeight());

                this.spaceSensor.tell(new SpaceSensor.FillSpace());
                this.weightSensor.tell(new WeightSensor.IncreaseWeight(weightProduct.get(productName)));
                this.fridge.tell(new SmartFridge.OrderSuccessful(reply, this.productName));


            }
        });
        //Terminate child actor (what a sentence)
        return Behaviors.stopped();
    }

    //Check whether or not WeightCheck and SpaceCheck have answered. maybe return Behaviors still, so Behaviors.stopped(); can be used
    private Behavior<OrderCommand> checkForSensorAnswers(){
        if(!spaceProduct.isEmpty() && !weightProduct.isEmpty()){
            if(spaceProduct.get(productName) == false){
                getContext().getLog().info("Not enough space in fridge");

                return Behaviors.stopped();
            }
            else if(weightProduct.get(productName) < 0F){
                getContext().getLog().info("Too much weight in fridge");

                return Behaviors.stopped();
            }
            else {

                return onOrderExecution();
            }
        }
        return this;
    }

    private Behavior<OrderCommand> onSpaceAnswer(SpaceSensorAnswer answer){
        this.spaceProduct.put(productName, answer.spaceAnswer);
        getContext().getLog().info("SpaceProduct: " + this.spaceProduct.get(productName));
        return checkForSensorAnswers();
    }

    private Behavior<OrderCommand> onWeightAnswer(WeightSensorAnswer answer){
        Float savedFloat = answer.weightAnswer ? answer.weight : -1F;
        this.weightProduct.put(productName, savedFloat);

        return checkForSensorAnswers();
    }


    @Override
    public Receive<OrderCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(Order.class, this::onOrder)
                .onMessage(WeightSensorAnswer.class, this::onWeightAnswer)
                .onMessage(SpaceSensorAnswer.class, this::onSpaceAnswer)
                .build();
    }

    //For shutdown
    /*return Behaviors.stopped();*/
}
