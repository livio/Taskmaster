package com.livio.taskmaster.smoketests;

import com.livio.taskmaster.Task;
import com.livio.taskmaster.Taskmaster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public abstract class BaseTest {


    final ITest callback;
    Taskmaster taskmaster;
    int completed = 0;
    long startTime = System.currentTimeMillis();
    List<String> taskNames = new ArrayList<>();
    final Object COUNT_LOCK = new Object(), SLEEP_LOCK = new Object();
    double totalSleep = 0;
    int numberOfGeneratedTasks = 0;
    HashSet<String> threads = new HashSet<>();
    boolean testEnded = false;

    public BaseTest(int threadCount, ITest callback){
        Taskmaster.Builder builder = new Taskmaster.Builder();
        builder.setThreadCount(threadCount);
        builder.shouldBeDaemon(false);
        builder.enableDebug(true);
        taskmaster = builder.build();

        this.callback = callback;
    }

    public abstract void setUp();

    public void onStart(){

    }

    public void endTest(boolean success){
        if(!testEnded) {
            testEnded = true;
            if (taskmaster != null) {
                taskmaster.shutdown();
                taskmaster = null;
                System.out.println("----------------------------------------------------------------");
                System.out.println(" ");
                if (callback != null) {
                    callback.onTestCompleted(success);
                }
            }
        }
    }
    public void start(){
        System.out.println(" ");
        System.out.println("----------------------------------------------------------------");

        setUp();
        if(!testEnded) {
            startTime = System.currentTimeMillis();
            taskmaster.start();
            onStart();
        }else{
            taskmaster = null;
        }
    }

    public void onTestCompleted(boolean success){
        System.out.println("----------------------------------------------------------------");
        System.out.println(" ");
        taskmaster.shutdown();
        taskmaster = null;

        if(callback != null ) {
            callback.onTestCompleted(success);
        }

    }


    public void onTaskExecuting(final Task task){

    }
    public void onTaskFinished(final Task task){

    }

    Task generateTask(final String name) {
        numberOfGeneratedTasks++;
        return new Task(name) {
            @Override
            public void onExecute() {

                threads.add(Thread.currentThread().getName());

                double sleep = new Random().nextInt(5) * 250;
                System.out.println("Sleeping for " + sleep);

                onTaskExecuting(this);

                synchronized (SLEEP_LOCK) {
                    totalSleep += sleep;
                }
                try {
                    Thread.sleep((long) (sleep));
                } catch (Exception e) {
                    e.printStackTrace();
                    this.onError();
                }
                System.out.println("Task finished " + name);

                this.onFinished();

                onTaskFinished(this);

                synchronized (COUNT_LOCK) {
                    completed++;
                    taskNames.add(this.getName());
                    System.out.println("Completed number " + completed);
                    if (completed == numberOfGeneratedTasks) {
                        System.out.println("Sleeps totaled:: " + (totalSleep));
                        System.out.println("Finished all tasks in: " + (System.currentTimeMillis() - startTime));
                        System.out.println("Used # threads: " + threads.size());

                        StringBuilder builder = new StringBuilder();
                        builder.append("Completion Order: ");
                        for (String name : taskNames) {
                            builder.append(name);
                            builder.append(", ");
                        }
                        System.out.println(builder.toString());

                        taskmaster.shutdown();
                        onTestCompleted(true);
                    }
                }
            }
        };
    }


    interface ITest{
        void onTestCompleted(boolean success);
    }
}
