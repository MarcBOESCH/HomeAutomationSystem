package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;

import java.time.Duration;
import java.util.Random;

/*
This actor simulates the environment temperature.
He changes the temperature periodically and sends it to the TemperatureSensor.
 */
public class TemperatureSimulation extends AbstractBehavior<TemperatureSimulation.TemperatureSimulationCommand> {

    // Interface for all messages that this actor can receive
    public interface TemperatureSimulationCommand {}

    // Internal message used to trigger periodic temperature updates
    private static class Tick implements TemperatureSimulationCommand {}

    private final ActorRef<TemperatureSensor.TemperatureCommand> temperatureSensor;
    private double currentTemperature;
    private final Random random = new Random();

    public static Behavior<TemperatureSimulationCommand> create(ActorRef<TemperatureSensor.TemperatureCommand> temperatureSensor, double initialTemperature) {
        return Behaviors.setup(context -> new TemperatureSimulation(context, initialTemperature, temperatureSensor));
    }

    private TemperatureSimulation(ActorContext<TemperatureSimulationCommand> context, double initialTemperature, ActorRef<TemperatureSensor.TemperatureCommand> temperatureSensor) {
        super(context);
        this.currentTemperature = initialTemperature;
        this.temperatureSensor = temperatureSensor;

        // Schedule a Tick message to self every 5 second
        context.getSystem().scheduler().scheduleAtFixedRate(
                Duration.ofSeconds(5),
                Duration.ofSeconds(5),
                () -> context.getSelf().tell(new Tick()),
                context.getSystem().executionContext()
        );

        context.getLog().info("TemperatureSimulation startet at {} Celsius", currentTemperature);
    }

    // Define how this actor handles incoming messages
    @Override
    public Receive<TemperatureSimulationCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(Tick.class, this::onTick)
                .build();
    }

    // Gets called every time a Tick message is received
    private Behavior<TemperatureSimulationCommand> onTick(Tick tick) {
        // Randomly increases or decreases temperature by max 5 degrees
        double change = (random.nextDouble() * 10 - 5);
        currentTemperature += change;

        // Limit temperature to range -20, 40
        if (currentTemperature > 40) {
            currentTemperature = 40;
        } else if (currentTemperature < -20.0) {
            currentTemperature = -20;
        }

        // Send new temperature to TemperatureSensor
        temperatureSensor.tell(new TemperatureSensor.ReadTemperature(currentTemperature));

        getContext().getLog().info("Simulated new temperature: {} Celsius", currentTemperature);

        return this;
    }

}
