package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private final int port;
    private final Map<String, Handler> handlers = new HashMap<>();

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handle(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handle(Socket socket) {
        try (
                socket;
                InputStream in = socket.getInputStream();
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            // --- Создаём Request ---
            Request request = new Request(socket.getInputStream());




            // --- Находим обработчик ---
            String key = request.getMethod() + " " + request.getPath();
            Handler handler = handlers.get(key);

            if (handler != null) {
                handler.handle(request, out);
            } else {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Type: text/plain; charset=UTF-8\r\n" +
                                "\r\n" +
                                "Страница не найдена"
                ).getBytes(StandardCharsets.UTF_8));
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        String key = method + " " + path;
        handlers.put(key, handler);
    }

    @FunctionalInterface
    public interface Handler {
        void handle(Request request, BufferedOutputStream responseStream) throws IOException;
    }
}
