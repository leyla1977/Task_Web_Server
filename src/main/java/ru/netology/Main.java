package ru.netology;

import java.io.IOException;
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

        // ---------------- POST (multipart/form-data) ----------------
        server.addHandler("POST", "/upload", (request, out) -> {
            StringBuilder response = new StringBuilder();

            // Берём поле user
            Part userPart = request.getPart("user"); //getPart("fieldName") → первый элемент поля/файла
            if (userPart != null) {
                response.append("User: ").append(userPart.getValue()).append("\n");
            } else {
                response.append("User: (не указан)\n");
            }

            // Берём файлы avatar
            List<Part> avatarParts = request.getParts("avatar"); // getParts("fieldName") → список всех элементов с одинаковым именем.
            if (avatarParts != null && !avatarParts.isEmpty()) {
                for (Part part : avatarParts) {
                    if (part.isFile()) {  // gроверка, файл это или обычное поле
                        response.append("Uploaded file: ").append(part.getFilename())
                                .append(" (").append(part.getContent().length).append(" bytes)\n"); //getContent() → содержимое файла в byte[]
                    } else {
                        response.append("Field: ").append(part.getName())
                                .append(" = ").append(part.getValue()).append("\n");
                    }
                }
            } else {
                response.append("No files uploaded\n");
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





