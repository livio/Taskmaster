package com.livio.taskmaster.smoketests;

import com.livio.taskmaster.Queue;

public class SimpleTest extends BaseTest {


    public SimpleTest( ITest callback){
        super(2, callback);
    }

    public void setUp(){

        Queue syncQueue = taskmaster.createQueue("Queue 5", 5, true);
        syncQueue.add(generateTask("51"), false);
        syncQueue.add(generateTask("52"), false);
        syncQueue.add(generateTask("53"), true);
        syncQueue.add(generateTask("54"), false);
        syncQueue.add(generateTask("55"), false);
        syncQueue.add(generateTask("56"), true);
    }

}
