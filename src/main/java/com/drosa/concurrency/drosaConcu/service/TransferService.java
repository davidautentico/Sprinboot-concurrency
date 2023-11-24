package com.drosa.concurrency.drosaConcu.service;

public interface TransferService {

    boolean transfer(String info,String fromIban, String toIban, long cents, long waitTime) throws InterruptedException;
}
