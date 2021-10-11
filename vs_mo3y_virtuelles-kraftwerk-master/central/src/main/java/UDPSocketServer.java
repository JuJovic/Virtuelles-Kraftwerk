package src.main.java;

import static src.main.java.ComponentInfo.deserialize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class UDPSocketServer extends Thread {

  private byte[] buf;
  private ArrayList<ComponentInfo> data;
  private int port;
  private HashMap<String, HashMap<Integer, byte[][]>> segments;
  private static final int MIN_NUMBER_OF_INFO_BYTES = 4; // number of packages
  private static final int NUMBER_OF_INFO_BYTES = 12; // number of packages, index, id

  public UDPSocketServer(int port, int bufferSize, ArrayList<ComponentInfo> data) {
    this.port = port;
    this.buf = new byte[bufferSize];
    this.data = data;
    this.segments = new HashMap<>();
  }

  public void run() {
    DatagramSocket udpSocket = null;
    try {
      udpSocket = new DatagramSocket(this.port);
      System.out.println(
          "UDP socket server started at port " + this.port + " with buffer size " + buf.length);
      // Receive packets continuously.
      while (true) {
        // Create the datagram packet structure that contains the received datagram information.
        DatagramPacket udpPacket = new DatagramPacket(buf, buf.length);
        udpSocket.receive(udpPacket);
        processPacketData(udpPacket);
      }
    } catch (SocketException e) {
      System.err.println("Could not start the UDP socket server.\n" + e.getMessage());
    } catch (IOException e) {
      System.err.println("Could not receive packet.\n" + e.getMessage());
    } finally {
      if (udpSocket != null) {
        udpSocket.close();
      }
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
        "Erzeugt: " + produced + " kW | Verbraucht: " + consumed + " kW | Gesamt: "
            + (produced - consumed) + " kW"
    );
  }

  private void processPacketData(DatagramPacket udpPacket) {
    InetAddress address = udpPacket.getAddress();
    int packetLength = udpPacket.getLength();
    byte[] payload = Arrays.copyOfRange(udpPacket.getData(), 0, packetLength);

    if (packetLength <= MIN_NUMBER_OF_INFO_BYTES) {
      System.out.println("Received mal formatted package from: " + address.getHostAddress()
          + "\n Reason: package is smaller than " + MIN_NUMBER_OF_INFO_BYTES + " bytes");
      return;
    }

    ByteBuffer bb = ByteBuffer.wrap(payload);
    int numberOfPackages = bb.getInt(); // get first 4 bytes --> number of packages
    if (numberOfPackages == 1) {
      // this package contains a complete componentInfo
      byte[] componentInfoBytes = new byte[packetLength - 4];
      bb.get(componentInfoBytes);
      saveComponentInfo(componentInfoBytes);
      return;
    }

    if (packetLength <= NUMBER_OF_INFO_BYTES) {
      System.out.println("Received mal formatted package from: " + address.getHostAddress()
          + "\n Reason: number of packages > 1 but package is smaller than " + NUMBER_OF_INFO_BYTES
          + " bytes");
      return;
    }

    // componentInfo was split into multiple packages
    int index = bb.getInt(); // get next 4 bytes --> segment index
    int packageId = bb.getInt(); // get next 4 bytes --> id of the package the segment is part of
    byte[] segment = new byte[packetLength - NUMBER_OF_INFO_BYTES];
    bb.get(segment);  // remaining bytes in buffer are the actual data

    if (numberOfPackages > 5000 || (index > numberOfPackages)) {
      // componentInfo have to be split in <=5000 segments (prevent OutOfMemoryError of heap space) AND have to be index < numberOfPackages
      System.out.println("Received mal formatted package from: " + address.getHostAddress());
      return;
    }
    processInfoSegment(address.getHostAddress(), index, packageId, numberOfPackages, segment);
  }

  private void saveComponentInfo(byte[] componentInfoBytes) {
    try {
      ComponentInfo info = deserialize(componentInfoBytes);

      // Keep order from oldest (index 0) to newest
      int index = data.size();
      if (index < 0) {
        index = 0;
      }
      while (index > 0 && data.get(index - 1).getTimestamp().isAfter(info.getTimestamp())) {
        index--;
      }

      data.add(index, info);
      printStatus();
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (ClassNotFoundException e) {
      System.err.println(e.getMessage());
    }
  }

  // returns if all segments of a componentInfo are complete
  private boolean checkSegments(String source, int id) {
    for (int i = 0; i < segments.get(source).get(id).length; i++) {
      if (segments.get(source).get(id)[i] == null) {
        return false;
      }
    }
    return true;
  }

  private void processInfoSegment(String source, int index, int id, int numberOfSegments,
      byte[] segment) {

    HashMap segmentsFromSource;
    if (segments.containsKey(source)) {
      segmentsFromSource = segments.get(source);
    } else {
      // create map for segments if not existing for source
      segmentsFromSource = new HashMap<>();
      segments.put(source, segmentsFromSource);
    }
    byte[][] infoSegments;
    if (segmentsFromSource.containsKey(id)) {
      infoSegments = segments.get(source).get(id);
    } else {
      // create new byte array if not existing for info with this id
      infoSegments = new byte[numberOfSegments][];
      segmentsFromSource.put(id, infoSegments);
    }
    infoSegments[index] = segment;
    if (checkSegments(source, id)) {
      // all segements complete
      try {
        // concat all segments of this information
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < infoSegments.length; i++) {
          outputStream.write(infoSegments[i]);
        }
        byte[] infoBytes = outputStream.toByteArray();
        // save info
        saveComponentInfo(infoBytes);
        // remove old segments
        segmentsFromSource.remove(id);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
