package ru.netology;

public class Part {
    private final String name;
    private final String value; // если поле обычное
    private final String filename; // если файл
    private final byte[] content;

    // Конструктор для текстового поля
    public Part(String name, String value) {
        this.name = name;
        this.value = value;
        this.filename = null;
        this.content = null;
    }

    // Конструктор для файла
    public Part(String name, String filename, byte[] content) {
        this.name = name;
        this.value = null;
        this.filename = filename;
        this.content = content;
    }

    public String getName() { return name; }
    public boolean isFile() { return filename != null; }
    public String getValue() { return value; }
    public String getFilename() { return filename; }
    public byte[] getContent() { return content; }
}
