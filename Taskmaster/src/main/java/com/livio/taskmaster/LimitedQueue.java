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

import java.util.List;

/**
 * This queue is for a short running set of tasks. Once the tasks are complete, the queue will close.
 */
public class LimitedQueue extends Queue {
    private static final String TAG = "LimitedQueue";

    public LimitedQueue(String name, int id, List<Task> tasks, boolean asynchronous, IQueue callback) {
        super(name, id, asynchronous, callback);
        addAll(tasks);

    }

    @Override
    protected void onQueueEmpty() {
        TaskmasterLogger.d(TAG, name + " queue as finished and will close");
        //should queue close?
        if (callback != null) {
            callback.onQueueClosed(LimitedQueue.this);
        }
    }

    private void addAll(List<Task> tasks) {
        synchronized (TASKS_LOCK) {
            //Go through list and create linked list
            for (Task task : tasks) {
                if (head == null) {
                    insertAtHead(task);
                } else {
                    insertAtTail(task);
                }
            }
        }
        prepareNextTask();
    }

    @SuppressWarnings("unused")
    @Override
    public final void add(Task task, boolean placeAtHead) {
        TaskmasterLogger.w(TAG, "Limited queues can't have tasks added after creation");
    }
}
