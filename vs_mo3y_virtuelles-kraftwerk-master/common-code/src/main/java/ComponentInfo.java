package src.main.java;

import java.time.LocalDateTime;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.Serializable;


public class ComponentInfo implements Serializable {

  private static final long serialVersionUID = 12345678;
  private int id;
  private String name;
  private int kW;
  private boolean isConsumer;
  private LocalDateTime timestamp;

  public ComponentInfo(int id, String name, boolean isConsumer, int kW, LocalDateTime timestamp) {
    this.id = id;
    this.name = name;
    this.isConsumer = isConsumer;
    this.kW = kW;
    this.timestamp = timestamp;
  }

  public int getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public boolean getIsConsumer() {
    return this.isConsumer;
  }

  public int getKW() {
    return this.kW;
  }

  public LocalDateTime getTimestamp() {
    return this.timestamp;
  }

  public String toString() {
    if (this.isConsumer) {
      return ("ID: " + this.id + " | Name: " + this.name + " | Verbraucht: " + kW + " kW"
          + " | Timestamp: " + timestamp);
    }
    return ("ID: " + this.id + " | Name: " + this.name + " | Erzeugt: " + kW + " kW"
        + " | Timestamp: " + timestamp);

  }

  public byte[] serialize() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream os = new ObjectOutputStream(out);
    os.writeObject(this);
    return out.toByteArray();
  }

  public static ComponentInfo deserialize(byte[] data)
      throws IOException, ClassNotFoundException {
    ByteArrayInputStream in = new ByteArrayInputStream(data);
    ObjectInputStream is = new ObjectInputStream(in);
    return (ComponentInfo) is.readObject();
  }
}