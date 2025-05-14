package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.*;

public class UIHandler extends AbstractBehavior<UIHandler.UICommand> {

    public interface UICommand {}

    public static final class UserInput implements UICommand {
        public final String input;
        public UserInput(String input) {
            this.input = input;
        }
    }

    private final ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private final ActorRef<WeatherSensor.WeatherCommand> weatherSensor;
    private final ActorRef<AirCondition.AirConditionCommand> airCondition;
    private final ActorRef<MediaStation.MediaCommand> mediaStation;


    public static Behavior<UICommand> create(
            ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
            ActorRef<WeatherSensor.WeatherCommand> weatherSensor,
            ActorRef<AirCondition.AirConditionCommand> airCondition,
            ActorRef<MediaStation.MediaCommand> mediaStation
    ) {
        return Behaviors.setup(context ->
                new UIHandler(context, tempSensor, weatherSensor, airCondition, mediaStation));
    }

    private UIHandler(
            ActorContext<UICommand> context,
            ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
            ActorRef<WeatherSensor.WeatherCommand> weatherSensor,
            ActorRef<AirCondition.AirConditionCommand> airCondition,
            ActorRef<MediaStation.MediaCommand> mediaStation
    ) {
        super(context);
        this.tempSensor = tempSensor;
        this.weatherSensor = weatherSensor;
        this.airCondition = airCondition;
        this.mediaStation = mediaStation;
        context.getLog().info("UIHandler started");
    }

    @Override
    public Receive<UICommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(UserInput.class, this::onUserInput)
                .build();
    }

    private Behavior<UICommand> onUserInput(UserInput message) {
        String[] command = message.input.trim().split(" ");
        if (command.length == 0 || command[0].isEmpty()) {
            return this;
        }

        switch (command[0].toLowerCase()) {
            // TemperatureSimulation
            case "t":
                if (command.length >= 2) {
                    try {
                        double value = Double.parseDouble(command[1]);
                        tempSensor.tell(new TemperatureSensor.ReadTemperature(value));
                        getContext().getLog().info("Set temperature to {}°C", value);
                    } catch (NumberFormatException e) {
                        getContext().getLog().info("Invalid temperature value: {}", command[1]);
                    }
                } else {
                    getContext().getLog().info("Usage: t <value>");
                }
                return this;

            // WeatherSimulation
            case "w":
                if (command.length >= 2) {
                    String condition = command[1];
                    weatherSensor.tell(new WeatherSensor.ReadWeather(condition));
                    getContext().getLog().info("Weather set to '{}'", condition);
                } else {
                    getContext().getLog().info("Usage: w <condition>");
                }
                return this;

            // AirCondition
            case "ac":
                if (command.length >= 2) {
                    if (command[1].equalsIgnoreCase("on")) {
                        airCondition.tell(new AirCondition.PowerAirCondition(true));
                    } else if (command[1].equalsIgnoreCase("off")) {
                        airCondition.tell(new AirCondition.PowerAirCondition(false));
                    } else {
                        getContext().getLog().info("Usage: ac on/off");
                    }
                } else {
                    getContext().getLog().info("Usage: ac on/off");
                }
                return this;

            // MediaStation
            case "ms":
                if (command.length >= 2) {
                    if (command[1].equalsIgnoreCase("on")) {
                        mediaStation.tell(new MediaStation.PowerMediaStation(true));
                    } else if (command[1].equalsIgnoreCase("off")) {
                        mediaStation.tell(new MediaStation.PowerMediaStation(false));
                    } else if (command[1].equalsIgnoreCase("play")) {
                        mediaStation.tell(new MediaStation.PlayMovie());
                    } else if (command[1].equalsIgnoreCase("stop")) {
                        mediaStation.tell(new MediaStation.StopMovie());
                    } else {
                        getContext().getLog().info("Usage: ms on/off/play/stop");
                    }
                } else {
                    getContext().getLog().info("Usage: ms on/off/play/stop");
                }
                return this;

            case "quit":
                getContext().getLog().info("Shutting down UI");
                System.exit(0);
                return this;

            default:
                getContext().getLog().info("Unknown command: {}", message.input);
                return this;
        }
    }
}
