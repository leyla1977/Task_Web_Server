package ru.netology;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server(9999);

        // Хендлер для GET /messages
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


        // Хендлер для POST /messages
        server.addHandler("POST", "/messages", (request, out) -> {
            String user = request.getPostParam("user");
            String text = request.getPostParam("text");

            String response = "Пользователь: " + user + ", Текст: " + text;
            out.write(("HTTP/1.1 200 OK\r\n\r\n" + response).getBytes());
            out.flush();
        });

        server.start();
    }
}
