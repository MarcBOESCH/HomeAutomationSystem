package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class WeightSensor extends AbstractBehavior<WeightSensor.WeightCommand> {
    public interface WeightCommand {}
    private double maximumWeight = 100;
    private double currentWeight = 0;

    //TODO: WeightSensor needs to check for available weight and respond accordingly -> Bool
    //TODO: WeightSensor needs to be able to increase/decrease weight
    //Messages


    //Behaviors






    public static Behavior<WeightCommand> create() {return Behaviors.setup(WeightSensor::new);}

    private WeightSensor(ActorContext<WeightCommand> context){
        super(context);
        getContext().getLog().info("WeightSensor started");
    }


    @Override
    public Receive<WeightCommand> createReceive() {
        return null;
    }

}
