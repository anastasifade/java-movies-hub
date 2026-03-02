package ru.practicum.moviehub.logging;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MoviesLogger {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yy - HH:mm:ss");
    private static final String DEFAULT_LOG_PATH = "log.txt";
    private static final String DEFAULT_ERROR_LOG_PATH = "errorLog.txt";
    private final PrintWriter logger;
    private final PrintWriter errorLogger;

    public MoviesLogger() throws FileNotFoundException {
        this.logger = new PrintWriter(DEFAULT_LOG_PATH);
        this.errorLogger = new PrintWriter(DEFAULT_ERROR_LOG_PATH);
    }

    public MoviesLogger(String logPath, String errorLogPath) throws FileNotFoundException {
        this.logger = new PrintWriter(logPath);
        this.errorLogger = new PrintWriter(errorLogPath);
    }

    public void error(Exception e, String message) {
        LocalDateTime timestamp = LocalDateTime.now();

        logger.printf("[%s] [ERROR]: %s. See error log for stack trace.\n", timestamp.format(dtf), message);
        logger.flush();

        errorLogger.printf("[%s]:\n", timestamp.format(dtf));
        e.printStackTrace(errorLogger);
        errorLogger.println();
        errorLogger.flush();
    }

    public void warn(String message) {
        logger.printf("[%s] [WARN]: %s.\n", LocalDateTime.now().format(dtf), message);
        logger.flush();
    }

    public void info(String message) {
        logger.printf("[%s] [INFO]: %s.\n", LocalDateTime.now().format(dtf), message);
        logger.flush();
    }
}
