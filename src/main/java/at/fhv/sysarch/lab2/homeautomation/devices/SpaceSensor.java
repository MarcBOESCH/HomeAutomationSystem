package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

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


    @Override
    public Receive<SpaceCommand> createReceive() {
        return null;
    }
}
