package src.main.java.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Publisher {

  public static final int QOS_EXACTLY_ONCE = 2;

  private String broker;
  private String topic;
  private MqttClient client;

  public Publisher(int port, String topic) {
    this.broker = "tcp://mosquitto:" + port;
    this.topic = topic;
    try {
      connect();
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  public void connect() throws MqttException {
    MqttConnectOptions mqttConnectOpts = new MqttConnectOptions();
    mqttConnectOpts.setCleanSession(true);

    this.client = new MqttClient(broker, MqttClient.generateClientId());
    client.connect(mqttConnectOpts);
    System.out.println("Verbunden zum MQTT broker: " + client.getServerURI());
  }

  public void disconnect() throws MqttException {
    // Disconnect from the MQTT broker.
    client.disconnect();
    System.out.println("Trennen vom MQTT broker.");
  }

  public void sendInfo(byte[] data) {

    try {
      // Create the message and set a quality-of-service parameter.
      MqttMessage message = new MqttMessage(data);
      message.setQos(QOS_EXACTLY_ONCE);

      // Publish the message.
      client.publish(this.topic, message);
    } catch (MqttPersistenceException e) {
      e.printStackTrace();
    } catch (MqttSecurityException e) {
      e.printStackTrace();
    } catch (MqttException e) {
      System.err.println("Fehler gefunden: " + e.getMessage());
    }
  }
}