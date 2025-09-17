package com.vlad.todo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.vlad.todo.exception.InvalidInputException;
import com.vlad.todo.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Spy
    @InjectMocks
    private LogService logService;

    private static final String TEST_DATE = "01-01-2023";
    private static final String LOG_FILE_PATH = "log/app.log";
    private static final Path TEMP_DIR = Paths.get("D:/documents/JavaLabs/temp");

    @BeforeEach
    void setUp() throws IOException {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMock.when(() -> Files.createDirectories(any(Path.class))).thenReturn(TEMP_DIR);
        }
    }

    @Test
    void parseDate_ValidFormat_ReturnsLocalDate() {
        LocalDate result = logService.parseDate(TEST_DATE);
        assertEquals(LocalDate.of(2023, 1, 1), result);
    }

    @Test
    void parseDate_InvalidFormat_ThrowsInvalidInputException() {
        String invalidDate = "2023/01/01";
        assertThrows(InvalidInputException.class, () -> logService.parseDate(invalidDate));
    }

    @Test
    void validateLogFileExists_FileExists_DoesNotThrow() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            assertDoesNotThrow(() -> logService.validateLogFileExists(Path.of(LOG_FILE_PATH)));
        }
    }

    @Test
    void validateLogFileExists_FileNotExists_ThrowsNotFoundException() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(false);
            assertThrows(NotFoundException.class, () -> logService.validateLogFileExists(Path.of(LOG_FILE_PATH)));
        }
    }

    @Test
    void createTempFile_Success_ReturnsPath() throws IOException {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            Path mockPath = mock(Path.class);
            File mockFile = mock(File.class);
            filesMock.when(() -> Files.createTempFile(any(Path.class), anyString(), anyString()))
                    .thenReturn(mockPath);
            when(mockPath.toFile()).thenReturn(mockFile);
            when(mockFile.setReadable(anyBoolean(), anyBoolean())).thenReturn(true);
            when(mockFile.setWritable(anyBoolean(), anyBoolean())).thenReturn(true);
            Path result = logService.createTempFile(LocalDate.now());
            verify(mockFile).setReadable(true, true);
            verify(mockFile).setWritable(true, true);
        }
    }

    @Test
    void createTempFile_CreateFails_ThrowsIllegalStateException() throws IOException {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.createTempFile(any(Path.class), anyString(), anyString()))
                    .thenThrow(new IOException("Failed to create file"));
            assertThrows(IllegalStateException.class, () -> logService.createTempFile(LocalDate.now()));
        }
    }

    @Test
    void filterAndWriteLogsToTempFile_Success_WritesFilteredLines() throws IOException {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            Path logFilePath = Path.of(LOG_FILE_PATH);
            Path tempFilePath = Path.of("temp.log");
            String formattedDate = "01-01-2023";
            BufferedReader mockReader = mock(BufferedReader.class);
            when(mockReader.lines()).thenReturn(List.of(
                    "01-01-2023 Log message 1",
                    "02-01-2023 Log message 2",
                    "01-01-2023 Log message 3"
            ).stream());
            filesMock.when(() -> Files.newBufferedReader(logFilePath)).thenReturn(mockReader);
            logService.filterAndWriteLogsToTempFile(logFilePath, formattedDate, tempFilePath);
            filesMock.verify(() -> Files.write(eq(tempFilePath), eq(List.of(
                    "01-01-2023 Log message 1",
                    "01-01-2023 Log message 3"
            ))));
        }
    }

    @Test
    void createResourceFromTempFile_NonEmptyFile_ReturnsResource() throws IOException {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            Path tempFilePath = Path.of("temp.log");
            filesMock.when(() -> Files.size(tempFilePath)).thenReturn(100L);
            Resource result = logService.createResourceFromTempFile(tempFilePath, TEST_DATE);
            assertNotNull(result);
        }
    }

    @Test
    void createResourceFromTempFile_EmptyFile_ThrowsNotFoundException() throws IOException {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            Path tempFilePath = Path.of("temp.log");
            filesMock.when(() -> Files.size(tempFilePath)).thenReturn(0L);
            assertThrows(NotFoundException.class,
                    () -> logService.createResourceFromTempFile(tempFilePath, TEST_DATE));
        }
    }

    @Test
    void createResourceFromTempFile_WhenIoException_ThrowsIllegalStateException() {
        Path mockTempFilePath = mock(Path.class);
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.size(mockTempFilePath)).thenReturn(100L);
            when(mockTempFilePath.toUri()).thenAnswer(invocation -> {
                throw new IOException("Simulated IO error");
            });
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.createResourceFromTempFile(mockTempFilePath, TEST_DATE));
            assertTrue(exception.getMessage().contains("Ошибка при создании ресурса из временного файла: Simulated IO error"));
            filesMock.verify(() -> Files.size(mockTempFilePath));
            verify(mockTempFilePath).toUri();
        }
    }

    @Test
    void filterAndWriteLogsToTempFile_WhenWriteFails_ThrowsIllegalStateException() {
        Path mockLogFilePath = mock(Path.class);
        Path mockTempFilePath = mock(Path.class);
        String testDate = "01-01-2023";
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            BufferedReader mockReader = mock(BufferedReader.class);
            filesMock.when(() -> Files.newBufferedReader(mockLogFilePath)).thenReturn(mockReader);
            when(mockReader.lines()).thenReturn(Stream.of("01-01-2023 Log line"));
            filesMock.when(() -> Files.write(any(Path.class), anyList()))
                    .thenThrow(new IOException("Simulated write error"));
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.filterAndWriteLogsToTempFile(mockLogFilePath, testDate, mockTempFilePath));
            assertTrue(exception.getMessage().contains("Ошибка при обработке файла логов: Simulated write error"));
        }
    }

    @Test
    void createTempFile_WhenSetReadableFails_ThrowsIllegalStateException() throws IOException {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Path mockPath = mock(Path.class);
            File mockFile = mock(File.class);

            filesMock.when(() -> Files.createTempFile(any(Path.class), anyString(), anyString()))
                    .thenReturn(mockPath);
            when(mockPath.toFile()).thenReturn(mockFile);
            when(mockFile.setReadable(true, true)).thenReturn(false);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.createTempFile(LocalDate.now()));

            assertTrue(exception.getMessage().contains("Не удалось установить права на чтение"));
        }
    }

    @Test
    void createTempFile_WhenSetWritableFails_ThrowsIllegalStateException() throws IOException {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Path mockPath = mock(Path.class);
            File mockFile = mock(File.class);

            filesMock.when(() -> Files.createTempFile(any(Path.class), anyString(), anyString()))
                    .thenReturn(mockPath);
            when(mockPath.toFile()).thenReturn(mockFile);
            when(mockFile.setReadable(true, true)).thenReturn(true);
            when(mockFile.setWritable(true, true)).thenReturn(false);
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.createTempFile(LocalDate.now()));

            assertTrue(exception.getMessage().contains("Не удалось установить права на запись"));
        }
    }

    @Test
    void createTempFile_WhenSetExecutableFails_LogsWarning() throws IOException {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Path mockPath = mock(Path.class);
            File mockFile = mock(File.class);

            filesMock.when(() -> Files.createTempFile(any(Path.class), anyString(), anyString()))
                    .thenReturn(mockPath);
            when(mockPath.toFile()).thenReturn(mockFile);
            when(mockFile.setReadable(true, true)).thenReturn(true);
            when(mockFile.setWritable(true, true)).thenReturn(true);
            when(mockFile.canExecute()).thenReturn(true);
            when(mockFile.setExecutable(false, false)).thenReturn(false);
            Path result = logService.createTempFile(LocalDate.now());
            verify(mockFile).setExecutable(false, false);
        }
    }

    @Test
    void createTempFile_WhenAllPermissionsSet_ReturnsPath() throws IOException {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Path mockPath = mock(Path.class);
            File mockFile = mock(File.class);

            filesMock.when(() -> Files.createTempFile(any(Path.class), anyString(), anyString()))
                    .thenReturn(mockPath);
            when(mockPath.toFile()).thenReturn(mockFile);
            when(mockFile.setReadable(true, true)).thenReturn(true);
            when(mockFile.setWritable(true, true)).thenReturn(true);
            when(mockFile.canExecute()).thenReturn(true);
            when(mockFile.setExecutable(false, false)).thenReturn(true);
            Path result = logService.createTempFile(LocalDate.now());
            verify(mockFile).setReadable(true, true);
            verify(mockFile).setWritable(true, true);
            verify(mockFile).setExecutable(false, false);
        }
    }

    @Test
    void downloadLogs_FileNotExists_ThrowsNotFoundException() {
        try (MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
             MockedStatic<Files> filesMock = mockStatic(Files.class)) {

            Path mockPath = mock(Path.class);
            pathsMock.when(() -> Paths.get(LOG_FILE_PATH)).thenReturn(mockPath);
            filesMock.when(() -> Files.exists(mockPath)).thenReturn(false);

            when(logService.parseDate(TEST_DATE)).thenReturn(LocalDate.now());
            assertThrows(NotFoundException.class,
                    () -> logService.downloadLogs(TEST_DATE));

            filesMock.verify(() -> Files.exists(mockPath));
        }
    }

}