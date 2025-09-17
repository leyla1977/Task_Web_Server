package ru.netology;

import org.apache.commons.fileupload.RequestContext;
import java.io.InputStream;
import java.util.Map;

public class InputStreamRequestContext implements RequestContext {
    private final String contentType;
    private final InputStream inputStream;
    private final Map<String, String> headers;

    public InputStreamRequestContext(String contentType, InputStream inputStream, Map<String, String> headers) {
        this.contentType = contentType;
        this.inputStream = inputStream;
        this.headers = headers;
    }


    @Override
    public String getCharacterEncoding() {
        return "UTF-8"; // можно менять, если нужно
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public int getContentLength() {
        String cl = headers.get("Content-Length");
        return cl == null ? -1 : Integer.parseInt(cl);
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }
}


