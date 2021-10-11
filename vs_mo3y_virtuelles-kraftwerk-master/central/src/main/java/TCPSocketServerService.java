package src.main.java;

import java.util.ArrayList;
import java.util.HashMap;

import java.time.format.DateTimeFormatter;
import src.main.java.rest.*;

import java.io.IOException;
import java.io.InputStream;



public class TCPSocketServerService {

  private static final String STYLE = "<style>table,th,td{border:1px solid #000;border-collapse:collapse}th,td{padding:5px;text-align:left}</style>";
  private static final String HTML_HEADER = "<html><head>" + STYLE + "<title></title></head><body><h1>Verteilte Systeme_Praktikum_Burchard</h1>";
  private static final String HTML_FOOTER = "</body></html>";
  private static final String TABLE_HEADER = "<table><tr><th>ID</th><th>Name</th><th>Type</th><th>KW</th><th>Timestamp</th></tr>";
  private static final String TABLE_FOOTER = "</table>";
  private static final String BAD_REQUEST = "<p>BAD_REQUEST<p>";
  private static final String CURRENT_STATE = "<h1>Current state: </h1>";
  private static final String HISTORY = "<h1>History: </h1>";
  private static final String BAD_REQUEST_MSG = HTML_HEADER + BAD_REQUEST + HTML_FOOTER;
  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
      .ofPattern("dd.MM.yyyy HH:mm");

  private static final String HOST_AS_KEY = "Host: ";
  private static final String USER_AGENT_AS_KEY = "User-Agent: ";
  private static final String ACCEPT_AS_KEY = "Accept: ";
  private static final String ACCEPT_LANGUAGE_AS_KEY = "Accept-Language: ";
  private static final String UPGRADE_INSECURE_REQUESTS_AS_KEY = "Upgrade-Insecure-Requests: ";
  private static final String CONNECTION_AS_KEY = "Connection: ";
  private static final String COOKIE_AS_KEY = "Cookie: ";
  private static final String ACCEPT_ENCODING_AS_KEY = "Accept-Encoding: ";


  public static String processRequest(InputStream inputStream, ArrayList<ComponentInfo> data)
      throws IOException {

    String request = buildRequest(inputStream);
    String[] requestHeader = request.split("\r\n");
    HttpRequest httpRequest = processHttpRequest(requestHeader);
    if (httpRequest == null) {
      return BAD_REQUEST_MSG;
    }
    String html = "";

    if (httpRequest.getMethod().equals("GET")) {

      if (httpRequest.getUri().equals("/ausgabe")) {
        html = HTML_HEADER + getInformation(data, httpRequest) + HTML_FOOTER;
      } else {

        html = getInfoFromComponent(httpRequest, data);
      }
    } else {
      return BAD_REQUEST_MSG;
    }

    final String CRLF = "\n\r";
    return "HTTP/1.1 200 ok" +
        CRLF + //codestatus
        "Content-Length" +
        html.getBytes().length +
        CRLF +
        CRLF +
        html +
        CRLF +
        CRLF;
  }

  private static String buildRequest(InputStream inputStream) throws IOException {
    String request = "";

    int _byte;
    while ((_byte = inputStream.read()) >= 0) {
      request += ((char) _byte);
      if (request.contains("\r\n\r\n")) {
        break;
      }
    }
    return request;
  }

  private static String getInfoHTMLTableRow(ComponentInfo info) {
    String tableRow = "<tr>";
    tableRow += "<td>" + info.getId() + "</td>";
    tableRow += "<td>" + info.getName() + "</td>";
    if (info.getIsConsumer()) {
      tableRow += "<td>Verbraucher</td>";
    } else {
      tableRow += "<td>Erzeuger</td>";
    }
    tableRow += "<td>" + info.getKW() + "</td>";
    tableRow += "<td>" + info.getTimestamp().format(dateTimeFormatter) + "</td>";
    tableRow += "</tr>";
    return tableRow;
  }

  private static String getInformation(ArrayList<ComponentInfo> data, HttpRequest httpRequest) {
    String info = "";
    int produced = 0;
    int consumed = 0;
    for (int i = data.size() - 1; i >= 0; i--) {
      info += getInfoHTMLTableRow(data.get(i));
      if (data.get(i).getIsConsumer()) {
        consumed += data.get(i).getKW();
      } else {
        produced += data.get(i).getKW();
      }
    }
    info = getUserAgentForBrowser(httpRequest)
        + "<p>"
        + "| Erzeugt: " + produced + " kW "
        + "| Verbraucht: " + consumed + " kW "
        + "| Gesamt: " + (produced - consumed) + " kW"
        + "</p>"
        + TABLE_HEADER
        + info
        + TABLE_FOOTER;
    return info;
  }

  private static ComponentInfo getCurrentStatusOfComponent(int id, ArrayList<ComponentInfo> data) {
    // neue componenten info per id
    for (int i = data.size() - 1; i >= 0; i--) {
      if (data.get(i).getId() == id) {
        return data.get(i);
      }
    }
    return null;
  }

