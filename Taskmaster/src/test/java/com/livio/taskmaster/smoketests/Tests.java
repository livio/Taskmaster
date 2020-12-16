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

    public static void main(String[] args) {

        Taskmaster.setLogger(new Logger());

        //Need to fix the chaining requirement, but for smoke tests this should be fine
        SimpleTest simpleTest = new SimpleTest(new BaseTest.ITest() {
            @Override
            public void onTestCompleted(boolean success) {
                System.out.println("Finished simple test");
                QueueModTest queueModTest = new QueueModTest(new BaseTest.ITest() {
                    @Override
                    public void onTestCompleted(boolean success) {
                        System.out.println("Finished queue mod test");

                        BigTest bigTest = new BigTest(new BaseTest.ITest() {
                            @Override
                            public void onTestCompleted(boolean success) {
                                System.out.println("Finished big test");

                                PauseTest pauseTest = new PauseTest(new BaseTest.ITest() {
                                    @Override
                                    public void onTestCompleted(boolean success) {
                                        System.out.println("Finished pause test");
                                    }
                                });
                                pauseTest.start();
                            }
                        });
                        bigTest.start();
                    }
                });
                queueModTest.start();
            }
        });

        simpleTest.start();
        //simpleTest();

        //testQueueModificaitons();
        //startTheMachine();


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
