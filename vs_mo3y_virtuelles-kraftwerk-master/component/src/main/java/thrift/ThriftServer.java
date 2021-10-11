package src.main.java.thrift;

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.layered.TFramedTransport;

import src.main.java.components.Erzeuger;

public class ThriftServer extends Thread {

  private int port;
  private Erzeuger erzeuger;

  public ThriftServer(int port, Erzeuger erzeuger) {
    this.port = port;
    this.erzeuger = erzeuger;
  }

  @Override
  public void run() {
    ComponentController.Processor<ControlHandler> processor = new ComponentController.Processor(
        new ControlHandler(this.erzeuger));
    try {
      TNonblockingServerSocket socket = new TNonblockingServerSocket(this.port);
      TNonblockingServer.Args arg = new TNonblockingServer.Args(socket);
      arg.protocolFactory(new TBinaryProtocol.Factory());
      arg.transportFactory(new TFramedTransport.Factory());
      arg.processorFactory(new TProcessorFactory(processor));
      TServer server = new TNonblockingServer(arg);
      System.out.println("*****Starten des thrift servers*****");
      server.serve();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
