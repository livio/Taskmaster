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


public abstract class Task implements Runnable {

    private static final String TAG = "Task";
    private static final long DELAY_CONSTANT = 500; //250ms
    private static final int DELAY_COEF = 1;

    public static final int BLOCKED = 0x00;
    public static final int READY = 0x10;
    public static final int IN_PROGRESS = 0x30;
    public static final int FINISHED = 0x50;
    public static final int CANCELED = 0xCA;
    public static final int ERROR = 0xFF;

    private final Object STATE_LOCK;
    private final long timestamp;

    private int state;
    private ITask callback;
    final String name;


    public Task(String name) {
        timestamp = System.currentTimeMillis();
        STATE_LOCK = new Object();
        switchStates(BLOCKED);
        this.name = name;
        if(name == null){
            throw new NullPointerException("Name can't be null");
        }
    }

    void switchStates(int newState) {
        TaskmasterLogger.v(TAG, name + " switchStates: " + state);
        int oldState = state;
        synchronized (STATE_LOCK) {
            state = newState;
        }

        if (callback != null) {
            callback.onStateChanged(this, oldState, state);
        }
    }

    void setCallback(ITask callback) {
        this.callback = callback;
    }

    protected void onError() {
        switchStates(ERROR);
    }

    protected void onFinished() {
        switchStates(FINISHED);
    }


    public int getState() {
        synchronized (STATE_LOCK) {
            return state;
        }
    }

    public String getName() {
        return this.name;
    }

    //Currently just tracks how long this task has been waiting
    public long getWeight(long currentTime) {
        return ((((currentTime - timestamp) + DELAY_CONSTANT) * DELAY_COEF));

    }

    @Override
    public final void run() {
        synchronized (STATE_LOCK) {
            if (state != READY) {
                TaskmasterLogger.w(TAG, "run() called while not in state READY. Actual state: " + state);
                return;
            }
        }
        switchStates(IN_PROGRESS);
        TaskmasterLogger.v(TAG, "Task is running");

        onExecute();

    }


    //TODO add a way to cancel the task

    public void cancelTask() {
        switchStates(CANCELED);
    }


    //This method should be implemented by the child class and will be called once a thread is ready to run the task
    public abstract void onExecute();

    public interface ITask {
        @SuppressWarnings("unused")
        void onStateChanged(Task task, int previous, int newState);
    }

}
