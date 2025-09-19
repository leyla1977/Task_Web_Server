**CURL для тестов:**
**1. GET-запросы с параметрами**
curl -v "http://localhost:9999/messages?last=5"
curl -v "http://localhost:9999/messages?last=1&last=2&last=3"
Используется request.getQueryParam("last") или getQueryParams().
Проверяет обработку нескольких GET-параметров с одинаковым именем.

**2. POST с обычной формой (x-www-form-urlencoded)**
curl -v -X POST http://localhost:9999/messages \ -d "hobby=code&hobby=music"
curl -v -X POST http://localhost:9999/messages \ -d "user=Alex&text=Hello!"
Проверяет request.getPostParam("user") и request.getPostParams().
Для нескольких одинаковых полей (hobby) проверяет, что сохраняются все значения.

**3. POST с multipart/form-data (файл + поля)**
curl -v -X POST http://localhost:9999/upload \
  -F "user=Alex" \
  -F "avatar=@C:/Work/Java/Task_Web_Server/file.txt"
request.getPart("user") возвращает поле user.
request.getPart("avatar") возвращает файл.
Для проверки размера файла - part.getContent().length (никак не сумела добиться, чтобы подтягивался).

**4. POST с несколькими файлами одного имени**
curl -v -X POST http://localhost:9999/upload \
  -F "photos=@C:/Work/Java/Task_Web_Server/file.txt" \
  -F "photos=@C:/Work/Java/Task_Web_Server/file2.txt"
request.getParts("photos") вернёт список всех загруженных файлов.
Можно проверить имена через part.getFilename() и для проверки размера -  part.getContent().length (не подтягивается).

Чтобы подтянуть, пробовала:
1. Использовать другой handler:
   server.addHandler("POST", "/messages", (request, out) -> {
    String user = null;
    String text = null;

    // 1. Пробуем вытащить application/x-www-form-urlencoded
    if (request.getPostParam("user") != null) {
        user = request.getPostParam("user");
    }
    if (request.getPostParam("text") != null) {
        text = request.getPostParam("text");
    }

    // 2. Если multipart → пробуем через getPart()
    if (request.getPart("user") != null) {
        user = request.getPart("user").getValue();
    }
    if (request.getPart("text") != null) {
        text = request.getPart("text").getValue();
    }

    // 3. Если есть файл avatar
    Part avatar = request.getPart("avatar");

    // 4. Формируем ответ
    StringBuilder response = new StringBuilder();
    response.append("Пользователь: ").append(user).append("\n");
    response.append("Текст: ").append(text).append("\n");

    if (avatar != null && avatar.isFile()) {
        response.append("Загружен файл: ")
                .append(avatar.getFilename())
                .append(" (")
                .append(avatar.getContent().length)
                .append(" bytes)\n");
    } else {
        response.append("Файл не загружен\n");
    }

    out.write((
            "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "\r\n" +
                    response
    ).getBytes());
    out.flush();
});
Размер не видит.
2. Для multipart/form-data читать байты и передавать в parseMultipart:
String contentType = headers.get("Content-Type");
if (contentLengthHeader != null) {
    int length = Integer.parseInt(contentLengthHeader);

    if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
        // читаем как сырые байты, без конвертации в String
        byte[] bodyBytes = in.readNBytes(length);
        parseMultipart(contentType, new ByteArrayInputStream(bodyBytes));
    } else {
        // x-www-form-urlencoded или текст
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

Соответственно в handler тоже добавлала вывод:
  // Отправляем ответ
    byte[] responseBytes = response.toString().getBytes(StandardCharsets.UTF_8);
    out.write(("HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/plain; charset=UTF-8\r\n" +
            "Content-Length: " + responseBytes.length + "\r\n\r\n").getBytes());
    out.write(responseBytes);
    out.flush();
    
тогда названия файла не видит. 
Уже мозг кипит несколько дней. Сегодня с нуля переписывала. Где-то что-то не так, но что?









