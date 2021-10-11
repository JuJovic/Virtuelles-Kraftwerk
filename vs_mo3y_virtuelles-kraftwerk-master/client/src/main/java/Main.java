package src.main.java;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;
import src.main.java.thrift.ComponentController;
import src.main.java.thrift.ComponentInfoThrift;
import src.main.java.thrift.HistoryPage;
import src.main.java.thrift.Status;
import src.main.java.thrift.StatusService;

import org.eclipse.paho.client.mqttv3.MqttException;
import src.main.java.mqtt.Subscriber;
import src.main.java.mqtt.Publisher;



public class Main {

  private static final int HISTORY_ENTRIES_PER_PAGE = 100;
  private static Subscriber subscriber;

  public static void main(String[] args) {
    int thriftPort = Integer.parseInt(System.getenv("THRIFT_PORT"));
    String thriftHost = System.getenv("THRIFT_HOST");

    // Using Scanner for Getting Input from User
    Scanner in = new Scanner(System.in);

    loop:
    while (true) {
      String command = in.nextLine();
	  System.out.println("*****Die Befehle fuer unser docker attach client*****");
	  System.out.println("*****status -> um den Status der kW zu erhalten*****");
	  System.out.println("*****history 1 -> Zeige Zentrale 1 und Verbraucher / Erzeuger*****");
	  System.out.println("*****history 2 -> Zeige Zentrale 2 und Verbraucher / Erzeuger*****");
	  System.out.println("*****history 3 -> Zeige Zentrale 3 und Verbraucher / Erzeuger*****");
	  System.out.println("*****test -> Testet RPC*****");
	  System.out.println("*****test mqtt -> Testet MQTT*****");
      switch (command) {
        case "status":
          getStatus(thriftHost, thriftPort);
          break;
        case "history 1":
          getHistory(getNextCentral(), "central1", thriftPort);
          break;
        case "history 2":
          getHistory(getNextCentral(), "central2", thriftPort);
          break;
        case "history 3":
          getHistory(getNextCentral(), "central3", thriftPort);
          break;
        case "test":
          getHistoryPerformanceTest(thriftHost, thriftPort, 100000);
          break;
        case "test mqtt":
          testMqtt();
          break;
        case "shutdown":
          shutDownFabric();
          break;
        case "subscribe":
          if (subscriber == null) {
            subscribe();
          } else {
            System.out.println("Subscription laeuft bereits");
          }
          break;
        case "stop":
          break loop;
        default:
          System.out.println("Ungueltige eingabe: " + command);
      }
    }
    in.close();
  }

  private static void subscribe() {
    int mqttPort = Integer.parseInt(System.getenv("MQTT_PORT"));
    subscriber = new Subscriber(mqttPort);
    subscriber.start();
  }

  private static void getStatus(String host, int port) {
    try (TTransport transport = new TSocket(host, port)) {
      transport.open();
      TFramedTransport ft = new TFramedTransport(transport);
      TProtocol protocol = new TBinaryProtocol(ft);
      StatusService.Client client = new StatusService.Client(protocol);
      Status status = client.getStatus();
      transport.close();
      System.out.println("Verbraucht: " + status.consumed + " Erzeugt: " + status.produced);
    } catch (TException x) {
      x.printStackTrace();
    }
  }

  private static int centralIndex = 0;

  private static String getNextCentral() {
    String[] centrals = {"central1", "central2", "central3"};
    String central = centrals[centralIndex];
    centralIndex = (centralIndex + 1) % 3;
    return central;
  }

  private static void getHistory(String host, String centralName, int port) {
    try (TTransport transport = new TSocket(host, port)) {
      transport.open();
      TFramedTransport ft = new TFramedTransport(transport);
      TProtocol protocol = new TBinaryProtocol(ft);
      StatusService.Client client = new StatusService.Client(protocol);
      long startTime = System.currentTimeMillis();
      ArrayList<ComponentInfoThrift> history = new ArrayList<ComponentInfoThrift>();
      int page = 1;
      HistoryPage historyPage;
      do {
        historyPage = client.getCompleteHistoryOfSystem(centralName, page, HISTORY_ENTRIES_PER_PAGE);
        history.addAll(historyPage.data);
        page++;
      } while (historyPage.numberOfPages >= page);
      long endTime = System.currentTimeMillis();
      transport.close();
      System.out.println("Daten von " + centralName + " aus " + host + ": ");
      for (int i = 0; i < history.size(); i++) {
        LocalDateTime timestamp = LocalDateTime.parse(history.get(i).timestamp);
        ComponentInfo info = new ComponentInfo(history.get(i).id, history.get(i).name,
        history.get(i).isConsumer, history.get(i).kw, timestamp);
        System.out.println(info);
      }
      System.out.println("Erhalten " + centralName + "'history mit "
          + history.size() + " eintraegen " + (endTime - startTime) + "ms aus" + host);
    } catch (TException x) {
      getHistory(getNextCentral(), centralName, port);
    }
  }

  private static long startTime = 0;

  private static void getHistoryPerformanceTest(String host, int port, int numberOfCalls) {
    ExecutorService es = Executors.newCachedThreadPool();

    startTime = System.nanoTime();
    for (int i = 0; i < numberOfCalls; i++) {
      es.execute(
          new Runnable() {
            public void run() {
              getTimeForDataRequest(getNextCentral(), "central1", port, numberOfCalls);
            }
          }
      );
    }
    es.shutdown();
    try {
      es.awaitTermination(1, TimeUnit.HOURS);
      long endTime = System.nanoTime();
      long totalTime = endTime - startTime;
      double totalTimeMs = totalTime / 1_000_000.0;
      System.out.println("Gesamtzeit: " + totalTimeMs + "ms von " + numberOfCalls + " Anfragen");
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static void getTimeForDataRequest(String host, String centralName, int port,
      int numberOfCalls) {
    try (TTransport transport = new TSocket(host, port)) {
      transport.open();
      TFramedTransport ft = new TFramedTransport(transport);
      TProtocol protocol = new TBinaryProtocol(ft);
      StatusService.Client client = new StatusService.Client(protocol);
      HistoryPage historyPage = client.getCompleteHistory(1, HISTORY_ENTRIES_PER_PAGE);
      transport.close();
    } catch (TException x) {
      getTimeForDataRequest(getNextCentral(), centralName, port, numberOfCalls);
    }
  }

  private static void shutDownFabric() {
    try (TTransport transport = new TSocket("Coal-Power-Plant", 9090)) {
      transport.open();
      TFramedTransport ft = new TFramedTransport(transport);
      TProtocol protocol = new TBinaryProtocol(ft);
      ComponentController.Client client = new ComponentController.Client(protocol);
      boolean result = client.setPower(0);
      transport.close();
      if (result) {
        System.out.println("Erfolgreich heruntergefahren!");
      } else {
        System.err.println("ERROR: keine Veraenderung!");
      }
    } catch (TException x) {
    }
  }

  private static void testMqtt() {
    int port = Integer.parseInt(System.getenv("MQTT_PORT"));
    Publisher publisher = new Publisher(port, "power/component/client/info");
    String someText = "Das erwartet die Zentrale nicht!";
    byte[] malFormattedMessage = someText.getBytes(StandardCharsets.UTF_8);
    publisher.sendInfo(malFormattedMessage);
    try {
      publisher.disconnect();
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

}
