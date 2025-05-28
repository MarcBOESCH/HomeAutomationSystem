package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.ActorRef;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherSensor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;

public class MqttBridge {

    private static MqttClient client;
    public static void start(
            ActorRef<WeatherSensor.WeatherCommand> weatherSensor,
            ActorRef<TemperatureSensor.TemperatureCommand> tempSensor
    ) {
        new Thread(() -> {
            try {
                client = new MqttClient("tcp://10.0.40.161:1883", MqttClient.generateClientId());
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setAutomaticReconnect(true);
                client.connect(options);
                System.out.println("MQTT-Simulation connected");

                ObjectMapper mapper = new ObjectMapper();

                client.subscribe("weather/condition", 1, (topic, msg) -> {
                    String payload = new String(msg.getPayload());
                    System.out.println("MQTT-Simulation Condition: " + payload);
                    try {
                        JsonNode json = mapper.readTree(payload);
                        if (json.has("condition")) {
                            String condition = json.get("condition").asText();
                            weatherSensor.tell(new WeatherSensor.ReadWeather(condition));
                        }
                    } catch (Exception e) {
                        System.err.println("MQTT-Simulation Weather parse error: " + e.getMessage());
                    }
                });

                client.subscribe("weather/temperature", 1, (topic, msg) -> {
                    String payload = new String(msg.getPayload());
                    System.out.println("MQTT-Simulation Temperature: " + payload);
                    try {
                        JsonNode json = mapper.readTree(payload);
                        if (json.has("temperature")) {
                            String tempStr = json.get("temperature").asText();
                            double temp = Double.parseDouble(tempStr);
                            tempSensor.tell(new TemperatureSensor.ReadTemperature(temp));
                        }
                    } catch (Exception e) {
                        System.err.println("MQTT-Simulation Temp parse error: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                System.err.println("MQTT-Simulation Fatal error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public static void stop() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                System.out.println("MQTT-Simulation stopped");
            }
        } catch (Exception e) {
            System.err.println("MQTT-Simulation could not be stopped: " + e.getMessage());
        }
    }
}
