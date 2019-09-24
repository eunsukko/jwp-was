package webserver;

import http.request.HttpRequest;
import http.response.DataOutputStreamWrapper;
import http.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.controller.Controller;
import webserver.router.RouterFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest httpRequest = HttpRequest.of(in);
            DataOutputStreamWrapper dos = new DataOutputStreamWrapper(new DataOutputStream(out));
            HttpResponse httpResponse = HttpResponse.of(dos);

            Controller controller = route(httpRequest);
            controller.service(httpRequest, httpResponse);
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private Controller route(HttpRequest httpRequest) {
        String path = httpRequest.getPath();

        return RouterFactory.getRouter().retrieveController(path);
    }
}
