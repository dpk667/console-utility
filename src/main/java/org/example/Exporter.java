package org.example;

import java.io.File;

public interface Exporter {
    void export(File file) throws ExporterException;
}

// Исключение, выбрасываемое экспортерами данных при возникновении ошибки
class ExporterException extends Exception {
    ExporterException(String message) {
        super(message);
    }
}
