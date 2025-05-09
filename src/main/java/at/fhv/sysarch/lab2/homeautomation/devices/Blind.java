package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Blind extends AbstractBehavior<Blind.BlindCommand> {

    public interface BlindCommand {}

    public static final class WeatherChange implements BlindCommand {
        String condition;

        public WeatherChange(String condition) {
            this.condition = condition;
        }
    }

    private final String identifier;

    private boolean isClosed = false;

    public Blind(ActorContext<BlindCommand> context, String identifier) {
        super(context);
        this.identifier = identifier;

        getContext().getLog().info("Blind available");
    }

    public static Behavior<BlindCommand> create(String identifier) {
        return Behaviors.setup(context -> new Blind(context, identifier));
    }

    @Override
    public Receive<BlindCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(WeatherChange.class, this::onWeatherChange)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<BlindCommand> onWeatherChange(WeatherChange message) {
        getContext().getLog().info("Blinds reading {}", message.condition);
        // TODO: Implement logic
        return Behaviors.same();
    }

    private Behavior<BlindCommand> onPostStop() {
        getContext().getLog().info("Blind actor {}-{} stopped", identifier);
        return this;
    }


}
