package src.main.java;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import src.main.java.thrift.ComponentInfoThrift;
import src.main.java.thrift.HistoryPage;
import src.main.java.thrift.StatusService;

public class DataManager {

  private static DataManager instance;
  private ArrayList<ComponentInfo> data;
  private Map<String, ArrayList<ComponentInfo>> slaveData;
  private String name, mqttTopic;
  private int mqttPort, tcpPort, thriftPort;
  private String[] slaves;
  private String[] producer;

  public static DataManager getInstance() {
    if (instance == null) {
      instance = new DataManager();
    }
    return instance;
  }

  private DataManager() {
    this.mqttPort = Integer.parseInt(System.getenv("MQTT_PORT"));
    this.tcpPort = Integer.parseInt(System.getenv("TCP_PORT"));
    this.name = System.getenv("NAME");
    this.thriftPort = Integer.parseInt(System.getenv("THRIFT_PORT"));
    this.producer = System.getenv("PRODUCER").split(";");
    this.slaves = System.getenv("SLAVES").split(";");
    this.mqttTopic = System.getenv("MQTT_TOPIC");

    // daten von Zentrale
    this.data = new ArrayList<ComponentInfo>();
    restoreDataFromCentral(this.name, this.data);
	//daten von anderen Zentralen welche diese als sklave(slave) dient
    this.slaveData = new HashMap<String, ArrayList<ComponentInfo>>();
    for (String slave : this.slaves) {
      slaveData.put(slave, new ArrayList<ComponentInfo>());
      restoreDataFromCentral(slave, slaveData.get(slave));
    }
  }

  public ArrayList<ComponentInfo> getData() {
    return data;
  }

  public Map<String, ArrayList<ComponentInfo>> getSlaveData() {
    return slaveData;
  }

  public String getName() {
    return name;
  }

  public int getMqttPort() {
    return mqttPort;
  }

  public int getTcpPort() {
    return tcpPort;
  }

  public int getThriftPort() {
    return thriftPort;
  }

  public String[] getSlaves() {
    return slaves;
  }

  public String[] getProducer() {
    return producer;
  }

  private void restoreDataFromCentral(String centralName, ArrayList<ComponentInfo> data) {
    for (String host : slaves) {
      try (TTransport transport = new TSocket(host, this.thriftPort)) {
        transport.open();
        TFramedTransport ft = new TFramedTransport(transport);
        TProtocol protocol = new TBinaryProtocol(ft);
        StatusService.Client client = new StatusService.Client(protocol);
        ArrayList<ComponentInfoThrift> history = new ArrayList<ComponentInfoThrift>();
        int page = 1;
        HistoryPage historyPage;
        do {
          historyPage = client.getCompleteHistoryOfSystem(centralName, page, 100);
          history.addAll(historyPage.data);
          page++;
        } while (historyPage.numberOfPages >= page);
        transport.close();

        for (int i = 0; i < history.size(); i++) {
          LocalDateTime timestamp = LocalDateTime.parse(history.get(i).timestamp);
          ComponentInfo info = new ComponentInfo(history.get(i).id, history.get(i).name,
              history.get(i).isConsumer, history.get(i).kw, timestamp);
		  //componenten info an der korrekten position wiederherstellen
          data.add(i, info);
        }
        // daten wurden wiederhergestellt
        return;
      } catch (TException x) {
      }
    }
    return;
  }

  public String getMqttTopic() {
    return mqttTopic;
  }

  public void setMqttTopic(String mqttTopic) {
    this.mqttTopic = mqttTopic;
  }
}
