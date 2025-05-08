package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

/*
AbstractBehavior --> makes class an Actor
Through it, we can define what messages the Actor can receive (TemperatureSensor.TemperatureCommand)
 */
public class TemperatureSensor extends AbstractBehavior<TemperatureSensor.TemperatureCommand> {

    // The message the Actor can receive
    public interface TemperatureCommand {}

    // Defines what can be inside a ReadTemperature command
    public static final class ReadTemperature implements TemperatureCommand {
        final Double value;

        public ReadTemperature(Double value) {
            this.value = value;
        }
    }

    // Initializes the Actor
    public static Behavior<TemperatureCommand> create(ActorRef<AirCondition.AirConditionCommand> airCondition) {
        return Behaviors.setup(context -> new TemperatureSensor(context, airCondition));
    }

    private final ActorRef<AirCondition.AirConditionCommand> airCondition;

    public TemperatureSensor(ActorContext<TemperatureCommand> context, ActorRef<AirCondition.AirConditionCommand> airCondition) {
        super(context);
        this.airCondition = airCondition;

        getContext().getLog().info("TemperatureSensor started");
    }

    // Defines how to handle receiving messages
    @Override
    public Receive<TemperatureCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadTemperature.class, this::onReadTemperature)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    // Defines the behavior
    private Behavior<TemperatureCommand> onReadTemperature(ReadTemperature r) {
        getContext().getLog().info("TemperatureSensor received {}", r.value);
        this.airCondition.tell(new AirCondition.EnrichedTemperature(r.value, "Celsius"));
        return this;
    }

    private TemperatureSensor onPostStop() {
        getContext().getLog().info("TemperatureSensor actor {}-{} stopped");
        return this;
    }

}
