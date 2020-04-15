package com.livio.taskmaster;

@SuppressWarnings("ALL")
class TaskmasterLogger {

    private static ITaskmasterLogger logger;
    private static boolean logsEnabled = false;

    TaskmasterLogger(ITaskmasterLogger logger) {
        if (TaskmasterLogger.logger != null) {
            throw new IllegalStateException("Already initialized with logger");
        } else {
            TaskmasterLogger.logger = logger;
        }
    }

    static void enableLogs(boolean enableLogs) {
        TaskmasterLogger.logsEnabled = enableLogs;
    }

    static void v(String tag, String message) {
        if (logsEnabled && logger != null) {
            logger.v(tag, message);
        }
    }

    static void d(String tag, String message) {
        if (logsEnabled && logger != null) {
            logger.v(tag, message);
        }
    }

    static void i(String tag, String message) {
        if (logsEnabled && logger != null) {
            logger.v(tag, message);
        }
    }

    static void w(String tag, String message) {
        if (logsEnabled && logger != null) {
            logger.v(tag, message);
        }
    }

    static void e(String tag, String message) {
        if (logsEnabled && logger != null) {
            logger.v(tag, message);
        }
    }

    static void e(String tag, String message, Exception e) {
        if (logsEnabled && logger != null) {
            logger.e(tag, message, e);
        }
    }
}
