package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.MediaStation;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;

import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private final ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private final ActorRef<AirCondition.AirConditionCommand> airCondition;
    private final ActorRef<MediaStation.MediaCommand> mediaStation;

    public static Behavior<Void> create(ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
                                        ActorRef<AirCondition.AirConditionCommand> airCondition,
                                        ActorRef<MediaStation.MediaCommand> mediaStation) {
        return Behaviors.setup(context -> new UI(context, tempSensor, airCondition, mediaStation));
    }

    private  UI(ActorContext<Void> context,
                ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
                ActorRef<AirCondition.AirConditionCommand> airCondition,
                ActorRef<MediaStation.MediaCommand> mediaStation) {
        super(context);
        // TODO: implement actor and behavior as needed
        // TODO: move UI initialization to appropriate place
        this.airCondition = airCondition;
        this.tempSensor = tempSensor;
        this.mediaStation = mediaStation;
        new Thread(this::runCommandLine).start();

        getContext().getLog().info("UI started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private UI onPostStop() {
        getContext().getLog().info("UI stopped");
        return this;
    }

    public void runCommandLine() {
        // TODO: Create Actor for UI Input-Handling?
        Scanner scanner = new Scanner(System.in);
        String[] input = null;
        String reader = "";


        while (!reader.equalsIgnoreCase("quit") && scanner.hasNextLine()) {
            reader = scanner.nextLine();
            // TODO: change input handling
            String[] command = reader.split(" ");
            if(command[0].equalsIgnoreCase("t")) {
                this.tempSensor.tell(new TemperatureSensor.ReadTemperature(Double.valueOf(command[1])));
            }
            // AirCondition
            if (command[0].equalsIgnoreCase("ac") && command.length > 1) {
                if (command[1].equalsIgnoreCase("on")) {
                    this.airCondition.tell(new AirCondition.PowerAirCondition(true));
                } else if (command[1].equalsIgnoreCase("off")) {
                    this.airCondition.tell(new AirCondition.PowerAirCondition(false));
                } else {
                    System.out.println("Unknown AC command. Use 'ac on' or 'ac off'");
                }
            }
            // MediaStation
            if (command[0].equalsIgnoreCase("ms") && command.length > 1) {
                if (command[1].equalsIgnoreCase("on")) {
                    this.mediaStation.tell(new MediaStation.PowerMediaStation(true));
                } else if (command[1].equalsIgnoreCase("off")) {
                    this.mediaStation.tell(new MediaStation.PowerMediaStation(false));
                } else {
                    System.out.println("Unknown MediaStation command. Use 'ms on' or 'ms off'");
                }
            }
            // TODO: process Input
        }
        getContext().getLog().info("UI done");
    }
}
