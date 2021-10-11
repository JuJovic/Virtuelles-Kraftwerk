package src.main.java.mqtt;

import src.main.java.ComponentInfo;
import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;



public class Subscriber extends Thread {

  private String broker;

  // Das Pluszeichen ist ein Platzhalter für eine Komponenten-ID)
  private final String TOPIC = "power/component/+/info";

  public Subscriber(int port) {
    this.broker = "tcp://mosquitto:" + port;
  }

  public void run() {
    try {
      MqttClient client = new MqttClient(broker, MqttClient.generateClientId());
      client.setCallback(new SensorCallback());

      client.connect();
      System.out.println("Verbunden zum MQTT broker: " + client.getServerURI());

      client.subscribe(TOPIC);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
}