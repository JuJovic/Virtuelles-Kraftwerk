package src.main.java;

import java.io.IOException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class Publisher {

  public static final int QOS_EXACTLY_ONCE = 2;

  private String broker;
  private String topic;
  private MqttClient client;

  public Publisher(int port, int componentId) {
    this.broker = "tcp://mosquitto:" + port;
    this.topic = System.getenv("MQTT_TOPIC");
    try {
      connect();
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  public void connect() throws MqttException {
    // Create some MQTT connection options.
    MqttConnectOptions mqttConnectOpts = new MqttConnectOptions();
    mqttConnectOpts.setCleanSession(true);

    this.client = new MqttClient(broker, MqttClient.generateClientId());
    // Connect to the MQTT broker using the connection options.
    client.connect(mqttConnectOpts);
    System.out.println("Verbunden zum MQTT broker: " + client.getServerURI());
  }

  public void disconnect() throws MqttException {
    // Disconnect from the MQTT broker.
    client.disconnect();
    System.out.println("Trennen vom MQTT broker");
  }

  public void sendInfo(ComponentInfo info) {

    try {
      // Create the message and set a quality-of-service parameter.
      MqttMessage message = new MqttMessage(info.serialize());
      message.setQos(QOS_EXACTLY_ONCE);

      // Publish the message.
      client.publish(this.topic, message);
    } catch (MqttPersistenceException e) {
      e.printStackTrace();
    } catch (MqttSecurityException e) {
      e.printStackTrace();
    } catch (MqttException e) {
      System.err.println("Fehler aufgetaucht: " + e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}