package ru.netology;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server(9999);

        // Хендлер по адресу /hello
        server.addHandler("GET", "/hello", (request, out) -> {
            String response = "Hello!";
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/plain; charset=UTF-8\r\n" +
                            "\r\n" +
                            response
            ).getBytes());
            out.flush();
        });

        // Хендлер по адресу /messages
        server.addHandler("GET", "/messages", (request, out) -> {
            String last = request.getQueryParam("last");
            String response = "Последних сообщений: " + last;
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/plain; charset=UTF-8\r\n" +
                            "\r\n" +
                            response
            ).getBytes());
            out.flush();
        });

        server.start();
    }
}
