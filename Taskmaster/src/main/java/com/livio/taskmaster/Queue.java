/*
 * Copyright (c) 2020 Livio, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the Livio Inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.livio.taskmaster;


import java.util.ArrayList;
import java.util.List;

public class Queue {

    private static final String TAG = "Queue";

    final Object TASKS_LOCK, PAUSE_LOCK;
    final String name;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final int id;
    private final Task.ITask taskCallback;
    private final boolean asynchronous;
    final IQueue callback;

    //Doubly linked list
    Node<Task> head;
    private Node<Task> tail;
    private Task currentTask = null;
    private boolean isPaused = false;


    public Queue(String name, int id, boolean asynchronous, final IQueue callback) {
        TASKS_LOCK = new Object();
        PAUSE_LOCK = new Object();
        this.name = name;
        this.id = id;
        this.asynchronous = asynchronous;
        this.callback = callback;
        taskCallback = new Task.ITask() {
            @Override
            public void onStateChanged(Task task, int previous, int newState) {
                // System.out.println("onStateChanged: "+ task.name + " changed to state " + newState);
                switch (newState) {
                    case Task.IN_PROGRESS:
                        if(Queue.this.asynchronous){
                            TaskmasterLogger.w(TAG, task.name + " task is in advance and queue is asynchronous");
                            handleCompletedTask(task);
                        }
                        break;
                    case Task.ERROR:
                        TaskmasterLogger.w(TAG, task.name + " task encountered an error");
                        handleCompletedTask(task);
                        break;
                    case Task.FINISHED:
                        TaskmasterLogger.d(TAG, task.name + " task has finished");
                        handleCompletedTask(task);
                        break;
                    case Task.CANCELED:
                        //This will only be called when the task was in advance, but canceled at some point
                        TaskmasterLogger.w(TAG, task.name + " task was canceled during operation");
                        handleCompletedTask(task);
                        break;
                }
            }

            private void handleCompletedTask(final Task task){

                if(task != null){
                    task.setCallback(null);
                }

                if(task == currentTask){
                    currentTask = null;
                }

                advance();
            }
        };
    }

    public String getName(){
        return this.name;
    }

    void onQueueEmpty() {
        TaskmasterLogger.i(TAG, this.name + " queue is now empty");
    }


    boolean prepareNextTask() {
        synchronized (TASKS_LOCK) {
            if (head != null) {
                TaskmasterLogger.v(TAG, "prepareNextTask: Attempting to unblock a task for queue " + name);
                Task nextTask = head.item;

                if(nextTask != null){
                    int taskState = nextTask.getState();
                    while ( head != null && taskState != Task.READY ){
                        if(taskState == Task.CANCELED){
                            TaskmasterLogger.v(TAG, nextTask.name + " task was canceled, dropping.");
                            //move to next task
                            head = head.next;
                            if(head == null){
                                tail = null;
                            }else{
                                taskState = head.item.getState();

                            }
                        }else if( taskState == Task.BLOCKED){
                            nextTask.switchStates(Task.READY);
                            break;
                        }
                    }
                    return head != null && head.item.getState() == Task.READY;
                }
            }
        }
        TaskmasterLogger.v(TAG, "prepareNextTask: Failed to unblock a task for queue " + name);

        return false;
    }

    /**
     * This method will try to move the queue forward
     * @return
     */
    private boolean advance(){
        if (prepareNextTask()) {

            if (callback != null) {
                //Alert that there is a new task ready
                callback.onTaskReady(Queue.this);
            }
            return true;

        } else if (head == null) {
            onQueueEmpty();
        }

        return false;
    }

    /**
     * This will take the given task and insert it at the tail of the queue
     *
     * @param task the task to be inserted at the tail of the queue
     */
    void insertAtTail(Task task) {
        if (task == null) {
            throw new NullPointerException();
        }
        Node<Task> oldTail = tail;
        Node<Task> newTail = new Node<>(task, oldTail, null);
        tail = newTail;
        if (head == null) {
            head = newTail;
        } else {
            oldTail.next = newTail;
        }

    }

    /**
     * This will take the given task and insert it at the head of the queue
     *
     * @param task the task to be inserted at the head of the queue
     */
    void insertAtHead(Task task) {
        if (task == null) {
            throw new NullPointerException();
        }
        Node<Task> oldHead = head;
        Node<Task> newHead = new Node<>(task, null, oldHead);
        head = newHead;
        if (tail == null) {
            tail = newHead;
        } else {
            if (oldHead != null) {
                oldHead.prev = newHead;
            }
        }
    }

    /**
     * Insert the task in the queue where it belongs
     *
     * @param task the new Task that needs to be added to the queue to be handled
     */
    public void add(Task task, boolean placeAtHead) {
        synchronized (TASKS_LOCK) {
            if (task == null) {
                throw new NullPointerException();
            }
            TaskmasterLogger.d(TAG,"Adding task " + task.getName());

            //If we currently don't have anything in our queue
            if (head == null || tail == null) {
                Node<Task> taskNode = new Node<>(task, head, tail);
                head = taskNode;
                tail = taskNode;

            } else if (placeAtHead) {
                if(head != null){
                    //reset the current head in case it is in a ready state
                    head.item.setCallback(null);
                    head.item.switchStates(Task.BLOCKED);
                }
                insertAtHead(task);
            } else {
                insertAtTail(task);
            }
        }
        if ((head == tail  || placeAtHead ) && currentTask == null) { //If there's either only one task or a new head, we need to set it to ready
            //there is only one task on the stack
            if (prepareNextTask() && callback != null) {
                TaskmasterLogger.v(TAG, "pushaddTask: Alerting task master");

                //Alert that there is a new task ready
                callback.onTaskReady(Queue.this);
            }
        }
    }

    /**
     * Removes the head of the queue.
     *
     * @return the current head of the queue
     */
    public Task poll() {

        synchronized (PAUSE_LOCK){
            if(isPaused){
                return null;
            }
        }

        synchronized (TASKS_LOCK) {
            if ( head == null ) {
                TaskmasterLogger.i(TAG, "Poll: head is null");
                return null;
            } else if ( head.item.getState() != Task.READY) {
                TaskmasterLogger.i(TAG, "Poll: head task sate is not READY: " + head.item.getState());
                return null;
            } else if ( currentTask != null) {
                TaskmasterLogger.i(TAG, "Poll: currentTask is not null");
                return null;
            } else {
                Node<Task> retValNode = head;
                Node<Task> newHead = head.next;

                //Check if the next task is cancelled. If so, move to the next task
                while (newHead != null && newHead.item != null && newHead.item.getState() == Task.CANCELED) {
                    //Next task was cancelled, so remove it from the queue
                    TaskmasterLogger.d(TAG, newHead.item.name + " task was canceled, moving to next");
                    newHead = newHead.next;
                }

                if (newHead == null) {
                    tail = null;
                }

                head = newHead;

                currentTask = retValNode.item;

                if ( currentTask != null ) {
                    currentTask.setCallback(taskCallback);
                }

                return currentTask;
            }
        }
    }

    public void pause(){
       synchronized (PAUSE_LOCK){
           isPaused = true;
       }
    }

    public void resume(){
        synchronized (PAUSE_LOCK){
            isPaused = false;
        }

        advance();
    }


    public Task deleteTask(String name){
        Task removedTask = null;
        synchronized (TASKS_LOCK) {
            removedTask = searchAndDestroy(name, true);
        }

        if (removedTask != null && removedTask.getState() == Task.READY){
            removedTask.setCallback(null);
            advance();
        }

        return removedTask;
    }

    public void clear(){
        synchronized (TASKS_LOCK) {
            head = null;
            tail = null;
            onQueueEmpty();
        }
    }

    public Task getTask(String name){
        synchronized (TASKS_LOCK) {
            return searchAndDestroy(name, false);
        }
    }

    /**
     * This will returned a copied list of the Tasks in this queue. The list itself will not honor
     * any modifications performed on it. However, the references to the Tasks themselves are the same as
     * in the queue. Therefore, the queue API should be used for any modifications to those Tasks to ensure
     * thread safety.
     * @return a list of the current tasks
     */
    public List<Task> getTasksAsList(){
        synchronized (TASKS_LOCK) {
            Node current = head;    //Initialize current
            List<Task> list = new ArrayList<>();
            while (current != null) {
                list.add((Task)current.item);
                current = current.next;
            }
            return list;
        }
    }

    /**
     * A simple method to traverse the linked nodes and find a specific task
     * @param name String name of the task being searched for
     * @param shouldRemove true if the task should be removed from the list
     * @return the found task
     */
    private Task searchAndDestroy(String name, boolean shouldRemove){
        Node current = head;    //Initialize current
        while (current != null) {

            if (current.item != null && ( (Task) current.item).getName().equals(name)) {
                //Task has been found

                if (shouldRemove) {

                    if (current.prev != null) {
                        current.prev.next = current.next;
                    }else{
                        //That means this is the head
                        head = current.next;
                    }

                    if (current.next != null) {
                        current.next.prev = current.prev;
                    }

                    //Clear no longer linked nodes
                    current.prev = null;
                    current.next = null;
                }

                return (Task) current.item;    //data found
            }
            current = current.next;
        }

        return null;
    }

    /**
     * Peeks at the current head of the queue without removing it
     *
     * @return the current head of the queue
     */
    public Task peekNextTask() {
        synchronized (TASKS_LOCK) {
            if (head != null) {
                return head.item;
            }
        }
        return null;
    }

    public void close() {
        synchronized (TASKS_LOCK) {
            if (head != null) {
                //call on error for all linked tasks
                Node<Task> current = head;
                while (current != null && current.next != null) {
                    current.item.onError();
                    current = current.next;
                }

            }
        }
        if (callback != null) {
            callback.onQueueClosed(this);
        }
    }

    @SuppressWarnings("unused")
    public interface IQueue {
        void onTaskReady(Queue queue);

        void onQueueClosed(Queue queue);
    }

    @SuppressWarnings("unused")
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
