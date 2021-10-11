package src.main.java.mqtt;

import static src.main.java.ComponentInfo.deserialize;

import java.util.ArrayList;
import java.io.IOException;


import src.main.java.thrift.Status;
import src.main.java.thrift.StatusService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;
import src.main.java.thrift.ComponentInfoThrift;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;


import src.main.java.ComponentInfo;
import src.main.java.DataManager;



public class SensorCallback implements MqttCallback {

  private ArrayList<ComponentInfo> data;

  public SensorCallback(ArrayList<ComponentInfo> data) {
    this.data = data;
  }

  @Override
  public void connectionLost(Throwable throwable) {
    System.err.println("Verbindung zu MQTT broker verloren!");
  }

  @Override
  public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
    System.out.println("Nachricht erhalten: " + s);
    saveComponentInfo(mqttMessage.getPayload());
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken mqttDeliveryToken) {
    try {
      System.out.println("Auslieferung komplett: " + mqttDeliveryToken.getMessage());
    } catch (MqttException e) {
      System.err.println("Fehler beim senden vom ausliefern des tokens: " + e.getMessage());
    }
  }

  private void printStatus() {
    int produced = 0;
    int consumed = 0;
    for (int i = 0; i < data.size(); i++) {
      if (data.get(i).getIsConsumer()) {
        consumed += data.get(i).getKW();
      } else {
        produced += data.get(i).getKW();
      }
    }
    System.out.println(
        "Erzeugt: " + produced + "kW - Verbraucht: " + consumed + "kW - Ergebnis: "
            + (produced - consumed) + "kW"
    );
  }

  private void saveComponentInfo(byte[] componentInfoBytes) {
    try {
      ComponentInfo info = deserialize(componentInfoBytes);

      // ordne von alt zu neu (index 0)
      int index = data.size();
      if (index < 0) {
        index = 0;
      }
      while (index > 0 && data.get(index - 1).getTimestamp().isAfter(info.getTimestamp())) {
        index--;
      }

      data.add(index, info);
      sendToSlaves(info, index);
      printStatus();
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (ClassNotFoundException e) {
      System.err.println(e.getMessage());
    }
  }

  private void sendToSlaves(ComponentInfo info, int index) {
    for (String central : DataManager.getInstance().getSlaves()) {
      try (TTransport transport = new TSocket(central, DataManager.getInstance().getThriftPort())) {
        transport.open();
        TFramedTransport ft = new TFramedTransport(transport);
        TProtocol protocol = new TBinaryProtocol(ft);
        StatusService.Client client = new StatusService.Client(protocol);
        ComponentInfoThrift componentInfoThrift = new ComponentInfoThrift(info.getId(),
            info.getName(), info.getKW(), info.getIsConsumer(), info.getTimestamp().toString());
        client.writeComponentInfo(componentInfoThrift, index, DataManager.getInstance().getName());
        transport.close();
      } catch (TException x) {
        // Ignore
      }
    }
  }

}