  private static String getInfoFromComponent(HttpRequest httpRequest,
      ArrayList<ComponentInfo> data) {

    String html = "";

    String id = "";
    boolean isHistory = false;
    String[] uriSeparated = httpRequest.getUri().split("/");
    if (uriSeparated.length > 4) {
      return BAD_REQUEST_MSG;
    }

    try {
      if (uriSeparated[1].equals("component")) {
        id = uriSeparated[2];
        if (uriSeparated[3].equals("history")) {
          isHistory = true;
        }
      } else {
        return BAD_REQUEST_MSG;
      }
    } catch (IndexOutOfBoundsException ignored) {
    }

    try {
      int componentInfoId = Integer.parseInt(id);
      if (isHistory) {
        // return complete component history
        String info = "";
        for (int i = data.size() - 1; i >= 0; i--) {
          if (data.get(i).getId() == componentInfoId) {
            info += getInfoHTMLTableRow(data.get(i));
          }
        }
        html = HTML_HEADER
            + getUserAgentForBrowser(httpRequest)
            + HISTORY
            + TABLE_HEADER
            + info
            + TABLE_FOOTER
            + HTML_FOOTER;
        return html;
      }

      // return only current component status
      ComponentInfo componentInfo = getCurrentStatusOfComponent(componentInfoId, data);
      if (componentInfo == null) {
        return BAD_REQUEST_MSG;
      }
      html = HTML_HEADER
          + getUserAgentForBrowser(httpRequest)
          + CURRENT_STATE
          + TABLE_HEADER
          + getInfoHTMLTableRow(componentInfo)
          + TABLE_FOOTER +
          HTML_FOOTER;
      return html;

    } catch (NumberFormatException e) {
      return BAD_REQUEST_MSG;
    }
  }

  public static HttpRequest processHttpRequest(String[] requestHeader) {
    String[] firstLine = requestHeader[0].split(" ");

    HashMap<String, String> map = getMapOfHeaderInfos(requestHeader);

    if (firstLine.length == 3) {
      return new HttpRequest(
          firstLine[0],
          firstLine[1],
          firstLine[2],
          map.get(HOST_AS_KEY),
          map.get(USER_AGENT_AS_KEY),
          map.get(ACCEPT_AS_KEY),
          map.get(ACCEPT_LANGUAGE_AS_KEY),
          map.get(UPGRADE_INSECURE_REQUESTS_AS_KEY),
          map.get(CONNECTION_AS_KEY),
          map.get(COOKIE_AS_KEY),
          map.get(UPGRADE_INSECURE_REQUESTS_AS_KEY)
      );
    }
    return null;
  }

  private static HashMap<String, String> getMapOfHeaderInfos(String[] requestHeader) {
    HashMap<String, String> map = new HashMap<>();
    map.put(HOST_AS_KEY, "");
    map.put(USER_AGENT_AS_KEY, "");
    map.put(ACCEPT_AS_KEY, "");
    map.put(ACCEPT_LANGUAGE_AS_KEY, "");
    map.put(ACCEPT_ENCODING_AS_KEY, "");
    map.put(UPGRADE_INSECURE_REQUESTS_AS_KEY, "");
    map.put(CONNECTION_AS_KEY, "");
    map.put(COOKIE_AS_KEY, "");

    for (int i = 0; i < requestHeader.length; i++) {

      if (requestHeader[i].contains(HOST_AS_KEY)) {
        map.replace(HOST_AS_KEY, requestHeader[i].substring(HOST_AS_KEY.length()));
      }

      if (requestHeader[i].contains(USER_AGENT_AS_KEY)) {
        map.replace(USER_AGENT_AS_KEY, requestHeader[i].substring(USER_AGENT_AS_KEY.length()));
      }

      if (requestHeader[i].contains(ACCEPT_AS_KEY)) {
        map.replace(ACCEPT_AS_KEY, requestHeader[i].substring(ACCEPT_AS_KEY.length()));
      }

      if (requestHeader[i].contains(ACCEPT_LANGUAGE_AS_KEY)) {
        map.replace(ACCEPT_LANGUAGE_AS_KEY,
            requestHeader[i].substring(ACCEPT_LANGUAGE_AS_KEY.length()));
      }

      if (requestHeader[i].contains(ACCEPT_ENCODING_AS_KEY)) {
        map.replace(ACCEPT_ENCODING_AS_KEY,
            requestHeader[i].substring(ACCEPT_ENCODING_AS_KEY.length()));
      }

      if (requestHeader[i].contains(UPGRADE_INSECURE_REQUESTS_AS_KEY)) {
        map.replace(UPGRADE_INSECURE_REQUESTS_AS_KEY,
            requestHeader[i].substring(UPGRADE_INSECURE_REQUESTS_AS_KEY.length()));
      }

      if (requestHeader[i].contains(CONNECTION_AS_KEY)) {
        map.replace(CONNECTION_AS_KEY, requestHeader[i].substring(CONNECTION_AS_KEY.length()));
      }

      if (requestHeader[i].contains(COOKIE_AS_KEY)) {
        map.replace(COOKIE_AS_KEY, requestHeader[i].substring(COOKIE_AS_KEY.length()));
      }
    }
    return map;
  }
  private static String getUserAgentForBrowser(HttpRequest httpRequest) {
	return "<p>Jusufovic 755481, Yilmaz" + "</p>";
  }
}
