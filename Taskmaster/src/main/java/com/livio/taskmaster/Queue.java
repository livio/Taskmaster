package com.livio.taskmaster;


public class Queue{

    private static final String TAG = "Queue";

    protected final Object TASKS_LOCK;
    protected final String name;
    protected final int id;
    protected final Task.ITask taskCallback;
    protected final IQueue callback;

    //Doubly linked list
    protected Node<Task> head;
    protected Node<Task> tail;


    public Queue(String name, int id, final IQueue callback){
        TASKS_LOCK = new Object();
        this.name = name;
        this.id = id;
        this.callback = callback;
        taskCallback = new Task.ITask() {
            @Override
            public void onStateChanged(Task task, int previous, int newState) {
               // System.out.println("onStateChanged: "+ task.name + " changed to state " + newState);
                switch (newState){
                    case Task.ERROR:
                    case Task.FINISHED:
                        if(unblockNextTask() && callback != null) {
                            TaskmasterLogger.v(TAG, "Task is ready so lets let the master know");

                            //Alert that there is a new task ready
                            callback.onTaskReady(Queue.this);

                        }else if(head == null){
                            onQueueEmpty();
                        }
                        break;
                    case Task.CANCELED:
                        //This will only be called when the task was in progress, but canceled at some point
                        TaskmasterLogger.d(TAG, task.name + " task was canceled during operation");
                        if(unblockNextTask() && callback != null) {
                            TaskmasterLogger.v(TAG, "Task is ready so lets let the master know");

                            //Alert that there is a new task ready
                            callback.onTaskReady(Queue.this);

                        }
                }
            }
        };
    }

    protected void onQueueEmpty(){

    }


    protected boolean unblockNextTask(){
        synchronized (TASKS_LOCK){
            if(head != null ){
                TaskmasterLogger.v(TAG,"unblockNextTask: Attempting to unblock a task for queue " + name);
                Task nextTask = head.item;

                if(nextTask != null && nextTask.getState() == Task.BLOCKED){
                    nextTask.switchStates(Task.READY);
                    nextTask.setCallback(taskCallback);
                    TaskmasterLogger.v(TAG,"unblockNextTask: Unblocked a task for queue " + name);

                    return true;
                }
            }
        }
        TaskmasterLogger.v(TAG, "unblockNextTask: Failed to unblock a task for queue " + name);

        return false;
    }
    /**
     * This will take the given task and insert it at the tail of the queue
     * @param task the task to be inserted at the tail of the queue
     */
    protected void insertAtTail(Task task){
        if (task == null){
            throw new NullPointerException();
        }
        Node<Task> oldTail = tail;
        Node<Task> newTail = new Node<>(task, oldTail, null);
        tail = newTail;
        if (head == null){
            head = newTail;
        }else{
            oldTail.next = newTail;
        }

    }

    /**
     * This will take the given task and insert it at the head of the queue
     * @param task the task to be inserted at the head of the queue
     */
    protected void insertAtHead(Task task){
        if (task == null){
            throw new NullPointerException();
        }
        Node<Task> oldHead = head;
        Node<Task> newHead = new Node<>(task, null, oldHead);
        head = newHead;
        if (tail == null){
            tail = newHead;
        }else{
            if(oldHead!=null){
                oldHead.prev = newHead;
            }
        }
    }

    /**
     * Insert the task in the queue where it belongs
     * @param task the new Task that needs to be added to the queue to be handled
     */
    public void add(Task task, boolean placeAtHead){
        synchronized(TASKS_LOCK){
            if (task == null){
                throw new NullPointerException();
            }

            //If we currently don't have anything in our queue
            if (head == null || tail == null) {
                Node<Task> taskNode = new Node<>(task, head, tail);
                head = taskNode;
                tail = taskNode;

            } else if (placeAtHead) {
                insertAtHead(task);
            } else {
                insertAtTail(task);
            }
        }
        if ( head == tail || placeAtHead){ //If there's either only one task or a new head, we need to set it to ready
            //there is only one task on the stack
            if(unblockNextTask() && callback != null) {
                TaskmasterLogger.v(TAG, "pushaddTask: Alerting task master");

                //Alert that there is a new task ready
                callback.onTaskReady(Queue.this);
            }
        }
    }

    /**
     * Removes the head of the queue.
     * @return the current head of the queue
     */
    public Task poll(){
        synchronized(TASKS_LOCK){
            if (head == null) {
                return null;
            } else {
                Node<Task> retValNode = head;
                Node<Task> newHead = head.next;

                //Check if the next task is cancelled. If so, move to the next task
                while(newHead != null && newHead.item != null && newHead.item.getState() == Task.CANCELED){
                    //Next task was cancelled, so remove it from the queue
                    TaskmasterLogger.d(TAG, newHead.item.name + " task was canceled, moving to next");
                    newHead = newHead.next;
                }

                if(newHead == null){
                    tail = null;
                }

                head = newHead;

                return retValNode.item;
            }
        }
    }

    /**
     * Peeks at the current head of the queue without removing it
     * @return the current head of the queue
     */
    public Task peekNextTask(){
        synchronized (TASKS_LOCK){
            if(head != null ) {
                return head.item;
            }
        }
        return null;
    }

    public void close(){
        synchronized (TASKS_LOCK){
            if(head != null ){
                //call on error for all linked tasks
                Node<Task> current = head;
                while ( current != null && current.next != null ) {
                    current.item.onError();;
                    current = current.next;
                }

            }
        }
        if(callback != null){
            callback.onQueueClosed(this);
        }
    }

    public interface IQueue{
        void onTaskReady(Queue queue);
        void onQueueClosed(Queue queue);
    }

    final class Node<E> {
        final E item;
        Node<E> prev;
        Node<E> next;
        Node(E item, Node<E> previous, Node<E> next) {
            this.item = item;
            this.prev = previous;
            this.next = next;
        }
    }
}
