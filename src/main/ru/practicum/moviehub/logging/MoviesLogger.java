package ru.practicum.moviehub.logging;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MoviesLogger {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yy - HH:mm:ss");
    private static final String DEFAULT_PATH = "log.txt";
    private final PrintWriter logger;

    public MoviesLogger() throws FileNotFoundException {
        this.logger = new PrintWriter(DEFAULT_PATH);
    }

    public MoviesLogger(String path) throws FileNotFoundException {
        this.logger = new PrintWriter(path);
    }

    public void logError(Exception e, String message) {
        logger.printf("[%s][ERROR]: %s", LocalDateTime.now().format(dtf), message);
        e.printStackTrace(logger);
        logger.flush();
    }

    public void logWarn(Exception e, String message) {
        logger.printf("[%s][WARN]: %s", LocalDateTime.now().format(dtf), message);
        e.printStackTrace(logger);
        logger.flush();
    }

    public void logInfo(String message) {
        logger.printf("[%s][INFO]: %s", LocalDateTime.now().format(dtf), message);
        logger.flush();
    }
}
