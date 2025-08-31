package ru.netology;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(9999);

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: 7\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    "Hello!\n";
            responseStream.write(response.getBytes());
            responseStream.flush();
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> {
            String body = request.getBody() == null ? "" : request.getBody();
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: " + body.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    body;
            responseStream.write(response.getBytes());
            responseStream.flush();
        });

        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

