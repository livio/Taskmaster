package com.livio.taskmaster;

/**
 * Implement this interface to define how logging should happen
 */
@SuppressWarnings("unused")
public interface ITaskmasterLogger {


    void v(String tag, String message);

    void d(String tag, String message);

    void i(String tag, String message);

    void w(String tag, String message);

    void e(String tag, String message);

    void e(String tag, String message, Exception e);
}
