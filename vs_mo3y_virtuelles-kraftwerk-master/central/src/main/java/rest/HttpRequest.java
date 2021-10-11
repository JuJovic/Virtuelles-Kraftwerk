package src.main.java.rest;

//setter und getter 

public class HttpRequest {

    String method;
    String uri;
    String protocol;
    String host;
    String userAgent;
    String accept;
    String acceptLanguage;
    String acceptEncoding;
    String connection;
    String cookie;
    String upgradeInsecureRequest;

    public HttpRequest() {
    }

    public HttpRequest(String method, String uri, String protocol, String host, String userAgent, String accept, String acceptLanguage, 
						String acceptEncoding, String connection, String cookie, String upgradeInsecureRequest) {
        this.method = method;
        this.uri = uri;
        this.protocol = protocol;
        this.host = host;
        this.userAgent = userAgent;
        this.accept = accept;
        this.acceptLanguage = acceptLanguage;
        this.acceptEncoding = acceptEncoding;
        this.connection = connection;
        this.cookie = cookie;
        this.upgradeInsecureRequest = upgradeInsecureRequest;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAccept() {
        return accept;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public void setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }

    public String getAcceptEncoding() {
        return acceptEncoding;
    }

    public void setAcceptEncoding(String acceptEncoding) {
        this.acceptEncoding = acceptEncoding;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getUpgradeInsecureRequest() {
        return upgradeInsecureRequest;
    }

    public void setUpgradeInsecureRequest(String upgradeInsecureRequest) {
        this.upgradeInsecureRequest = upgradeInsecureRequest;
    }

}
