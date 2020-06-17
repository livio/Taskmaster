package com.livio.taskmaster;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class LimitedQueueTest {

    private static final String LIMITED_QUEUE_NAME = "LimitedQueue";
    private static final int LIMITED_QUEUE_ID = 45656;


    @Test
    public void limitedQueueConstructor(){

        List<Task> limitedTasks = new ArrayList<>();
        for(int i=0; i<10; i++){
            limitedTasks.add(TestUtilities.generateSampleTask("Task " +i));
        }

        assertEquals(10, limitedTasks.size());

        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        LimitedQueue queue = taskmaster.createLimitedQueue(LIMITED_QUEUE_NAME, LIMITED_QUEUE_ID, limitedTasks);

        assertEquals(LIMITED_QUEUE_NAME, queue.getName());

        List<Task> tasks = queue.getTasksAsList();
        assertEquals(10, tasks.size());

        taskmaster.shutdown();
    }


    @Test
    public void add() {

        List<Task> limitedTasks = new ArrayList<>();
        for(int i=0; i<10; i++){
            limitedTasks.add(TestUtilities.generateSampleTask("Task " +i));
        }

        assertEquals(10, limitedTasks.size());

        Taskmaster taskmaster = TestUtilities.generateTaskMaster(1);
        LimitedQueue queue = taskmaster.createLimitedQueue(LIMITED_QUEUE_NAME, LIMITED_QUEUE_ID, limitedTasks);


        List<Task> tasks = queue.getTasksAsList();
        assertEquals(10, tasks.size());


        queue.add(TestUtilities.generateSampleTask(), false);
        tasks = queue.getTasksAsList();
        assertEquals(10, tasks.size());

        taskmaster.shutdown();
    }

}