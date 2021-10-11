package src.main.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class UDPSocketClient {

  private InetAddress address;
  private int port;
  private int bufferSize;
  private static int packageId = 0;
  private static final int MIN_NUMBER_OF_INFO_BYTES = 4; // number of packages
  private static final int NUMBER_OF_INFO_BYTES = 12; // number of packages, index, id

  public UDPSocketClient(String destination, int port, int bufferSize) {
    this.port = port;
    this.bufferSize = bufferSize;
    // Try to set the destination host address.
    try {
      address = InetAddress.getByName(destination);
    } catch (UnknownHostException e) {
      System.err.println("Can not parse the destination host address.\n" + e.getMessage());
      System.exit(1);
    }
  }

  private static int getPackageId() {
    packageId++;
    return packageId;
  }

  public void sendComponentInfo(ComponentInfo info) {
    try {
      byte[] serializedInfo = info.serialize();

      if (serializedInfo.length > this.bufferSize - 4) {
        // information needs to be split in multiple packages
        int segmentSize = bufferSize - NUMBER_OF_INFO_BYTES;
        int numberOfPackages = (int) serializedInfo.length / segmentSize;
        if (serializedInfo.length % segmentSize > 0) {
          numberOfPackages++;
        }
        int packageId = getPackageId(); // Identifier for client to match packets
        ByteBuffer bb = ByteBuffer.wrap(serializedInfo);
        for (int i = 0; i < numberOfPackages - 1; i++) {
          byte[] infoSegment = new byte[segmentSize];
          bb.get(infoSegment, 0, segmentSize);
          sendInfoSegment(infoSegment, numberOfPackages, i, packageId);
        }
        byte[] infoSegment = new byte[bb.remaining()];
        bb.get(infoSegment, 0, infoSegment.length);
        sendInfoSegment(infoSegment, numberOfPackages, numberOfPackages - 1, packageId);
        return;
      }

      // all information can be send in one package
      int numberOfPackages = 1;
      // create byte array beginning with numberOfPackages(4bytes) followed by serializedInfo
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      outputStream.write(ByteBuffer.allocate(4).putInt(numberOfPackages).array());
      outputStream.write(serializedInfo);
      byte[] buf = outputStream.toByteArray();
      // send data
      sendPackage(buf);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void sendInfoSegment(byte[] infoSegment, int numberOfPackages, int index, int id) {
    System.out.println(
        "sendInfoSegment - id:" + id + " nmb: " + numberOfPackages + " index: " + index);
    // create byte array beginning with numberOfPackages(4bytes) followed by serializedInfo
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      outputStream.write(ByteBuffer.allocate(4).putInt(numberOfPackages).array());
      outputStream.write(ByteBuffer.allocate(4).putInt(index).array());
      outputStream.write(ByteBuffer.allocate(4).putInt(id).array());
      outputStream.write(infoSegment);
      byte[] buf = outputStream.toByteArray();
      // send data
      sendPackage(buf);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void sendPackage(byte[] buf) {
    try (DatagramSocket udpSocket = new DatagramSocket()) {
      // Create a new UDP packet with a byte-array as payload.
      DatagramPacket packet = new DatagramPacket(
          buf,
          buf.length,
          address,
          this.port
      );
      udpSocket.send(packet);
    } catch (SocketException e) {
      System.err.println("Could not start the UDP socket server.\n" + e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
