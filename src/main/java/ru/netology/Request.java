package ru.netology;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;


public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers = new HashMap<>();
    private final String body;
    private final Map<String, String> queryParams = new HashMap<>();

    public Request(BufferedReader in) throws IOException {
        String startLine = in.readLine();
        if (startLine == null || startLine.isEmpty()) {
            throw new IOException("Empty request");
        }
        String[] parts = startLine.split(" ");
        method = parts[0];

        String fullPath = parts[1];
        String[] pathParts = fullPath.split("\\?", 2);
        path = pathParts[0];

        if (pathParts.length > 1) {
            String query = pathParts[1];
            List<NameValuePair> params = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
            for (NameValuePair pair : params) {
                queryParams.put(pair.getName(), pair.getValue());
            }
        }

        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(":");
            if (idx > 0) {
                headers.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
            }
        }

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


    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }
}

