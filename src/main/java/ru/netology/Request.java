package ru.netology;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;



import java.util.*;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers = new HashMap<>();
    private final String body;
    private final Map<String, List<String>> queryParams = new HashMap<>();
    private final Map<String, List<String>> postParams = new HashMap<>();

    public Request(BufferedReader in) throws IOException {
        // читаем startLine
        String startLine = in.readLine();
        if (startLine == null || startLine.isEmpty()) throw new IOException("Empty request");
        String[] parts = startLine.split(" ");
        method = parts[0];
        String fullPath = parts[1];

        // разбиваем путь и query
        String[] pathParts = fullPath.split("\\?", 2);
        path = pathParts[0];
        if (pathParts.length > 1) {
            parseParams(pathParts[1], queryParams);
        }

        // читаем заголовки
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(":");
            if (idx > 0) headers.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
        }

        // читаем тело, если есть Content-Length
        String contentLengthHeader = headers.get("Content-Length");
        if (contentLengthHeader != null) {
            int length = Integer.parseInt(contentLengthHeader);
            char[] buf = new char[length];
            int read = in.read(buf, 0, length);
            body = new String(buf, 0, read);
            if ("application/x-www-form-urlencoded".equals(headers.get("Content-Type"))) {
                parseParams(body, postParams);
            }
        } else {
            body = null;
        }
    }

    private void parseParams(String paramString, Map<String, List<String>> map) {
        String[] pairs = paramString.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = kv[0];
            String value = kv.length > 1 ? kv[1] : "";
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }


    public String getQueryParam(String name) {
        List<String> values = queryParams.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }


    public String getPostParam(String name) {
                List<String> values = postParams.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    public Map<String, List<String>> getPostParams() {
        return postParams;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
    public String getBody() {
        return body;
    }
}

