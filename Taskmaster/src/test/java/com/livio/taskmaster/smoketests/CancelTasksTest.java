package com.livio.taskmaster.smoketests;

import com.livio.taskmaster.Queue;
import com.livio.taskmaster.Task;

public class CancelTasksTest extends BaseTest {
    Queue queue;

    public CancelTasksTest(ITest callback) {
        super(1, callback);
    }

    @Override
    public void setUp() {
        queue = taskmaster.createQueue("Queue", 1, false);
        queue.add(taskA, false);
        taskA.cancelTask();
        queue.add(taskB, false);
        queue.add(taskC, false);
    }


    Task taskA = new Task("-- Task A --") {

        @Override
        public void onExecute() {
            System.out.println("-------- Running task A, this should not happen");
            this.onFinished();
        }
    };

    Task taskB = new Task("-- Task B --") {

        @Override
        public void onExecute() {
            System.out.println("-------- Running task B");
            this.onFinished();
        }
    };
    Task taskC = new Task("-- Task C --") {

        @Override
        public void onExecute() {
            System.out.println("-------- Running task C");
            this.onFinished();
            taskmaster.shutdown();
            onTestCompleted(true);
        }
    };
}
