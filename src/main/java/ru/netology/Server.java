package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final int port;
    private final ConcurrentHashMap<String, Handler> handlers = new ConcurrentHashMap<>();

    public Server(int port) {
        this.port = port;
    }

    // Метод регистрации обработчиков
    public void addHandler(String method, String path, Handler handler) {
        String key = method + " " + path;
        handlers.put(key, handler);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleConnection(socket)).start();
            }
        }
    }

    private void handleConnection(Socket socket) {
        try (
                socket;
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            // Создаём объект Request
            Request request;
            try {
                request = new Request(in);
            } catch (IOException e) {
                return; // плохой запрос
            }

            // Используем только путь без Query String для поиска хендлера
            String pathOnly = request.getPath().split("\\?")[0];
            String key = request.getMethod() + " " + pathOnly;
            Handler handler = handlers.get(key);

            if (handler != null) {
                // Хендлер может работать с GET-параметрами и POST-параметрами
                handler.handle(request, out);
            } else {
                // 404 Not Found
                out.write(("HTTP/1.1 404 Not Found\r\n\r\nСтраница не найдена").getBytes());
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
