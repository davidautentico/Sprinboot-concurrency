package com.drosa.concurrency.drosaConcu.service;

public interface TransferService {

    boolean transfer(String fromIban, String toIban, long cents);
}
