# Taskmaster

The Taskmaster library is a queue oriented thread management library written in Java. This project will help developers create serial queues of tasks that need to be accomplished in order. 


# Introduction

## Taskmaster
The Taskmaster class is the API into this library. It can be set up with a few different configurations that make it customizable to the situation in which it should be used. 

### Thread count

The most widely used option will be the number of threads. By default, the taskmaster will only use a single thread for executing tasks which is not efficient. Each use case will be different, but in general the thread count should not exceed the number of queues while being as close to as many as possible. 

## Queues

Queues are responsible for keeping the order and status of pending tasks. They are generated from the developer created Taskmaster instance. Once a queue has been created from the taskmaster instance, a developer can add tasks to them. Tasks will be executed in a first in - first out pattern. 

If there is a priority task that must be completed as soon as possible, the task can be submitted directly to the head of the queue. Once the Taskmaster instance is ready to submit a new task to a be executed, this will be the next task taken from that queue. 

### Limited Queues

Limited queues are just like regular queues except they are created with a finite number of tasks. These queues are created with all of their tasks at creation. Once all the tasks are completed, the queue itself closes and the taskmaster will remove it from its memory.

## Tasks

Tasks are the basic building blocks to be submitted to queues. The Task class itself is abstract with a single abstract method that needs to be implemented, the `onExecute()` method. This method will be called once a thread has picked up the Task to be ran. 

