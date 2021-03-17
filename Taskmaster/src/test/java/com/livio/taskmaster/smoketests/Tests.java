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

package com.livio.taskmaster.smoketests;

import com.livio.taskmaster.ITaskmasterLogger;
import com.livio.taskmaster.Taskmaster;

public class Tests {

    private final static int SIMPLE = 0;
    private final static int QUEUE_MOD = 1;
    private final static int BIG = 2;
    private final static int PAUSE = 3;
    private final static int CANCEL_TASK = 4;

    public static void main(String[] args) {

        Taskmaster.setLogger(new Logger());


        BaseTest.ITest testCallback = new BaseTest.ITest() {
            int testProgress = -1;

            @Override
            public void onTestCompleted(boolean success) {
                if (testProgress >= 0) {
                    System.out.println("------------ Finished " + nameForTask(testProgress) + " test successfully? " + success);
                }

                testProgress++;
                BaseTest nextTest = nextTestToRun(testProgress, this);

                if (nextTest != null) {
                    System.out.println("------------ Starting test: " + nameForTask(testProgress) + " ------------");
                    nextTest.start();
                } else {
                    System.out.println(" ------------------   Finished all tests       ------------------");
                    return;
                }
            }
        };
        testCallback.onTestCompleted(true);
    }


    public static BaseTest nextTestToRun(int progress, BaseTest.ITest callback) {
        switch (progress) {
            case SIMPLE:
                return new SimpleTest(callback);
            case QUEUE_MOD:
                return new QueueModTest(callback);
            case BIG:
                return new BigTest(callback);
            case PAUSE:
                return new PauseTest(callback);
            case CANCEL_TASK:
                return new CancelTasksTest(callback);
        }
        return null;
    }

    public static String nameForTask(int progress) {
        switch (progress) {
            case SIMPLE:
                return "SIMPLE";
            case QUEUE_MOD:
                return "QUEUE_MOD";
            case BIG:
                return "BIG";
            case PAUSE:
                return "PAUSE";
            case CANCEL_TASK:
                return "CANCEL_TASK";
        }
        return null;
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
