package at.fhv.sysarch.lab2.homeautomation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.Blind;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherSensor;
import at.fhv.sysarch.lab2.homeautomation.environment.TemperatureSimulation;
import at.fhv.sysarch.lab2.homeautomation.environment.WeatherSimulation;
import at.fhv.sysarch.lab2.homeautomation.ui.UI;

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
        // TODO: consider guardians and hierarchies. Who should create and communicate with which Actors?
        ActorRef<Blind.BlindCommand> blinds = getContext().spawn(Blind.create(UUID.randomUUID().toString()), "Blinds");
        ActorRef<AirCondition.AirConditionCommand> airCondition = getContext().spawn(AirCondition.create(UUID.randomUUID().toString()), "AirCondition");
        ActorRef<TemperatureSensor.TemperatureCommand> tempSensor = getContext().spawn(TemperatureSensor.create(airCondition), "temperatureSensor");
        ActorRef<WeatherSensor.WeatherCommand> weatherSensor = getContext().spawn(WeatherSensor.create(blinds), "weatherSensor");
        ActorRef<TemperatureSimulation.TemperatureSimulationCommand> temperatureSimulation = getContext().spawn(TemperatureSimulation.create(tempSensor,23.0), "TemperatureSimulation");
        ActorRef<WeatherSimulation.WeatherSimulationCommand> weatherSimulation = getContext().spawn(WeatherSimulation.create(weatherSensor), "WeatherSimulation");
        ActorRef<Void> ui = getContext().spawn(UI.create(tempSensor, airCondition), "UI");
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
