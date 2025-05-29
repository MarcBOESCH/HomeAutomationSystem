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
    private int maximumSpace = 15;
    private int occupiedSpace = 0;


    public static Behavior<SpaceCommand> create() {return Behaviors.setup(SpaceSensor::new);
    }

    private SpaceSensor(ActorContext<SpaceCommand> context){
        super(context);
        getContext().getLog().info("SpaceSensor started");
    }

    public final static class SpaceCheck implements SpaceCommand{
        private final ActorRef<OrderExecutor.OrderCommand> replyTo;

        public SpaceCheck(ActorRef<OrderExecutor.OrderCommand> replyTo){
            this.replyTo = replyTo;
        }
    }

    private Behavior<SpaceCommand> onSpaceCheck(SpaceCheck msg){
        boolean hasSpace = false;
        if(occupiedSpace < maximumSpace){
            hasSpace = true;
        }
        getContext().getLog().info("SpaceSensor Reply: " +  hasSpace);
        msg.replyTo.tell(new OrderExecutor.SpaceSensorAnswer(hasSpace));
        return this;
    }


    @Override
    public Receive<SpaceCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(SpaceCheck.class, this::onSpaceCheck)
                .build();
    }
}
