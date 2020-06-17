package com.livio.taskmaster;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class TaskmasterTest {


    private static final String QUEUE_NAME = "TestQueue";
    private static final int QUEUE_ID = 0;

    @Test
    public void setLogger() {
        TaskmasterLogger.clearLogger();
        Taskmaster.setLogger(TestUtilities.generateSampleLogger());

        try{
            TaskmasterLogger.initTaskmasterLogger(null);
        }catch (IllegalStateException e){
            assert true;
            return;
        }
        assert false;
    }


    @Test
    public void createQueue() {
        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        Queue queue = taskmaster.createQueue(QUEUE_NAME, QUEUE_ID, false);

        assertNotNull(queue);
        assertEquals(QUEUE_NAME, queue.getName());

        taskmaster.shutdown();
    }

    @Test
    public void createLimitedQueue() {
        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);

        Queue queue = taskmaster.createLimitedQueue(QUEUE_NAME, QUEUE_ID, new ArrayList<Task>());

        assertNotNull(queue);
        assertEquals(QUEUE_NAME, queue.getName());

        taskmaster.shutdown();
    }
}