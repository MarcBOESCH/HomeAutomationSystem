package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Blind extends AbstractBehavior<Blind.BlindCommand> {

    public interface BlindCommand {}

    public static final class MediaStationPlaying implements BlindCommand {
        Boolean value;

        public MediaStationPlaying(Boolean value) {
            this.value = value;
        }
    }

    public static final class WeatherChange implements BlindCommand {
        String condition;

        public WeatherChange(String condition) {
            this.condition = condition;
        }
    }

    private final String identifier;

    private boolean isClosed = false;
    private boolean isMoviePlaying = false;

    private Blind(ActorContext<BlindCommand> context, String identifier) {
        super(context);
        this.identifier = identifier;

        getContext().getLog().info("Blind ready");
    }

    public static Behavior<BlindCommand> create(String identifier) {
        return Behaviors.setup(context -> new Blind(context, identifier));
    }

    @Override
    public Receive<BlindCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(MediaStationPlaying.class, this::onMediaStationPlaying)
                .onMessage(WeatherChange.class, this::onWeatherChange)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<BlindCommand> onMediaStationPlaying(MediaStationPlaying message) {
        this.isMoviePlaying = message.value;

        if (isMoviePlaying) {
            isClosed = true;
            getContext().getLog().info("Movie started - blinds CLOSED - weather control deactivated");
        } else {
            getContext().getLog().info("Movie stopped - weather control active again");
        }
        return this;
    }

    private Behavior<BlindCommand> onWeatherChange(WeatherChange message) {
        if (isMoviePlaying) {
            getContext().getLog().info("Ignoring weather '{}': MediaStation is playing a movie", message.condition);
            return this;
        }

        getContext().getLog().info("Blinds reading {}", message.condition);
        if (message.condition.equalsIgnoreCase("sunny")) {
            isClosed = true;
            getContext().getLog().info("Blinds CLOSED due to sunny weather");
        } else {
            isClosed = false;
            getContext().getLog().info("Blinds OPENED due to {} weather", message.condition);
        }

        return this;
    }

    private Behavior<BlindCommand> onPostStop() {
        getContext().getLog().info("Blind actor {}-{} stopped", identifier);
        return this;
    }


}
