package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.order.OrderExecutor;

public class WeightSensor extends AbstractBehavior<WeightSensor.WeightCommand> {
    public interface WeightCommand {}
    private double maximumWeight = 100;
    private double currentWeight = 0;

    //TODO: WeightSensor needs to check for available weight and respond accordingly -> Bool
    //TODO: WeightSensor needs to be able to increase/decrease weight
    //Messages

    public static final class WeightCheck implements WeightCommand{
        private final float weight;
        private final ActorRef<OrderExecutor.OrderCommand> replyTo;
        public WeightCheck(float weight, ActorRef<OrderExecutor.OrderCommand> replyTo){
            this.weight = weight;
            this.replyTo = replyTo;

        }
    }

    public static final class IncreaseWeight implements WeightCommand{
        private final float weight;
        public IncreaseWeight(Float weight){
            this.weight = weight;
        }
    }

    public static final class DecreaseWeight implements WeightCommand {
        private final float weight;
        public DecreaseWeight(Float weight){
            this.weight = weight;
        }
    }

    //Behaviors

    private Behavior<WeightCommand> onWeightCheck(WeightCheck msg){
        msg.replyTo.tell(new OrderExecutor.WeightSensorAnswer((currentWeight+msg.weight <= maximumWeight), msg.weight));
        return this;
    }

    private Behavior<WeightCommand> onIncreaseWeight(IncreaseWeight msg){
        this.currentWeight+=msg.weight;
        getContext().getLog().info("Current Weight after stocking up: {}", this.currentWeight);
        return this;
    }

    private Behavior<WeightCommand> onDecreaseWeight(DecreaseWeight msg){
        this.currentWeight-=msg.weight;
        getContext().getLog().info("Current Weight after consumption: {}", this.currentWeight);

        return this;
    }




    public static Behavior<WeightCommand> create() {return Behaviors.setup(WeightSensor::new);}

    private WeightSensor(ActorContext<WeightCommand> context){
        super(context);
        getContext().getLog().info("WeightSensor started");
    }



    @Override
    public Receive<WeightCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(WeightCheck.class, this::onWeightCheck)
                .onMessage(IncreaseWeight.class, this::onIncreaseWeight)
                .onMessage(DecreaseWeight.class, this::onDecreaseWeight)
                .build();
    }

}
