package src.main.java.mqtt;

import java.util.ArrayList;

import src.main.java.ComponentInfo;
import src.main.java.DataManager;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;


public class Subscriber extends Thread {

  private String broker;
  private String topic;

  public Subscriber() {
    DataManager dM = DataManager.getInstance();
    this.broker = "tcp://mosquitto:" + dM.getMqttPort();
    this.topic = dM.getMqttTopic();

  }

  public void run() {
    try {
      ArrayList<ComponentInfo> data = DataManager.getInstance().getData();
      MqttClient client = new MqttClient(broker, MqttClient.generateClientId());
      client.setCallback(new SensorCallback(data));

      // Verbindung zum MQTT broker.
      client.connect();
      System.out.println("Verbunden zum MQTT broker: " + client.getServerURI());

      // Subscribe to a topic.
      client.subscribe(this.topic);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
}