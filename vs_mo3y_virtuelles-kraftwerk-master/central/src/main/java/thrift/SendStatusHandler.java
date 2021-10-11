package src.main.java.thrift;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.ArrayList;


import src.main.java.ComponentInfo;
import src.main.java.DataManager;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;


public class SendStatusHandler implements StatusService.Iface {

  public SendStatusHandler() {
  }

  @Override
  public Status getStatus() throws TException {
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
    Status status = new Status(produced, consumed);
    return status;
  }

  @Override
  public void writeComponentInfo(ComponentInfoThrift info, int index, String centralName)
      throws TException {
    ComponentInfo componentInfo = new ComponentInfo(info.getId(), info.getName(), info.isConsumer,
        info.getKw(), LocalDateTime.parse(info.getTimestamp()));

    ArrayList<ComponentInfo> slaveData = DataManager.getInstance().getSlaveData().get(centralName);
    if (index > slaveData.size()) {
      for (int i = slaveData.size(); i < index; i++) {
        //fehlende Dateneingabe von einer anderen zentrale
        slaveData.add(i, getSingleComponentInfoFromCentral(i, centralName));
      }
    }
    slaveData.add(index, componentInfo);
  }

  @Override
  public ComponentInfoThrift getSingleComponentInfo(int index, String centralName)
      throws TException {
    ArrayList<ComponentInfo> dataToGetComponentInfoFrom;
    if (centralName.equals(DataManager.getInstance().getName())) {
      dataToGetComponentInfoFrom = DataManager.getInstance().getData();
    } else {
      dataToGetComponentInfoFrom = DataManager.getInstance().getSlaveData().get(centralName);
    }

    if (index >= dataToGetComponentInfoFrom.size()) {
      return null;
    }

    // parsen von daten mit thrift
    ComponentInfo info = dataToGetComponentInfoFrom.get(index);
    ComponentInfoThrift componentInfoThrift = new ComponentInfoThrift(info.getId(),
        info.getName(),
        info.getKW(), info.getIsConsumer(), info.getTimestamp().toString());
    return componentInfoThrift;
  }

  @Override
  public HistoryPage getCompleteHistory(int page, int entriesPerPage) throws TException {
    ArrayList<ComponentInfo> data = DataManager.getInstance().getData();
    if (page < 1) {
      page = 1;
    }
    ArrayList<ComponentInfoThrift> history = new ArrayList<ComponentInfoThrift>();
    int startIndex = (page - 1) * entriesPerPage;
    int endIndex = page * entriesPerPage - 1;
    if (endIndex > data.size()) {
      endIndex = data.size() - 1;
    }
    // parsen von daten mit thrift
    for (int i = startIndex; i <= endIndex; i++) {
      ComponentInfo info = data.get(i);
      history.add(new ComponentInfoThrift(info.getId(), info.getName(),
          info.getKW(), info.getIsConsumer(), info.getTimestamp().toString()));
    }

    int numberOfPages = (int) Math.ceil(data.size() / (double) entriesPerPage);
    HistoryPage historyPage = new HistoryPage(numberOfPages, history);
    return historyPage;
  }

  @Override
  public HistoryPage getCompleteHistoryOfSystem(String centralName, int page, int entriesPerPage)
      throws TException {
    Map<String, ArrayList<ComponentInfo>> slaveData = DataManager.getInstance().getSlaveData();
    if (centralName.equals(DataManager.getInstance().getName())) {
      return getCompleteHistory(page, entriesPerPage);
    }
    ArrayList<ComponentInfo> dataOfSystem = slaveData.get(centralName);
    ArrayList<ComponentInfoThrift> history = new ArrayList<ComponentInfoThrift>();
    int startIndex = (page - 1) * entriesPerPage;
    int endIndex = page * entriesPerPage - 1;
    if (endIndex > dataOfSystem.size()) {
      endIndex = dataOfSystem.size() - 1;
    }
    // parsen von daten mit thrift
    for (int i = startIndex; i <= endIndex; i++) {
      ComponentInfo info = dataOfSystem.get(i);
      history.add(new ComponentInfoThrift(info.getId(), info.getName(),
          info.getKW(), info.getIsConsumer(), info.getTimestamp().toString()));
    }
    int numberOfPages = (int) Math.ceil(dataOfSystem.size() / (double) entriesPerPage);
    HistoryPage historyPage = new HistoryPage(numberOfPages, history);
    return historyPage;
  }

  private ComponentInfo getSingleComponentInfoFromCentral(int index, String centralName) {
    for (String central : DataManager.getInstance().getSlaves()) {
      try (TTransport transport = new TSocket(central, DataManager.getInstance().getThriftPort())) {
        transport.open();
        TFramedTransport ft = new TFramedTransport(transport);
        TProtocol protocol = new TBinaryProtocol(ft);
        StatusService.Client client = new StatusService.Client(protocol);
        ComponentInfoThrift componentInfoThrift = client.getSingleComponentInfo(index, centralName);
        transport.close();
        ComponentInfo componentInfo = new ComponentInfo(componentInfoThrift.id,
            componentInfoThrift.name,
            componentInfoThrift.isConsumer, componentInfoThrift.kw,
            LocalDateTime.parse(componentInfoThrift.timestamp));
        if (componentInfo == null) {
          continue;
        }
        return componentInfo;
      } catch (TException x) {
      }
    }
    return null;
  }

}
