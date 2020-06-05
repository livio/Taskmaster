package com.livio.taskmaster;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class QueueTest {

    private static final String QUEUE_NAME_SYNC = "TestQueueSync";
    private static final int QUEUE_ID_SYNC = 0;
    private static final String QUEUE_NAME_ASYNC = "TestQueueAsync";
    private static final int QUEUE_ID_ASYNC = 1;


    Taskmaster taskmaster;
    Queue basicSyncQueue, basicAsyncQueue;

    @Before
    public void setUp() throws Exception {
        taskmaster = TestUtilities.generateTaskMaster(2);
        initBasicQueues();
    }

    @After
    public void tearDown() throws Exception {
        taskmaster.shutdown();
    }

    public void initBasicQueues(){
        basicSyncQueue = taskmaster.createQueue(QUEUE_NAME_SYNC, QUEUE_ID_SYNC, false);
        basicAsyncQueue = taskmaster.createQueue(QUEUE_NAME_ASYNC, QUEUE_ID_ASYNC, true);

        basicSyncQueue.add(TestUtilities.generateSampleTask(), false);
    }


    @Test
    public void getName() {
        assertEquals(QUEUE_NAME_SYNC,basicSyncQueue.getName());
    }


    @Test
    public void prepareNextTask() {
        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        Queue queue = taskmaster.createQueue(QUEUE_NAME_SYNC, QUEUE_ID_SYNC, false);

        assertFalse(queue.prepareNextTask());

        queue.add(TestUtilities.generateSampleTask(), false);

        assertTrue(queue.prepareNextTask());

        taskmaster.shutdown();

    }

    @Test
    public void insertAtTail() {
        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        Queue queue = taskmaster.createQueue(QUEUE_NAME_SYNC, QUEUE_ID_SYNC, false);

        queue.add(TestUtilities.generateSampleTask(), false);

        List<Task> tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(1, tasks.size());

        queue.insertAtTail(TestUtilities.generateSampleTask("shouldBeTail"));

        tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(2, tasks.size());

        assertEquals(TestUtilities.TASK_NAME, tasks.get(0).getName());
        assertEquals("shouldBeTail", tasks.get(tasks.size() -1).getName());

        taskmaster.shutdown();
    }

    @Test
    public void insertAtHead() {
        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        Queue queue = taskmaster.createQueue(QUEUE_NAME_SYNC, QUEUE_ID_SYNC, false);

        queue.add(TestUtilities.generateSampleTask(), false);

        List<Task> tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(1, tasks.size());

        queue.insertAtHead(TestUtilities.generateSampleTask("shouldBeHead"));

        tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(2, tasks.size());

        assertEquals("shouldBeHead", tasks.get(0).getName());
        assertEquals(TestUtilities.TASK_NAME, tasks.get(1).getName());

        taskmaster.shutdown();
    }

    @Test
    public void add() {
        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        Queue queue = taskmaster.createQueue(QUEUE_NAME_SYNC, QUEUE_ID_SYNC, false);

        queue.add(TestUtilities.generateSampleTask(), false);

        List<Task> tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        queue.add(TestUtilities.generateSampleTask("head"), true);

        tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(2, tasks.size());

        taskmaster.shutdown();
    }

    @Test
    public void poll() {
        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        Queue queue = taskmaster.createQueue(QUEUE_NAME_SYNC, QUEUE_ID_SYNC, false);

        Task task = TestUtilities.generateSampleTask();
        queue.add(task, false);

        Task polledTask = queue.poll();
        assertEquals(task, polledTask);

        List<Task> tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(0, tasks.size());

        taskmaster.shutdown();
    }

    @Test
    public void pause() {
        assertFalse(basicSyncQueue.isPaused());

        basicSyncQueue.pause();

        assertTrue(basicSyncQueue.isPaused());
    }

    @Test
    public void resume() {
        assertFalse(basicSyncQueue.isPaused());

        basicSyncQueue.pause();

        assertTrue(basicSyncQueue.isPaused());

        basicSyncQueue.resume();

        assertFalse(basicSyncQueue.isPaused());

    }

    @Test
    public void deleteTask() {
        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        Queue queue = taskmaster.createQueue(QUEUE_NAME_SYNC, QUEUE_ID_SYNC, false);

        queue.add(TestUtilities.generateSampleTask(), false);

        List<Task> tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(1, tasks.size());

        queue.deleteTask(TestUtilities.TASK_NAME);

        tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(0, tasks.size());

        taskmaster.shutdown();
    }

    @Test
    public void clear() {
        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        Queue queue = taskmaster.createQueue(QUEUE_NAME_SYNC, QUEUE_ID_SYNC, false);

        queue.add(TestUtilities.generateSampleTask("1"), true);
        queue.add(TestUtilities.generateSampleTask("2"), true);
        queue.add(TestUtilities.generateSampleTask("3"), true);

        List<Task> tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(3, tasks.size());

        queue.clear();

        tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(0, tasks.size());

        taskmaster.shutdown();
    }

    @Test
    public void getTask() {
        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        Queue queue = taskmaster.createQueue(QUEUE_NAME_SYNC, QUEUE_ID_SYNC, false);

        queue.add(TestUtilities.generateSampleTask(), false);


        Task task = queue.getTask(TestUtilities.TASK_NAME);

        assertNotNull(task);
        assertEquals(TestUtilities.TASK_NAME, task.getName());

        taskmaster.shutdown();

    }

    @Test
    public void getTasksAsList() {
        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        Queue queue = taskmaster.createQueue(QUEUE_NAME_SYNC, QUEUE_ID_SYNC, false);

        List<Task> tasks = queue.getTasksAsList();
        assertEquals(0, tasks.size());

        queue.add(TestUtilities.generateSampleTask(), false);
        tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertNotEquals(0, tasks.size());

        taskmaster.shutdown();

    }

    @Test
    public void peekNextTask() {

        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        Queue queue = taskmaster.createQueue(QUEUE_NAME_SYNC, QUEUE_ID_SYNC, false);

        Task task = TestUtilities.generateSampleTask();
        queue.add(task, false);

        Task polledTask = queue.peekNextTask();
        assertEquals(task, polledTask);

        List<Task> tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(1, tasks.size());

        taskmaster.shutdown();
    }

    @Test
    public void close() {
        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        Queue queue = taskmaster.createQueue(QUEUE_NAME_SYNC, QUEUE_ID_SYNC, false);

        queue.add(TestUtilities.generateSampleTask("1"), true);
        queue.add(TestUtilities.generateSampleTask("2"), true);
        queue.add(TestUtilities.generateSampleTask("3"), true);

        List<Task> tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(3, tasks.size());

        queue.close();

        tasks = queue.getTasksAsList();

        assertNotNull(tasks);
        assertEquals(0, tasks.size());

        taskmaster.shutdown();
    }
}