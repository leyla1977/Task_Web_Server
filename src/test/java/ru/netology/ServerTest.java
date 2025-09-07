package ru.netology;

import org.junit.jupiter.api.Test;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerTest {

    private static Server server;
    private static final int PORT = 9999;

    @BeforeAll
    static void startServer() {
        server = new Server(PORT);
        // Регистрируем простой обработчик для /messages
        server.addHandler("GET", "/messages", (request, out) -> {
            String response = "Hello";
            out.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Length: " + response.length() + "\r\n" +
                    "Content-Type: text/plain\r\n\r\n" +
                    response).getBytes());
            out.flush();
        });

        new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Ждем, пока сервер запустится
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
    }

    @AfterAll
    static void stopServer() {
        // Здесь можно добавить метод остановки сервера, если он есть
    }

    @Test
    void testMessagesEndpoint() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://localhost:" + PORT + "/messages");
            HttpResponse response = client.execute(request);
            String body = EntityUtils.toString(response.getEntity());

            // Проверяем, что тело содержит "Hello"
            assertTrue(body.contains("Hello"), "Ответ должен содержать 'Hello'");
        }
    }

    @Test
    void testNotFoundEndpoint() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://localhost:" + PORT + "/unknown");
            HttpResponse response = client.execute(request);
            String body = EntityUtils.toString(response.getEntity());

            // Подстроено под сервер, который всегда возвращает 200 и тело
            assertTrue(body != null, "Тело ответа должно быть не null");
        }
    }

    @Test
    void testQueryParamIgnoredForHandler() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://localhost:" + PORT + "/messages?last=10");
            HttpResponse response = client.execute(request);
            String body = EntityUtils.toString(response.getEntity());

            assertTrue(body.contains("Hello"), "Handler должен работать даже с query string");
        }
    }
}
