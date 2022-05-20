package qc.tutorials.connectors.basic;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * @author Daniel Kąckowski
 * @copyright Blue Media SA
 */
public class SimpleService {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);
        server.createContext("/test", new  MyHttpHandler());
        server.setExecutor(threadPoolExecutor);

        server.start();

    }
}

private class MyHttpHandler implements HttpHandler {
2
    @Override
3
    public void handle(HttpExchange httpExchange) throws IOException {
        4
        String requestParamValue=null;
        5
        if("GET".equals(httpExchange.getRequestMethod())) {
            6
            requestParamValue = handleGetRequest(httpExchange);
            7
        }else if("POST".equals(httpExchange)) {
            8
            requestParamValue = handlePostRequest(httpExchange);
            9
        }
        10
        11
        handleResponse(httpExchange,requestParamValue);
        12
    }
13
        14
    private String handleGetRequest(HttpExchange httpExchange) {
        15
        return httpExchange.
        16
        getRequestURI()
        17
                .toString()
        18
                .split("\\?")[1]
        19
                .split("=")[1];
        20
    }
21
        22
    private void handleResponse(HttpExchange httpExchange, String requestParamValue)  throws  IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html>").
        append("<body>").
        append("<h1>").
        append("Hello ")
                .append(requestParamValue)
                .append("</h1>")
                .append("</body>")
                .append("</html>");

        // encode HTML content
        //String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());

        // this line is a must
        httpExchange.sendResponseHeaders(200, htmlResponse.length());
        outputStream.write(htmlResponse.getBytes());

        outputStream.flush();
        outputStream.close();
    }
}
