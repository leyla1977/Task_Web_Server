package ru.netology;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;


import java.io.*;
import java.util.*;

public class Request {
    private String method;
    private String path;
    private String protocol;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, List<String>> params = new HashMap<>();     // GET + POST
    private final Map<String, List<String>> postParams = new HashMap<>(); // только POST form-urlencoded
    private final Map<String, List<Part>> parts = new HashMap<>();        // multipart
    private String body;

    public Request(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

        // ----- 1. Request line -----
        String requestLine = in.readLine();
        if (requestLine == null) return;
        String[] requestParts = requestLine.split(" ");
        method = requestParts[0];
        path = requestParts[1];
        protocol = requestParts[2];

        // ----- 2. Headers -----
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(":");
            if (idx != -1) {
                String name = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                headers.put(name, value);
            }
        }

        // ----- 3. Query parameters (GET) -----
        int qIdx = path.indexOf("?");
        if (qIdx != -1) {
            parseQueryParams(path.substring(qIdx + 1));
            path = path.substring(0, qIdx);
        }

        // ----- 4. Body -----
        String contentLengthHeader = headers.get("Content-Length");
        String contentType = headers.get("Content-Type");

        if (contentLengthHeader != null) {
            int length = Integer.parseInt(contentLengthHeader);

            if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
                // multipart: читаем как байты
                byte[] bodyBytes = new byte[length];
                int totalRead = 0;
                while (totalRead < length) {
                    int read = inputStream.read(bodyBytes, totalRead, length - totalRead);
                    if (read == -1) break;
                    totalRead += read;
                }
                parseMultipart(contentType, new ByteArrayInputStream(bodyBytes));

            } else {
                // form-urlencoded или текст
                char[] buf = new char[length];
                int read = in.read(buf, 0, length);
                body = new String(buf, 0, read);

                if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType)) {
                    parseParams(body, postParams);
                    mergeParams(postParams);
                }
            }
        } else {
            body = null;
        }
    }

    // ----------------- Методы работы с параметрами -----------------

    private void parseQueryParams(String query) {
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = kv[0];
            String value = kv.length > 1 ? kv[1] : "";
            params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
    }

    private void parseParams(String query, Map<String, List<String>> target) {
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = kv[0];
            String value = kv.length > 1 ? kv[1] : "";
            target.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
    }

    private void mergeParams(Map<String, List<String>> postParams) {
        for (Map.Entry<String, List<String>> entry : postParams.entrySet()) {
            params.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                    .addAll(entry.getValue());
        }
    }

    // ----------------- Multipart -----------------
    private void parseMultipart(String contentType, InputStream inputStream) {
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            FileUpload upload = new FileUpload(factory);
            upload.setHeaderEncoding("UTF-8");
            System.out.println("parseMultipart start, contentType=" + contentType);

            FileItemIteratorWrapper iter = new FileItemIteratorWrapper(upload, contentType, inputStream, headers);
            while (iter.hasNext()) {
                FileItem item = iter.next();
                if (item.isFormField()) {
                    parts.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>())
                            .add(new Part(item.getFieldName(), item.getString("UTF-8")));
                } else {
                    parts.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>())
                            .add(new Part(item.getFieldName(), item.getName(), item.get()));
                }
            }
            System.out.println("parseMultipart done, parts=" + parts.size());

        } catch (Exception e) {
            throw new RuntimeException("Ошибка разбора multipart", e);
        }
    }

    // ----------------- GETTERS -----------------
    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }

    public String getParam(String name) {
        List<String> values = params.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    public List<String> getParams(String name) {
        return params.getOrDefault(name, Collections.emptyList());
    }

    public String getPostParam(String name) {
        List<String> values = postParams.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    public List<String> getPostParams(String name) {
        return postParams.getOrDefault(name, Collections.emptyList());
    }

    public Part getPart(String name) {
        List<Part> list = parts.get(name);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    public List<Part> getParts(String name) {
        return parts.getOrDefault(name, Collections.emptyList());
    }
}

// ----------------- Вспомогательный wrapper для FileItemIterator -----------------
class  FileItemIteratorWrapper implements Iterator<FileItem> {
    private final List<FileItem> items;
    private int index = 0;

    public FileItemIteratorWrapper(FileUpload upload, String contentType, InputStream inputStream, Map<String,String> headers) throws Exception {
        RequestContext context = new RequestContext() {
            @Override
            public String getCharacterEncoding() { return "UTF-8"; }
            @Override
            public String getContentType() { return contentType; }
            @Override
            public int getContentLength() {
                String cl = headers.get("Content-Length");
                return cl == null ? -1 : Integer.parseInt(cl);
            }
            @Override
            public InputStream getInputStream() { return inputStream; }
        };
        items = upload.parseRequest(context);
    }

    @Override
    public boolean hasNext() {
        return index < items.size();
    }

    @Override
    public FileItem next() {
        return items.get(index++);
    }
}