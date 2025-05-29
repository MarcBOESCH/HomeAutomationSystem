package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.order.OrderExecutor;

public class SpaceSensor extends AbstractBehavior<SpaceSensor.SpaceCommand> {
    public interface SpaceCommand {}
    private final int maximumSpace = 15;
    private int occupiedSpace = 0;


    public static Behavior<SpaceCommand> create() {return Behaviors.setup(SpaceSensor::new);
    }

    private SpaceSensor(ActorContext<SpaceCommand> context){
        super(context);
        getContext().getLog().info("SpaceSensor started");
    }

    public final static class SpaceCheck implements SpaceCommand{
        private final ActorRef<OrderExecutor.OrderCommand> replyTo;
        private final int amount;
        public SpaceCheck(int amount, ActorRef<OrderExecutor.OrderCommand> replyTo){
            this.replyTo = replyTo;
            this.amount = amount;
        }
    }
    public final static class FillSpace implements SpaceCommand{
        private final int amount;
        public FillSpace(int amount){
            this.amount = amount;
        }
    }
    public final static class FreeSpace implements SpaceCommand{
        public FreeSpace(){}
    }

    private Behavior<SpaceCommand> onSpaceCheck(SpaceCheck msg){
        boolean hasSpace = false;
        if(occupiedSpace + msg.amount < maximumSpace){
            hasSpace = true;
        }
        getContext().getLog().info("SpaceSensor Reply: " +  hasSpace);
        msg.replyTo.tell(new OrderExecutor.SpaceSensorAnswer(hasSpace));
        return this;
    }
    private Behavior<SpaceCommand> onFillSpace(FillSpace msg){
        this.occupiedSpace += msg.amount;
        getContext().getLog().info("Current occupied space after stocking up: {}", this.occupiedSpace);
        return this;
    }

    private Behavior<SpaceCommand> onFreeSpace(FreeSpace msg){
        this.occupiedSpace--;
        getContext().getLog().info("Current occupied space after consumption: {}", this.occupiedSpace);
        return this;
    }


    @Override
    public Receive<SpaceCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(SpaceCheck.class, this::onSpaceCheck)
                .onMessage(FillSpace.class, this::onFillSpace)
                .onMessage(FreeSpace.class, this::onFreeSpace)
                .build();
    }
}
