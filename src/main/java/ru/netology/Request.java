package ru.netology;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers = new HashMap<>();
    private final String body;

    // Конструктор принимает BufferedReader и парсит запрос
    public Request(BufferedReader in) throws IOException {
        // Читаем первую строку: METHOD /path HTTP/1.1
        String startLine = in.readLine();
        if (startLine == null || startLine.isEmpty()) {
            throw new IOException("Empty request");
        }
        String[] parts = startLine.split(" ");
        method = parts[0];
        path = parts[1];

        // Читаем заголовки до пустой строки
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(":");
            if (idx > 0) {
                headers.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
            }
        }

        // Если есть Content-Length — читаем тело
        String contentLengthHeader = headers.get("Content-Length");
        if (contentLengthHeader != null) {
            int length = Integer.parseInt(contentLengthHeader);
            char[] buf = new char[length];
            int read = in.read(buf, 0, length);
            body = new String(buf, 0, read);
        } else {
            body = null;
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
