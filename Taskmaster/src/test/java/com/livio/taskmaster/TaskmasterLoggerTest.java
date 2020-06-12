package com.livio.taskmaster;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class TaskmasterLoggerTest {

    private static final String SAMPLE_LOG_MESSAGE = "HELLO WORLD";

    static TestLogger logger = new TestLogger();

    @BeforeClass
    public static void setUp() {
        TaskmasterLogger.clearLogger();
        TaskmasterLogger.initTaskmasterLogger(logger);
        TaskmasterLogger.enableLogs(true);
    }


    @Test
    public void v() {
        TaskmasterLogger.v(SAMPLE_LOG_MESSAGE, SAMPLE_LOG_MESSAGE);
        assertNotNull(logger.logMap.get(TestLogger.LOG_LEVEL_V));

        assertEquals(logger.logMap.get(TestLogger.LOG_LEVEL_V), SAMPLE_LOG_MESSAGE);
    }

    @Test
    public void d() {
        TaskmasterLogger.d(SAMPLE_LOG_MESSAGE, SAMPLE_LOG_MESSAGE);
        assertNotNull(logger.logMap.get(TestLogger.LOG_LEVEL_D));

        assertEquals(logger.logMap.get(TestLogger.LOG_LEVEL_D), SAMPLE_LOG_MESSAGE);
    }

    @Test
    public void i() {
        TaskmasterLogger.i(SAMPLE_LOG_MESSAGE, SAMPLE_LOG_MESSAGE);
        assertNotNull(logger.logMap.get(TestLogger.LOG_LEVEL_I));

        assertEquals(logger.logMap.get(TestLogger.LOG_LEVEL_I), SAMPLE_LOG_MESSAGE);
    }

    @Test
    public void w() {
        TaskmasterLogger.w(SAMPLE_LOG_MESSAGE, SAMPLE_LOG_MESSAGE);
        assertNotNull(logger.logMap.get(TestLogger.LOG_LEVEL_W));

        assertEquals(logger.logMap.get(TestLogger.LOG_LEVEL_W), SAMPLE_LOG_MESSAGE);
    }

    @Test
    public void e() {
        TaskmasterLogger.e(SAMPLE_LOG_MESSAGE, SAMPLE_LOG_MESSAGE);
        assertNotNull(logger.logMap.get(TestLogger.LOG_LEVEL_E));

        assertEquals(logger.logMap.get(TestLogger.LOG_LEVEL_E), SAMPLE_LOG_MESSAGE);
    }

    @Test
    public void e2() {
        TaskmasterLogger.e(SAMPLE_LOG_MESSAGE, SAMPLE_LOG_MESSAGE, new Exception("Oh no!"));
        assertNotNull(logger.logMap.get(TestLogger.LOG_LEVEL_E2));

        assertEquals(logger.logMap.get(TestLogger.LOG_LEVEL_E2), SAMPLE_LOG_MESSAGE);
    }

    private static class TestLogger implements ITaskmasterLogger{

        static final String LOG_LEVEL_V = "V";
        static final String LOG_LEVEL_D = "D";
        static final String LOG_LEVEL_I = "I";
        static final String LOG_LEVEL_W = "W";
        static final String LOG_LEVEL_E = "E";
        static final String LOG_LEVEL_E2 = "E2";

        final HashMap<String, String> logMap;

        TestLogger(){
            logMap = new HashMap<>();
        }

        @Override
        public void v(String tag, String message) {
            logMap.put(LOG_LEVEL_V, message);
        }

        @Override
        public void d(String tag, String message) {
            logMap.put(LOG_LEVEL_D, message);
        }

        @Override
        public void i(String tag, String message) {
            logMap.put(LOG_LEVEL_I, message);
        }

        @Override
        public void w(String tag, String message) {
            logMap.put(LOG_LEVEL_W, message);

        }

        @Override
        public void e(String tag, String message) {
            logMap.put(LOG_LEVEL_E, message);

        }

        @Override
        public void e(String tag, String message, Exception e) {
            logMap.put(LOG_LEVEL_E2, message);

        }
    };
}