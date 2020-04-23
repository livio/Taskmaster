package com.livio.taskmaster.tests;

import com.livio.taskmaster.ITaskmasterLogger;
import com.livio.taskmaster.Queue;
import com.livio.taskmaster.Task;
import com.livio.taskmaster.Taskmaster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Tests {

    public static void main(String[] args) {
        //simpleTest();

        startTheMachine();


        /* If you want to test the daemon setting use something like the following
        new Thread(new Runnable(){
            @Override
            public void run() {
                Object LOCK = new Object();
               try{
                   //LOCK.wait();
                   Thread.sleep(50000);

               }catch (Exception e){
                   e.printStackTrace();}
            }


        }).start();
        */
    }
    public static void simpleTest() {
        //Build the task master instance
        Taskmaster.Builder builder = new Taskmaster.Builder();
        builder.setThreadCount(2);
        builder.shouldBeDaemon(false);
        builder.setTaskMasterLogger(new Logger());
        builder.enableDebug(true);
        taskmaster = builder.build();
        taskmaster.start();

        Queue syncQueue = taskmaster.createQueue("Queue 5", 5, true);
        syncQueue.add(generateTask("51"), false);
        syncQueue.add(generateTask("52"), false);
        syncQueue.add(generateTask("53"), true);
        syncQueue.add(generateTask("54"), false);
        syncQueue.add(generateTask("55"), false);
        syncQueue.add(generateTask("56"), true);
    }

    static int completed = 0;
    static final long startTime = System.currentTimeMillis();
    static Taskmaster taskmaster;
    static List<String> taskNames = new ArrayList<>();
    static Queue q1, q2;

    public static void startTheMachine() {

        //Build the task master instance
        Taskmaster.Builder builder = new Taskmaster.Builder();
        builder.setThreadCount(3);
        builder.shouldBeDaemon(false);
        builder.setTaskMasterLogger(new Logger());
        builder.enableDebug(true);


        taskmaster = builder.build();
        taskmaster.start();

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

    static final Object COUNT_LOCK = new Object(), SLEEP_LOCK = new Object();
    static double totalSleep = 0;
    static int numberOfGeneratedTasks = 0;
    static HashSet<String> threads = new HashSet<>();

    static Task generateTask(final String name) {
        numberOfGeneratedTasks++;
        return new Task(name) {
            @Override
            public void onExecute() {

                threads.add(Thread.currentThread().getName());

                double sleep = new Random().nextInt(5) * 250;
                System.out.println("Sleeping for " + sleep);
                if (name.equals("33")) {
                    synchronized (COUNT_LOCK) {
                        completed++;
                        taskNames.add(this.getName());
                    }
                    this.cancelTask();

                    return;
                }
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
                if (this.getName().equals("3")) {
                    System.out.println(" Closing queue 1");

                    q1.close();
                    synchronized (COUNT_LOCK) {
                        numberOfGeneratedTasks -= 2;
                    }
                    q2.add(generateTask("25"), false);
                    q2.add(generateTask("26"), false);
                    q2.add(generateTask("27"), true);

                }
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
                    }
                }
            }
        };
    }


    //Create logging interface
    public static class Logger implements ITaskmasterLogger {

        @Override
        public void v(String tag, String message) {
            System.out.println("VERBOSE:  " + tag + " : " + message);
        }

        @Override
        public void d(String tag, String message) {
            System.out.println("DEBUG:  " + tag + " : " + message);

        }

        @Override
        public void i(String tag, String message) {
            System.out.println("INFO:  " + tag + " : " + message);

        }

        @Override
        public void w(String tag, String message) {
            System.out.println("WARNING:  " + tag + " : " + message);

        }

        @Override
        public void e(String tag, String message) {
            System.out.println("ERROR:  " + tag + " : " + message);

        }

        @Override
        public void e(String tag, String message, Exception e) {
            System.out.println("ERROR:  " + tag + " : " + message);

        }
    }
}
