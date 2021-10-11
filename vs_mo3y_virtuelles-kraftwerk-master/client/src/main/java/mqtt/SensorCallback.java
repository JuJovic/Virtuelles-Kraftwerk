package src.main.java.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import src.main.java.ComponentInfo;

public class SensorCallback implements MqttCallback {

  @Override
  public void connectionLost(Throwable throwable) {
    System.err.println("Verbindung zum MQTT broker verloren!");
  }

  @Override
  public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
    System.out.println("Nachricht erhalten: " + s);
    ComponentInfo info = ComponentInfo.deserialize(mqttMessage.getPayload());
    System.out.println(info.toString());
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken mqttDeliveryToken) {
    try {
      System.out.println("Auslieferung komplett: " + mqttDeliveryToken.getMessage());
    } catch (MqttException e) {
      System.err.println("Fehler beim senden vom ausliefern des tokens: " + e.getMessage());
    }
  }

}
