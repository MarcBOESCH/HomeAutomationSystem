package at.fhv.sysarch.lab2.homeautomation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.HomeAutomationSystem;
import at.fhv.sysarch.lab2.homeautomation.devices.*;
import at.fhv.sysarch.lab2.homeautomation.ui.UIHandler;

import java.util.UUID;

/*
Defines which Actors are available
Creates the other Actors
 */
public class HomeAutomationController extends AbstractBehavior<Void>{

    public static Behavior<Void> create() {
        return Behaviors.setup(HomeAutomationController::new);
    }

    private  HomeAutomationController(ActorContext<Void> context) {
        super(context);

        // Devices
        ActorRef<Blind.BlindCommand> blinds = getContext().spawn(Blind.create(UUID.randomUUID().toString()), "Blinds");
        ActorRef<AirCondition.AirConditionCommand> airCondition = getContext().spawn(AirCondition.create(UUID.randomUUID().toString()), "AirCondition");
        ActorRef<MediaStation.MediaCommand> mediaStation = getContext().spawn(MediaStation.create(blinds), "MediaStation");
        ActorRef<TemperatureSensor.TemperatureCommand> tempSensor = getContext().spawn(TemperatureSensor.create(airCondition), "temperatureSensor");
        ActorRef<WeatherSensor.WeatherCommand> weatherSensor = getContext().spawn(WeatherSensor.create(blinds), "weatherSensor");
        ActorRef<SmartFridge.FridgeCommand> fridge = getContext().spawn(SmartFridge.create(), "SmartFridge");

        // UI
        ActorRef<UIHandler.UICommand> uiHandler = getContext().spawn(UIHandler.create(tempSensor, weatherSensor, airCondition, mediaStation, fridge), "UIHandler");
        HomeAutomationSystem.setUiHandler(uiHandler);
        getContext().getLog().info("HomeAutomation Application started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private HomeAutomationController onPostStop() {
        getContext().getLog().info("HomeAutomation Application stopped");
        return this;
    }
}
