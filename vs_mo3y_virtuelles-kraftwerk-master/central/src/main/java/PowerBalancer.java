package src.main.java;

import java.util.ArrayList;

import src.main.java.DataManager;
import src.main.java.thrift.ComponentController;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;


public class PowerBalancer extends Thread {

  public PowerBalancer() {

  }

  public void run() {
    ArrayList<ComponentInfo> data = DataManager.getInstance().getData();
    String[] producers = DataManager.getInstance().getProducer();
    while (true) {
      if (getTotal() <= 15000) {

        for (int i = 0; i < producers.length; i++) {
          setPower(producers[i], 0.76);
        }
      } else if (getTotal() > 15000 && getTotal() <= 20000) {
        for (int i = 0; i < producers.length; i++) {
          setPower(producers[i], 0.4);
        }
      } else if (getTotal() > 20000 && getTotal() <= 25000) {
        for (int i = 0; i < data.size(); i++) {
          if (!data.get(i).getIsConsumer()) {
            setPower(data.get(i).getName(), 0.33);
          }
        }
      } else if (getTotal() >= 25000 && getTotal() < 30000) {
        for (int i = 0; i < data.size(); i++) {
          if (!data.get(i).getIsConsumer()) {
            setPower(data.get(i).getName(), 0.25);
          }
        }
      } else if (getTotal() >= 30000 && getTotal() < 35000) {
        for (int i = 0; i < data.size(); i++) {
          if (!data.get(i).getIsConsumer()) {
            setPower(data.get(i).getName(), 0.2);
          }
        }
      } else if (getTotal() >= 35000 && getTotal() < 45000) {
        for (int i = 0; i < data.size(); i++) {
          if (!data.get(i).getIsConsumer()) {
            setPower(data.get(i).getName(), 0.16);
          }
        }
      } else if (getTotal() >= 45000 && getTotal() < 50000) {
        for (int i = 0; i < data.size(); i++) {
          if (!data.get(i).getIsConsumer()) {
            setPower(data.get(i).getName(), 0.15);
          }
        }
      } else {
        for (int i = 0; i < data.size(); i++) {
          if (!data.get(i).getIsConsumer()) {
            setPower(data.get(i).getName(), 0.11);
          }
        }
      }
    }
  }

  private int getTotal() {
    ArrayList<ComponentInfo> data = DataManager.getInstance().getData();
    int produced = 0;
    int consumed = 0;
    for (int i = 0; i < data.size(); i++) {
      if (data.get(i).getIsConsumer()) {
        consumed += data.get(i).getKW();
      } else {
        produced += data.get(i).getKW();
      }
    }
    return produced - consumed;
  }

  private void setPower(String host, double power) {
    try (TTransport transport = new TSocket(host, DataManager.getInstance().getThriftPort())) {
      transport.open();
      TFramedTransport ft = new TFramedTransport(transport);
      TProtocol protocol = new TBinaryProtocol(ft);
      ComponentController.Client client = new ComponentController.Client(protocol);
      boolean result = client.setPower(power);
      transport.close();
      if (!result) {
        System.err.println("ERROR: Power wurde nicht verändert!");
      }
    } catch (TException x) {
    }
  }

}
