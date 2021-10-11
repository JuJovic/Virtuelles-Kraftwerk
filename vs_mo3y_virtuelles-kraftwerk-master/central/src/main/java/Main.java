package src.main.java;

import src.main.java.mqtt.Subscriber;
import src.main.java.thrift.ThriftServer;

public class Main {

  public static void main(String[] args) {
    // Erstellen des MQTT Subscriber
    Subscriber subscriber = new Subscriber();
    subscriber.start();

    // Erstellen des TCP socket server
    TCPSocketServer tcpSocketServer = new TCPSocketServer();
	//Starten des TCP socket server als thread
    tcpSocketServer.start();

    // Erstellen des Thrift Server
    ThriftServer thriftServer = new ThriftServer();
    thriftServer.start();

    // Starten des powerbalancer
    PowerBalancer powerBalancer = new PowerBalancer();
    powerBalancer.start();
  }

}
