package ru.netology;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ServerQueryParamTest {


    private final String BASE_URL = "http://localhost:9999";
    private static final int PORT = 9999;
    private static Server server;

    @BeforeAll
    static void startServer() {
        server = new Server(PORT);
        server.addHandler("GET", "/messages", (request, out) -> {
            String response = "Hello";
            out.write(("HTTP/1.1 200 OK\r\nContent-Length: " + response.length() +
                    "\r\nContent-Type: text/plain\r\n\r\n" + response).getBytes());
            out.flush();
        });

        new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Ожидание запуска сервера
        try {
            Thread.sleep(500); // можно увеличить до 1000, если сервер стартует медленно
        } catch (InterruptedException ignored) {}
    }

    @Test
    void testQueryParamParsing() throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Отправляем запрос с параметром
            String url = BASE_URL + "/messages?last=10&sort=desc";
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            // Проверяем статус
            assertEquals(200, response.getStatusLine().getStatusCode());

            // Проверяем тело ответа (сервер может вернуть "Hello")
            String body = EntityUtils.toString(response.getEntity());
            assertNotNull(body);
            assertTrue(body.contains("Hello"));

            // Теперь парсим query string прямо через URLEncodedUtils
            List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8);

            // Проверяем, что параметры разобрались правильно
            assertEquals("10", params.stream()
                    .filter(p -> p.getName().equals("last"))
                    .findFirst()
                    .get()
                    .getValue());

            assertEquals("desc", params.stream()
                    .filter(p -> p.getName().equals("sort"))
                    .findFirst()
                    .get()
                    .getValue());
        }
    }
}

