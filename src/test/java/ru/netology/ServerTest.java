package ru.netology;

import org.junit.jupiter.api.Test;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import java.nio.charset.StandardCharsets;


class ServerTest {

    private static final int PORT = 9999;
    private static Server server;

    @BeforeAll
    static void startServer() {
        server = new Server(PORT);

        // GET /messages handler
        server.addHandler("GET", "/messages", (request, out) -> {
            String last = request.getQueryParam("last");
            if (last == null) last = "none";
            String response = "GET Last: " + last;
            out.write(("HTTP/1.1 200 OK\r\n\r\n" + response).getBytes());
            out.flush();
        });

        // POST /submit handler
        server.addHandler("POST", "/submit", (request, out) -> {
            String param1 = request.getPostParam("param1");
            String param2 = request.getPostParam("param2");
            String response = "POST param1: " + param1 + ", param2: " + param2;
            out.write(("HTTP/1.1 200 OK\r\n\r\n" + response).getBytes());
            out.flush();
        });

        new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Test
    void testGetWithQueryParam() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet("http://localhost:" + PORT + "/messages?last=10");
            try (CloseableHttpResponse response = client.execute(get)) {
                String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(body.contains("Last: 10"), "Response должен содержать параметр last=10");
            }
        }
    }

    @Test
    void testGetWithoutQueryParam() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet("http://localhost:" + PORT + "/messages");
            try (CloseableHttpResponse response = client.execute(get)) {
                String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(body.contains("Last: none"), "Response должен обрабатывать отсутствие параметра last");
            }
        }
    }

    @Test
    void testPostWithFormParams() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("http://localhost:" + PORT + "/submit");
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setEntity(new StringEntity("param1=value1&param2=value2"));

            try (CloseableHttpResponse response = client.execute(post)) {
                String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(body.contains("param1: value1") && body.contains("param2: value2"),
                        "Response должен содержать переданные POST-параметры");
            }
        }
    }

    @Test
    void testNotFoundEndpoint() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet("http://localhost:" + PORT + "/notfound");
            try (CloseableHttpResponse response = client.execute(get)) {
                String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(body.contains("не найдена") || body.contains("Not Found"),
                        "Неверный endpoint должен возвращать 404");
            }
        }
    }
}
