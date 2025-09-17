package ru.netology;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server(9999);

        // ---------------- GET ----------------
        server.addHandler("GET", "/messages", (request, out) -> {
            String last = request.getParam("last"); // query string
            String response = "Последних сообщений: " + last;
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/plain; charset=UTF-8\r\n" +
                            "\r\n" +
                            response
            ).getBytes());
            out.flush();
        });

        // ---------------- POST (form-urlencoded) ----------------
        server.addHandler("POST", "/messages", (request, out) -> {
            String user = request.getPostParam("user");
            String text = request.getPostParam("text");

            String response = "Пользователь: " + user + ", Текст: " + text;
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/plain; charset=UTF-8\r\n" +
                            "\r\n" +
                            response
            ).getBytes());
            out.flush();
        });

        // ---------------- POST (multipart) ----------------
        server.addHandler("POST", "/upload", (request, out) -> {
            StringBuilder response = new StringBuilder();

            // Если есть поле "user"
            String user = request.getPostParam("user");
            if (user != null) {
                response.append("User: ").append(user).append("\n");
            }

            List<Part> photoParts = request.getParts("photos");
            for (Part part : photoParts) {
                if (part.isFile()) {
                    response.append("Uploaded file: ").append(part.getFilename())
                            .append(" (").append(part.getContent().length).append(" bytes)\n");
                } else {
                    response.append("Field: ").append(part.getName()).append(" = ").append(part.getValue()).append("\n");
                }
            }

            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/plain; charset=UTF-8\r\n\r\n" +
                            response.toString()
            ).getBytes());
            out.flush();

        });


        // ---------------- Запуск ----------------
        server.start();
    }
}




