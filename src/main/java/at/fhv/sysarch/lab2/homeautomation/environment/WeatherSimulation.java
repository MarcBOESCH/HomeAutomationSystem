package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherSensor;

import java.time.Duration;
import java.util.List;
import java.util.Random;


public class WeatherSimulation extends AbstractBehavior<WeatherSimulation.WeatherSimulationCommand> {

    public interface WeatherSimulationCommand {}

    private static class Tick implements WeatherSimulationCommand {}

    private final ActorRef<WeatherSensor.WeatherCommand> weatherSensor;
    private final Random random = new Random();
    private final List<String> conditions = List.of("sunny", "cloudy", "rainy", "snowing");

    public static Behavior<WeatherSimulationCommand> create(ActorRef<WeatherSensor.WeatherCommand> weatherSensor) {
        return Behaviors.setup(context -> new WeatherSimulation(context, weatherSensor));
    }

    private WeatherSimulation(ActorContext<WeatherSimulationCommand> context, ActorRef<WeatherSensor.WeatherCommand> weatherSensor) {
        super(context);
        this.weatherSensor = weatherSensor;

        context.getSystem().scheduler().scheduleAtFixedRate(
                Duration.ofSeconds(5),
                Duration.ofSeconds(5),
                () -> context.getSelf().tell(new Tick()),
                context.getSystem().executionContext()
        );

        context.getLog().info("WeatherSimulation started");
    }

    @Override
    public Receive<WeatherSimulationCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(Tick.class, this::onTick)
                .build();
    }

    private Behavior<WeatherSimulationCommand> onTick(Tick tick) {
        String newCondition = conditions.get(random.nextInt(conditions.size()));
        weatherSensor.tell(new WeatherSensor().ReadWeather(newCondition));

        getContext().getLog().info("Simulated new weather: {}", newCondition);
        return this;
    }
}
