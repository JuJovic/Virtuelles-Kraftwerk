package src.main.java;

import src.main.java.components.Component;
import src.main.java.components.Erzeuger;

import src.main.java.components.Erzeuger_Kohlewerk;
import src.main.java.components.Erzeuger_Solarwerk;
import src.main.java.components.Erzeuger_Windkraftwerk;

import src.main.java.components.Verbraucher_Fabrik;
import src.main.java.components.Verbraucher_Haushalt;
import src.main.java.thrift.ThriftServer;

public class Main {

  public static void main(String[] args) {
    int ID = Integer.parseInt(System.getenv("ID"));
    String name = System.getenv("NAME");
    Component component;
    switch (System.getenv("TYPE")) {
      case "CONSUMER_FACTORY":
        component = new Verbraucher_Fabrik(ID, name);
        break;
      case "CONSUMER_HOUSEHOLD":
        component = new Verbraucher_Haushalt(ID, name);
        break;
      case "PROD_WIND":
        Erzeuger producer_wind = new Erzeuger_Windkraftwerk(ID, name,
            Integer.parseInt(System.getenv("MAX_CAPACITY")));
        startThriftServer(producer_wind);
        component = producer_wind;
        break;
      case "PROD_SUN":
        Erzeuger producer_sun = new Erzeuger_Solarwerk(ID, name,
            Integer.parseInt(System.getenv("MAX_CAPACITY")));
        startThriftServer(producer_sun);
        component = producer_sun;
        break;
      case "PROD_COAL":
        Erzeuger producer_coal = new Erzeuger_Kohlewerk(ID, name,
            Integer.parseInt(System.getenv("MAX_CAPACITY")));
        startThriftServer(producer_coal);
        component = producer_coal;
        break;
      default:
        System.exit(1);
        return;
    }

    String destination = System.getenv("DESTINATION");
    int port = Integer.parseInt(System.getenv("MQTT_PORT"));
    int bufferSize = Integer.parseInt(System.getenv("BUFFER_SIZE"));
    component.start(destination, port, bufferSize);
  }

  private static void startThriftServer(Erzeuger producer) {
    // Create Thrift Server
    int thriftPort = Integer.parseInt(System.getenv("THRIFT_PORT"));
    ThriftServer thriftServer = new ThriftServer(thriftPort, producer);
    thriftServer.start();
  }
}
