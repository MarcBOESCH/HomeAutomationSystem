package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;

public class MediaStation extends AbstractBehavior<MediaStation.MediaCommand> {

    public interface MediaCommand {}

    public static class PowerMediaStation implements MediaCommand {
        private boolean value;

        public PowerMediaStation(Boolean value) {
            this.value = value;
        }
    }
    public static class PlayMovie implements MediaCommand {}
    public static class StopMovie implements MediaCommand {}

    private boolean isPowered = false;
    private boolean isPlaying = false;
    private final ActorRef<Blind.BlindCommand> blind;

    public static Behavior<MediaCommand> create(ActorRef<Blind.BlindCommand> blind) {
        return Behaviors.setup(context -> new MediaStation(context, blind));
    }

    private MediaStation(ActorContext<MediaCommand> context, ActorRef<Blind.BlindCommand> blind) {
        super(context);
        this.blind = blind;
        context.getLog().info("MediaStation available");
    }

    @Override
    public Receive<MediaCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(PowerMediaStation.class, this::onPowerMediaStation)
                .build();
    }

    private Behavior<MediaCommand> onPowerMediaStation(PowerMediaStation message) {
        this.isPowered = message.value;

        getContext().getLog().info("MediaStation power is turned {}", isPowered ? "ON" : "OFF");
        if (isPowered) {
            return Behaviors.receive(MediaCommand.class)
                    .onMessage(PowerMediaStation.class, this::onPowerMediaStation)
                    .onMessage(PlayMovie.class, this::onPlayMovie)
                    .onMessage(StopMovie.class, this::onStopMovie)
                    .onSignal(PostStop.class, signal -> onPostStop())
                    .build();
        }

        return Behaviors.same();
    }

    private Behavior<MediaCommand> onPlayMovie(PlayMovie message) {
        if (isPlaying) {
            getContext().getLog().info("A movie is already playing! Ignoring play request.");
        } else {
            isPlaying = true;
            getContext().getLog().info("Movie started - closing blinds.");
            blind.tell(new Blind.MediaStationPlaying(true));
        }
        return Behaviors.same();
    }

    private Behavior<MediaCommand> onStopMovie(StopMovie message) {
        if (!isPlaying) {
            getContext().getLog().info("No movie playing! Ignoring stop request");
        } else {
            isPlaying = false;
            getContext().getLog().info("Movie stopped");
            blind.tell(new Blind.MediaStationPlaying(false));
        }
        return Behaviors.same();
    }
}
