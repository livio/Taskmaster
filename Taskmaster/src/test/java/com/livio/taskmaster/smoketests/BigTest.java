package com.livio.taskmaster.smoketests;

import com.livio.taskmaster.Queue;
import com.livio.taskmaster.Task;

import java.util.ArrayList;
import java.util.List;

public class BigTest extends BaseTest {

    static Queue q1, q2;


    public BigTest( ITest callback){
        super(2, callback);
    }

    @Override
    public void setUp() {
        //Nothing to do
    }

    @Override
    public void onStart() {
        q1 = taskmaster.createQueue("Queue 1", 1, true);
        q2 = taskmaster.createQueue("Queue 2", 2, false);
        Queue q3 = taskmaster.createQueue("Queue 3", 3,true);
        Queue q5 = taskmaster.createQueue("Queue 5", 5,false);


        q1.add(generateTask("1"), false);
        q1.add(generateTask("2"), false);
        q1.add(generateTask("3"), false);
        q1.add(generateTask("4"), false);
        q1.add(generateTask("5"), false);

        q2.add(generateTask("21"), false);
        q2.add(generateTask("22"), false);
        q2.add(generateTask("23"), false);
        Task task = generateTask("24");
        numberOfGeneratedTasks--;
        task.cancelTask();
        q2.add(task, false);

        q3.add(generateTask("31"), false);
        q3.add(generateTask("32"), false);
        q3.add(generateTask("33"), false);
        q3.add(generateTask("34"), false);
        q3.add(generateTask("35"), true);

        List<Task> tasks = new ArrayList<>();
        tasks.add(generateTask("41"));
        tasks.add(generateTask("42"));
        tasks.add(generateTask("43"));

        Queue q4 = taskmaster.createLimitedQueue("Queue 4", 3, tasks);

        q5.add(generateTask("51"), false);
        q5.add(generateTask("52"), false);
        q5.add(generateTask("53"), true);
        q5.add(generateTask("54"), false);
        q5.add(generateTask("55"), false);

    }

    @Override
    public void onTaskExecuting(final Task task){
        if (task.getName().equals("33")) {
            synchronized (COUNT_LOCK) {
                completed++;
                taskNames.add(task.getName());
            }
            task.cancelTask();

            return;
        }
    }

    @Override
    public void onTaskFinished(final Task task){
        if (task.getName().equals("3")) {
            System.out.println(" Closing queue 1");

            q1.close();
            synchronized (COUNT_LOCK) {
                numberOfGeneratedTasks -= 2;
            }
            q2.add(generateTask("25"), false);
            q2.add(generateTask("26"), false);
            q2.add(generateTask("27"), true);

        }
    }
}
