package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class AirCondition extends AbstractBehavior<AirCondition.AirConditionCommand> {
    public interface AirConditionCommand {}

    public static final class PowerAirCondition implements AirConditionCommand {
        final Boolean value;

        public PowerAirCondition(Boolean value) {
            this.value = value;
        }
    }

    public static final class EnrichedTemperature implements AirConditionCommand {
        Double value;
        String unit;

        public EnrichedTemperature(Double value, String unit) {
            this.value = value;
            this.unit = unit;
        }
    }

    private final String identifier;
    private boolean isPowered = false;

    public AirCondition(ActorContext<AirConditionCommand> context, String identifier) {
        super(context);
        this.identifier = identifier;
        getContext().getLog().info("AirCondition started");
    }

    public static Behavior<AirConditionCommand> create(String identifier) {
        return Behaviors.setup(context -> new AirCondition(context, identifier));
    }

    @Override
    public Receive<AirConditionCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(PowerAirCondition.class, this::onPowerChange)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<AirConditionCommand> onReadTemperature(EnrichedTemperature message) {
        getContext().getLog().info("Aircondition reading {}", message.value);

        if (!isPowered) {
            getContext().getLog().info("Temperature is {} {}, but AC is powered OFF", message.value, message.unit);
        } else if (message.value > 25) {
            getContext().getLog().info("Temperature is {} {}, cooling started", message.value, message.unit);
        } else {
            getContext().getLog().info("Temperature is {} {}, cooling stopped", message.value, message.unit);
        }

        return Behaviors.same();
    }

    private Behavior<AirConditionCommand> onPowerChange(PowerAirCondition message) {
        this.isPowered = message.value;

        getContext().getLog().info("AC power is turned {}", isPowered ? "ON" : "OFF");
        if (isPowered) {
            return Behaviors.receive(AirConditionCommand.class)
                    .onMessage(PowerAirCondition.class, this::onPowerChange)
                    .onMessage(EnrichedTemperature.class, this::onReadTemperature)
                    .onSignal(PostStop.class, signal -> onPostStop())
                    .build();
        }

        return Behaviors.same();
    }

    private AirCondition onPostStop() {
        getContext().getLog().info("AirCondition actor {}-{} stopped", identifier);
        return this;
    }
}
