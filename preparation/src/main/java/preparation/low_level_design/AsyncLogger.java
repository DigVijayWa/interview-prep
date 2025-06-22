package preparation.low_level_design;

import java.util.*;
import java.util.concurrent.*;

public class AsyncLogger {
    private final BlockingQueue<LogMessage> memory;
    private static final int INTERVAL_IN_SECONDS = 2;
    private final ScheduledExecutorService scheduler;
    private final LogLevel minLogLevel;

    public AsyncLogger(final LogLevel logLevel) {
        this.memory = new LinkedBlockingQueue<>();
        this.minLogLevel = logLevel;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(this::flush, INTERVAL_IN_SECONDS, INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
    }

    public void log(final String message, final LogLevel logLevel) {
        memory.offer(new LogMessage(logLevel, message, System.currentTimeMillis(), Thread.currentThread().getName()));
    }

    protected void flush() {
        List<LogMessage> logsToWrite = new ArrayList<>();
        memory.drainTo(logsToWrite);
        for (LogMessage log : logsToWrite) {
            if (log.logLevel.getPriority() >= this.minLogLevel.getPriority()) {
                writeToDisk(log);
            }
        }
    }

    private void writeToDisk(final LogMessage logMessage) {
        System.out.printf("[%s] [%s] %s%n",
                new Date(logMessage.timestamp),
                logMessage.threadName,
                logMessage.message
        );
    }

    public void shutdown() {
        scheduler.shutdown();
        flush();
    }
}

class LogMessage {
    final LogLevel logLevel;
    final String message;
    final long timestamp;
    final String threadName;

    public LogMessage(final LogLevel logLevel, final String message, final long timestamp, final String threadName) {
        this.logLevel = logLevel;
        this.message = message;
        this.timestamp = timestamp;
        this.threadName = threadName;
    }
}

enum LogLevel {
    DEBUG(1),
    INFO(2),
    ERROR(3);

    private final int priority;
    LogLevel(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return this.priority;
    }
}
