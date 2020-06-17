package com.livio.taskmaster.smoketests;

import com.livio.taskmaster.Queue;
import com.livio.taskmaster.Task;

import java.util.List;

public class QueueModTest extends BaseTest {


    public QueueModTest( ITest callback){
        super(2, callback);
    }

    @Override
    public void setUp(){
        Queue syncQueue = taskmaster.createQueue("Queue 5", 5, true);
        syncQueue.add(generateTask("51"), false);
        syncQueue.add(generateTask("52"), false);
        syncQueue.add(generateTask("53"), true);
        syncQueue.add(generateTask("54"), false);
        syncQueue.add(generateTask("55"), false);
        syncQueue.add(generateTask("56"), true);

        List<Task> tasks = syncQueue.getTasksAsList();
        for(Task task : tasks){
            System.out.println("Task in queue: " + task.getName());
        }

        //Test getting a task
        Task getTask = syncQueue.getTask("54");
        System.out.println("Get task: " + getTask.getName());

        //Test deleting a task
        Task delTask = syncQueue.deleteTask("54");
        System.out.println("Deleted task: " + delTask.getName());

        tasks = syncQueue.getTasksAsList();
        for(Task task : tasks){
            System.out.println("Task in queue: " + task.getName());
        }

        //Test removing head
        Task peekTask = syncQueue.peekNextTask();
        delTask = syncQueue.deleteTask(peekTask.getName());
        System.out.println("Deleted head task: " + delTask.getName());
        System.out.println("Head task state: " + syncQueue.peekNextTask().getState());

        tasks = syncQueue.getTasksAsList();
        for(Task task : tasks){
            System.out.println("Task in queue: " + task.getName());
        }

        syncQueue.clear();
        tasks = syncQueue.getTasksAsList();
        System.out.println("Clear tasks in queue: " + tasks.size());

        for(Task task : tasks){
            System.out.println("Task in queue: " + task.getName());
        }

        //Taskmaster doesn't need to run
        this.endTest(true);
    }

}
