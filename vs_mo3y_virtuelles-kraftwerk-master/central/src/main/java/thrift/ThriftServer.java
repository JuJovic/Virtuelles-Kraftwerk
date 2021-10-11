package src.main.java.thrift;

import src.main.java.DataManager;

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.layered.TFramedTransport;



public class ThriftServer extends Thread {

  public ThriftServer() {
  }

  public void run() {
    StatusService.Processor<SendStatusHandler> processor = new StatusService.Processor<>(
        new SendStatusHandler());
    try {
      TNonblockingServerSocket socket = new TNonblockingServerSocket(DataManager.getInstance()
          .getThriftPort());
      TNonblockingServer.Args arg = new TNonblockingServer.Args(socket);
      arg.protocolFactory(new TBinaryProtocol.Factory());
      arg.transportFactory(new TFramedTransport.Factory());
      arg.processorFactory(new TProcessorFactory(processor));
      TServer server = new TNonblockingServer(arg);
      System.out.println("***** Starte thrift server *****");
      server.serve();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
