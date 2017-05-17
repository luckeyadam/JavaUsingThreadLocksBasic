package com.adamluckey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static com.adamluckey.Main.EOF;

/**
 * This is an abnormal use of locks for basic unlock/lock demonstration
 * and not the typical use of locking and unlocking using the reentrant lock
 */
public class Main {
    public static final String EOF = "EOF";

    public static void main(String[] args) {
        // Array list isn't thread safe, it needs to be synchronized below
        List<String> buffer = new ArrayList<>();
        ReentrantLock bufferLock = new ReentrantLock(); // prevent thread interference using a lock
        MyProducer producer = new MyProducer(buffer, ThreadColor.ANSI_GREEN, bufferLock);
        MyConsumer consumer1 = new MyConsumer(buffer, ThreadColor.ANSI_PURPLE, bufferLock);
        MyConsumer consumer2 = new MyConsumer(buffer, ThreadColor.ANSI_CYAN, bufferLock);

        new Thread(producer).start(); // add things to the buffer
        new Thread(consumer1).start(); // remove things from the buffer
        new Thread(consumer2).start(); // remove things from the buffer
    }
}

class MyProducer implements Runnable {
    private List<String> buffer;
    private String color;
    private ReentrantLock bufferLock;

    public MyProducer(List<String> buffer, String color, ReentrantLock bufferLock) {
        this.buffer = buffer;
        this.color = color;
        this.bufferLock = bufferLock;
    }


    public void run() {
        String[] nums = {"1", "2", "3", "4", "5"};
        // add a number to the buffer array list
        for (String num : nums) {
            try {
                System.out.println(color + "Adding... " + num);
                bufferLock.lock();
                buffer.add(num);
                bufferLock.unlock();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Producer was interrupted");
            }
        }
        System.out.println(color + "Adding EOF and exiting...");
        bufferLock.lock();
        buffer.add("EOF");
        bufferLock.unlock();
    }
}

class MyConsumer implements Runnable {
    private List<String> buffer;
    private String color;
    private ReentrantLock bufferLock;

    public MyConsumer(List<String> buffer, String color, ReentrantLock bufferLock) {
        this.buffer = buffer;
        this.color = color;
        this.bufferLock = bufferLock;
    }

    public void run() {
        // remove entries from the buffer
        while (true) {
            bufferLock.lock();
            if (buffer.isEmpty()) {
                bufferLock.unlock();
                continue;
            }
            if (buffer.get(0).equals(EOF)) {
                System.out.println(color + "Exiting");
                bufferLock.unlock();
                break;
            } else {
                System.out.println(color + "Removed " + buffer.remove(0));
            }
            bufferLock.unlock();
        }
    }
}