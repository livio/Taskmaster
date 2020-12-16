package com.livio.taskmaster.smoketests;

import com.livio.taskmaster.Queue;
import com.livio.taskmaster.Task;

public class PauseTest extends BaseTest {
    Queue queue1, queue2;

    public PauseTest(ITest callback) {
        super(2, callback);
    }

    public void setUp() {

        queue1 = taskmaster.createQueue("Queue 1", 1, false);
        queue1.add(generateTask("1"), false);
        queue1.add(generateTask("2"), false);
        queue1.add(generateTask("3"), false);
        queue1.add(generateTask("4"), false);
        numberOfGeneratedTasks++;
        queue1.add(taskA, false);
        numberOfGeneratedTasks++;
        queue1.add(new Task("Final task") {
            @Override
            public void onExecute() {
                System.out.println("-------- Final Task is executing, test should end after this");
                PauseTest.this.endTest(true);
                this.onFinished();
            }
        }, false);


        queue2 = taskmaster.createQueue("Queue 2", 2, false);
        queue2.pause();
        queue2.add(generateTask("21"), false);
        queue2.add(generateTask("22"), false);
        queue2.add(generateTask("23"), false);
        queue2.add(generateTask("24"), false);
        numberOfGeneratedTasks++;
        queue2.add(taskB, false);
    }

    Task taskA = new Task("-- Task A --") {

        @Override
        public void onExecute() {
            System.out.println("-------- Running task A, should pause queue1, resume queue2");
            if (taskmaster != null && queue1 != null) {
                queue1.pause();
                queue2.resume();
            }
            this.onFinished();
        }
    };

    Task taskB = new Task("-- Task B --") {

        @Override
        public void onExecute() {
            System.out.println("-------- Running task B, should resume queue1");

            if (taskmaster != null && queue1 != null) {
                queue1.resume();
            }
            this.onFinished();
        }
    };


}
