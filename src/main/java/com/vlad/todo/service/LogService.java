package com.vlad.todo.service;

import com.vlad.todo.exception.InvalidInputException;
import com.vlad.todo.exception.NotFoundException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LogService {

    private static final String LOG_FILE_PATH = "log/app.log";
    private static final Path TEMP_DIR = Paths.get("D:/documents/JavaLabs/temp");

    static {
        try {
            if (!Files.exists(TEMP_DIR)) {
                Files.createDirectories(TEMP_DIR);
                log.info("Создана защищённая временная директория: {}", TEMP_DIR);
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Не удаётся создать защищённую временную директорию", e);
        }
    }

    public Resource downloadLogs(String date) {
        LocalDate logDate = parseDate(date);
        Path logFilePath = Paths.get(LOG_FILE_PATH);
        validateLogFileExists(logFilePath);
        String formattedDate = logDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        Path tempFilePath = createTempFile(logDate);
        filterAndWriteLogsToTempFile(logFilePath, formattedDate, tempFilePath);

        Resource resource = createResourceFromTempFile(tempFilePath, date);
        log.info("Файл логов с датой {} успешно загружен", date);
        return resource;
    }

    private LocalDate parseDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Неверный формат даты. Требуется dd-MM-yyyy");
        }
    }

    private void validateLogFileExists(Path path) {
        if (!Files.exists(path)) {
            throw new NotFoundException("Файл не существует: " + LOG_FILE_PATH);
        }
    }

    private Path createTempFile(LocalDate logDate) {
        try {
            File tempFile = Files.createTempFile(TEMP_DIR, "log-"
                    + logDate + "-", ".log").toFile();
            if (!tempFile.setReadable(true, true)) {
                throw new IllegalStateException("Не удалось установить права на чтение "
                        + "для временного файла: " + tempFile);
            }
            if (!tempFile.setWritable(true, true)) {
                throw new IllegalStateException("Не удалось установить права на запись "
                        + "для временного файла: " + tempFile);
            }
            if (tempFile.canExecute()
                    && !tempFile.setExecutable(false, false)) {
                log.warn("Не удалось удалить права на выполнение для временного файла: {}",
                        tempFile);
            }
            log.info("Создан защищённый временный файл: {}", tempFile.getAbsolutePath());
            return tempFile.toPath();
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка при создании временного файла: "
                    + e.getMessage());
        }
    }

    private void filterAndWriteLogsToTempFile(Path logFilePath,
                                              String formattedDate, Path tempFilePath) {
        try (BufferedReader reader = Files.newBufferedReader(logFilePath)) {
            Files.write(tempFilePath, reader.lines()
                    .filter(line -> line.contains(formattedDate))
                    .toList());
            log.info("Отфильтрованные логи за дату {} записаны во временный файл {}",
                    formattedDate, tempFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка при обработке файла логов: " + e.getMessage());
        }
    }

    private Resource createResourceFromTempFile(Path tempFilePath, String date) {
        try {
            if (Files.size(tempFilePath) == 0) {
                throw new NotFoundException("Нет логов за указанную дату: " + date);
            }
            Resource resource = new UrlResource(tempFilePath.toUri());
            tempFilePath.toFile().deleteOnExit();
            log.info("Создан загружаемый ресурс из временного файла: {}", tempFilePath);
            return resource;
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка при создании ресурса из временного файла: "
                    + e.getMessage());
        }
    }

}