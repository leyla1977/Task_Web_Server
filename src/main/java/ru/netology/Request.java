package ru.netology;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;


import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private String method;
    private String path;
    private String protocol;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, List<String>> params = new HashMap<>();     // GET + POST (form-urlencoded)
    private final Map<String, List<String>> postParams = new HashMap<>(); // только POST
    private final Map<String, List<Part>> parts = new HashMap<>();        // multipart
    private String body;

    public Request(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

        // ----- 1. Стартовая строка -----
        String startLine = in.readLine();
        if (startLine == null || startLine.isEmpty()) {
            throw new IOException("Пустой запрос");
        }
        String[] partsLine = startLine.split(" ");
        method = partsLine[0];
        String uri = partsLine[1];
        protocol = partsLine.length > 2 ? partsLine[2] : "";

        // Query string
        int qIdx = uri.indexOf("?");
        if (qIdx >= 0) {
            path = uri.substring(0, qIdx);
            String query = uri.substring(qIdx + 1);
            parseParams(query, params);
        } else {
            path = uri;
        }

        // ----- 2. Заголовки -----
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(":");
            if (idx > 0) {
                String name = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                headers.put(name, value);
            }
        }

        // ----- 3. Тело -----
        String contentLengthHeader = headers.get("Content-Length");
        if (contentLengthHeader != null) {
            int length = Integer.parseInt(contentLengthHeader);
            char[] buf = new char[length];
            int read = in.read(buf, 0, length);
            body = new String(buf, 0, read);

            String contentType = headers.get("Content-Type");
            if (contentType != null) {
                if (contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
                    parseParams(body, postParams);
                    mergeParams(postParams);
                } else if (contentType.toLowerCase().startsWith("multipart/")) {
                    parseMultipart(contentType, new ByteArrayInputStream(body.getBytes(StandardCharsets.ISO_8859_1)));
                }
            }
        } else {
            body = null;
        }
    }

    // ================= Getters =================

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public String getProtocol() { return protocol; }
    public String getHeader(String name) { return headers.get(name); }
    public String getBody() { return body; }

    // ----- Универсальные параметры (GET+POST form-urlencoded) -----
    public String getParam(String name) {
        List<String> values = params.get(name);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }

    public List<String> getParams(String name) {
        return params.getOrDefault(name, List.of());
    }

    public Map<String, List<String>> getAllParams() {
        return params;
    }

    // ----- POST параметры -----
    public String getPostParam(String name) {
        List<String> values = postParams.get(name);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }

    public List<String> getPostParams(String name) {
        return postParams.getOrDefault(name, List.of());
    }

    public Map<String, List<String>> getPostParams() {
        return postParams;
    }

    // ----- Multipart -----
    public Part getPart(String name) {
        List<Part> list = parts.get(name);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    public List<Part> getParts(String name) {
        return parts.getOrDefault(name, List.of());
    }

    public Map<String, List<Part>> getParts() {
        return parts;
    }

    // ================= Внутренние методы =================

    private void parseParams(String raw, Map<String, List<String>> target) {
        if (raw == null || raw.isEmpty()) return;
        String[] pairs = raw.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            target.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
    }

    private void mergeParams(Map<String, List<String>> source) {
        for (var e : source.entrySet()) {
            params.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).addAll(e.getValue());
        }
    }

    private void parseMultipart(String contentType, InputStream inputStream) {
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            FileUpload upload = new FileUpload(factory); // для сокетного сервера
            upload.setHeaderEncoding("UTF-8");

            // Оборачиваем InputStream и headers
            InputStreamRequestContext requestContext = new InputStreamRequestContext(contentType, inputStream, headers);

            // Получаем все элементы формы
            List<FileItem> items = upload.parseRequest(requestContext);

            for (FileItem item : items) {
                if (item.isFormField()) {
                    // обычное поле
                    parts.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>())
                            .add(new Part(item.getFieldName(), item.getString("UTF-8")));
                } else {
                    // файл
                    parts.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>())
                            .add(new Part(item.getFieldName(), item.getName(), item.get()));

                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка разбора multipart", e);
        }
    }
}
