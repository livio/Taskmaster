package com.livio.taskmaster;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Currently each queue is going to be a serial queue.
 * In order to allow for concurrent queues, we will need to adjust monitoring the executor service to make sure
 * a thread is available, or each queue has had an equal chance to operate. Otherwise, the task master will simply
 * add all items from the concurrent queue into the executor service queue at once, this will block other tasks from
 * occurring until all those tasks are complete.
 */
public class Taskmaster {

    private static final String TAG = "Taskmaster";

    private final Object QUEUE_LOCK;
    private final Vector<Queue> queues;
    private final TaskmasterThread taskmasterThread;
    private final Queue.IQueue queueCallback;


    private boolean shouldBeDameon = false, debugEnabled = false;
    private TaskmasterLogger taskMasterLogger;
    private ExecutorService executorService;

    public Taskmaster() {
        QUEUE_LOCK = new Object();
        queues = new Vector<>();
        taskmasterThread = new TaskmasterThread();
        queueCallback = new Queue.IQueue() {
            @Override
            public void onTaskReady(Queue queue) {
                if (taskmasterThread != null) {
                    taskmasterThread.alert();
                }
            }

            @Override
            public void onQueueClosed(Queue queue) {
                synchronized (QUEUE_LOCK) {
                    queues.remove(queue);
                }

            }
        };
    }

    //Init

    protected void initThreadPool(int threadCount) {
        if (threadCount > 1) {
            executorService = Executors.newFixedThreadPool(threadCount);
        } else if (threadCount == 1) {
            executorService = Executors.newSingleThreadExecutor();
        } else {
            //Unbound limit of threads
            executorService = Executors.newCachedThreadPool();
        }
    }


    //Public API

    public synchronized void start() {
        if (taskmasterThread != null) {
            taskmasterThread.start();
        }
    }

    public synchronized void shutdown() {
        if (taskmasterThread != null) {
            taskmasterThread.close();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public Queue createQueue(String name, int id) {
        synchronized (QUEUE_LOCK) {
            Queue queue = new Queue(name, id, queueCallback);
            queues.add(queue);
            return queue;
        }

    }

    public LimitedQueue createLimitedQueue(String name, int id, List<Task> tasks) {
        LimitedQueue queue;
        synchronized (QUEUE_LOCK) {
            queue = new LimitedQueue(name, id, tasks, queueCallback);
            queues.add(queue);
        }
        if (taskmasterThread != null) {
            //Try to alert the task master thread to start churning through tasks if necessary
            taskmasterThread.alert();
        }
        return queue;
    }

    protected Task getNextTask() {
        TaskmasterLogger.v(TAG, "Getting next task");

        final long currentTime = System.currentTimeMillis();

        Queue priorityQueue = null;

        long currentPriority = -Long.MAX_VALUE, peekWeight;

        synchronized (QUEUE_LOCK) {

            if (queues == null || queues.isEmpty()) {
                // System.out.println("No queues available");
                return null;
            }

            Task peekTask;
            for (Queue queue : queues) {
                peekTask = queue.peekNextTask();
                if (peekTask != null && peekTask.getState() == Task.READY) {
                    peekWeight = peekTask.getWeight(currentTime);
                    if (peekWeight > currentPriority) {
                        currentPriority = peekWeight;
                        priorityQueue = queue;
                    }
                }
            }

            if (priorityQueue != null) {
                TaskmasterLogger.v(TAG, "Priority queue is " + priorityQueue.name);
                return priorityQueue.poll();
            }
        }

        return null;
    }


    private class TaskmasterThread extends Thread {
        protected final Object TASK_THREAD_LOCK = new Object();
        private boolean isHalted = false, isWaiting = false;

        public TaskmasterThread() {
            this.setName("TaskmasterThread");
            this.setDaemon(shouldBeDameon);
        }


        @Override
        public void run() {
            while (!isHalted) {
                try {
                    Task task;
                    synchronized (TASK_THREAD_LOCK) {
                        task = getNextTask();
                        if (task != null) {
                            TaskmasterLogger.d(TAG, "Submitting task to executor service");

                            //There is a task that needs to be executed
                            //Find a thread to run this on
                            executorService.submit(task);
                            //FIXME we might want to switch this to a better managed system
                            //For example, this method simply takes all queues and processes them

                        } else {

                            TaskmasterLogger.d(TAG, "No tasks ready, pausing thread");

                            isWaiting = true;
                            TASK_THREAD_LOCK.wait();
                            isWaiting = false;
                        }
                    }
                } catch (InterruptedException e) {
                    Taskmaster.this.shutdown();
                    break;
                }
            }
        }

        private void alert() {

            if (isWaiting) {
                synchronized (TASK_THREAD_LOCK) {
                    TASK_THREAD_LOCK.notify();
                }
            }
        }

        private void close() {
            this.isHalted = true;
            alert();
        }
    }


    public static class Builder {
        Taskmaster taskMaster;
        int threadCount;
        ITaskmasterLogger logger;

        public Builder() {
            taskMaster = new Taskmaster();
        }

        //there is a diminishing return once thread count >= queue count
        public Builder setThreadCount(int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public Builder shouldBeDameon(boolean shouldBeDameon) {
            taskMaster.shouldBeDameon = shouldBeDameon;
            return this;
        }

        public Builder setTaskMasterLogger(ITaskmasterLogger taskMasterLogger) {
            this.logger = taskMasterLogger;
            return this;
        }

        public Builder enableDebug(boolean enableDebug) {
            taskMaster.debugEnabled = enableDebug;
            return this;
        }


        public Taskmaster build() {

            taskMaster.taskMasterLogger = new TaskmasterLogger(logger);
            TaskmasterLogger.enableLogs(taskMaster.debugEnabled);
            taskMaster.initThreadPool(threadCount);

            return taskMaster;
        }

    }
}
