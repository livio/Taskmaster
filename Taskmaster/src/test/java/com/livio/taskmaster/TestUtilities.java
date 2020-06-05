package com.livio.taskmaster;

public class TestUtilities {

    public static final String TASK_NAME = "SAMPLE_TASK";


    public static Taskmaster generateTaskMaster(int threadCount){
        Taskmaster.Builder builder = new Taskmaster.Builder();
        builder.setThreadCount(threadCount);
        builder.shouldBeDaemon(false);
        builder.enableDebug(true);
        return builder.build();

    }
    public static Task generateSampleTask() {
        return generateSampleTask(null);
    }

    public static Task generateSampleTask(String name){
        if(name == null){
            name = TASK_NAME;
        }
        return new Task(name){

            boolean isExecuting = false;
            @Override
            public void onExecute() {
                isExecuting = true;
            }
        };

    }

    public static ITaskmasterLogger generateSampleLogger(){
        return new ITaskmasterLogger() {
            @Override
            public void v(String tag, String message) {

            }

            @Override
            public void d(String tag, String message) {

            }

            @Override
            public void i(String tag, String message) {

            }

            @Override
            public void w(String tag, String message) {

            }

            @Override
            public void e(String tag, String message) {

            }

            @Override
            public void e(String tag, String message, Exception e) {

            }
        };
    }
}
