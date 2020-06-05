package com.livio.taskmaster;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TaskTest {

    private static final Task.ITask DEFAULT_ITASK = new Task.ITask(){

        @Override
        public void onStateChanged(Task task, int previous, int newState) {

        }
    };

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }



    @Test
    public void switchStates() {
        Task task = TestUtilities.generateSampleTask();
        assertEquals(task.getState(), Task.BLOCKED);
        task.switchStates(Task.READY);
        assertEquals(task.getState(), Task.READY);
        task.switchStates(Task.IN_PROGRESS);
        assertEquals(task.getState(), Task.IN_PROGRESS);
        task.switchStates(Task.FINISHED);
        assertEquals(task.getState(), Task.FINISHED);
        task.switchStates(Task.ERROR);
        assertEquals(task.getState(), Task.ERROR);
        task.switchStates(Task.CANCELED);
        assertEquals(task.getState(), Task.CANCELED);
    }

    @Test
    public void setCallback() {
        Task task = TestUtilities.generateSampleTask();
        assertNull(task.getCallback());
        task.setCallback(DEFAULT_ITASK);
        assertEquals(DEFAULT_ITASK, task.getCallback());
    }

    @Test
    public void onError() {
        Task task = TestUtilities.generateSampleTask();
        task.onError();
        assertEquals(task.getState(), Task.ERROR);
    }

    @Test
    public void onFinished() {
        Task task = TestUtilities.generateSampleTask();
        task.onFinished();
        assertEquals(task.getState(), Task.FINISHED);
    }

    @Test
    public void getState() {
        Task task = TestUtilities.generateSampleTask();
        assertEquals(task.getState(), Task.BLOCKED);
    }

    @Test
    public void getName() {
        Task task = TestUtilities.generateSampleTask();
        assertEquals(task.getName(), TestUtilities.TASK_NAME);
    }

    @Test
    public void getWeight() {
        Task task =TestUtilities. generateSampleTask();
        assertTrue(task.getWeight(System.currentTimeMillis()) > 0);
    }


    @Test
    public void cancelTask() {
        Task task = TestUtilities.generateSampleTask();
        task.cancelTask();
        assertEquals(task.getState(), Task.CANCELED);
    }

    @Test
    public void onExecute() {
        Task task = TestUtilities.generateSampleTask();
        assertEquals(task.getState(), Task.BLOCKED);
        task.run();
        //The task was still in a blocked stated, so the run method should fail
        assertEquals(task.getState(), Task.BLOCKED);

        task.switchStates(Task.READY);
        task.run();
        assertEquals(task.getState(), Task.IN_PROGRESS);
    }
}