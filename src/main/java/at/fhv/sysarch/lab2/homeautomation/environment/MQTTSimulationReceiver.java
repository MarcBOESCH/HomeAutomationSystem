package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherSensor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MQTTSimulationReceiver extends AbstractBehavior<MQTTSimulationReceiver.ReceiverCommand> {

    public interface ReceiverCommand {}

    public static final class StopMqttReceiver implements ReceiverCommand {}
    private final MqttClient client;

    public static Behavior<ReceiverCommand> create(
            ActorRef<WeatherSensor.WeatherCommand> weatherSensor,
            ActorRef<TemperatureSensor.TemperatureCommand> tempSensor
    ) {
        return Behaviors.setup(context -> new MQTTSimulationReceiver(context, weatherSensor, tempSensor));
    }

    private MQTTSimulationReceiver(
            ActorContext<ReceiverCommand> context,
            ActorRef<WeatherSensor.WeatherCommand> weatherSensor,
            ActorRef<TemperatureSensor.TemperatureCommand> tempSensor
    ) throws MqttException {
        super(context);

        ObjectMapper objectMapper = new ObjectMapper();

        //MQTT connection
        client = new MqttClient("tcp://10.0.40.161:1883", MqttClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        client.connect(options);

        getContext().getLog().info("MQTT simulation connected");

        // Subscribe to weather topic
        client.subscribe("weather/condition", (topic, message) -> {
            String payload = new String(message.getPayload());
            getContext().getLog().info("MQTT weather condition: {}", payload);
            try {
                JsonNode json = objectMapper.readTree(payload);
                if (json.has("condition")) {
                    String condition = json.get("condition").asText();
                    weatherSensor.tell(new WeatherSensor.ReadWeather(condition));
                }
            } catch (Exception e) {
                getContext().getLog().error("Failed to parse weather condition", e);
            }
        });

        // Subscribe to temperature topic
        client.subscribe("weather/temperature", (topic, message) -> {
            String payload = new String(message.getPayload());
            getContext().getLog().info("MQTT temperature: {}", payload);
            try {
                JsonNode json = objectMapper.readTree(payload);
                if (json.has("temperature")) {
                    double temp = json.get("temperature").asDouble();
                    tempSensor.tell(new TemperatureSensor.ReadTemperature(temp));
                }
            } catch (Exception e) {
                getContext().getLog().error("Failed to parse temperature", e);
            }
        });
    }

    @Override
    public Receive<ReceiverCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(StopMqttReceiver.class, message -> onPostStop())
                .build();
    }

    private Behavior<ReceiverCommand> onPostStop() {
        try {
            client.disconnect();
            getContext().getLog().info("MQTT Simulation disconnected");
        } catch (MqttException e) {
            getContext().getLog().error("Error disconnecting from MQTT Simulation", e);
        }
        return Behaviors.stopped();
    }
}
