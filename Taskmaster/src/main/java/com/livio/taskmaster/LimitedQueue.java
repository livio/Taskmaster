package com.livio.taskmaster;

import java.util.List;

/**
 * This queue is for a short running set of tasks. Once the tasks are complete, the queue will close.
 */
public class LimitedQueue extends Queue {
    private static final String TAG = "LimitedQueue";

    public LimitedQueue(String name, int id, List<Task> tasks, IQueue callback){
        super(name, id, callback);
        addAll(tasks);

    }

    @Override
    protected void onQueueEmpty() {
        TaskmasterLogger.d(TAG, name + " queue as finished and will close");
        //should queue close?
        if(callback !=null){
            callback.onQueueClosed(LimitedQueue.this);
        }
    }

    private void addAll(List<Task> tasks){
        synchronized (TASKS_LOCK){
            //Go through list and create linked list
            for(Task task : tasks){
                if (head == null) {
                    insertAtHead(task);
                } else {
                    insertAtTail(task);
                }
            }
        }
        unblockNextTask();
    }

    @Override
    public final void add(Task task, boolean placeAtHead) {
        TaskmasterLogger.w(TAG, "Limited queues can't have tasks added after creation");
        return;
    }
}
