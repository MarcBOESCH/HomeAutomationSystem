package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class WeatherSensor extends AbstractBehavior<WeatherSensor.WeatherCommand> {

    public interface WeatherCommand {}

    public static final class ReadWeather implements WeatherCommand {
        final String condition;

        public ReadWeather(String condition) {
            this.condition = condition;
        }
    }

    private final ActorRef<Blind.BlindCommand> blind;

    public static Behavior<WeatherSensor.WeatherCommand> create(ActorRef<Blind.BlindCommand> blind) {
        return Behaviors.setup(context -> new WeatherSensor(context, blind));
    }

    public WeatherSensor(ActorContext<WeatherCommand> context, ActorRef<Blind.BlindCommand> blind) {
        super(context);
        this.blind = blind;
        getContext().getLog().info("WeatherSensor started");
    }

    @Override
    public Receive<WeatherCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadWeather.class, this::onReadWeather)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<WeatherSensor.WeatherCommand> onReadWeather(ReadWeather message) {
        getContext().getLog().info("WeatherSensor received {}", message.condition);
        blind.tell(new Blind.WeatherChange(message.condition));
        return this;
    }

    private WeatherSensor onPostStop() {
        getContext().getLog().info("WeatherSensor actor {}-{} stopped");
        return this;
    }
}
