package com.livio.taskmaster;


public abstract class Task implements Runnable {

    private static final String TAG = "Task";
    private static final long DELAY_CONSTANT = 500; //250ms
    private static final int DELAY_COEF = 1;

    public static final int BLOCKED = 0x00;
    public static final int READY = 0x10;
    private static final int IN_PROGRESS = 0x30;
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
