package src.main.java;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class TCPSocketServer extends Thread {

  private ServerSocket serverSocket = null;

  public TCPSocketServer() {
    try {
      this.serverSocket = new ServerSocket(DataManager.getInstance().getTcpPort());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void run() {
    while (true) {
      Socket socket = null;
      try {
		//Öffnen eines Verbindungssockets, wenn der client sich mit dem server socket verbindet
        socket = this.serverSocket.accept();
        InputStream inputStream = socket.getInputStream();
        String response = TCPSocketServerService
            .processRequest(inputStream, DataManager.getInstance().getData());
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(response.getBytes());
      } catch (IOException e) {
        System.err.println(e.getMessage());
      } finally {
        if (socket != null) {
          try {
            socket.close();
          } catch (IOException e) {
          }
        }
      }
    }
  }

}