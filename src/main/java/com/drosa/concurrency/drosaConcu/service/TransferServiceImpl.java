package com.drosa.concurrency.drosaConcu.service;

import com.drosa.concurrency.drosaConcu.infra.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class TransferServiceImpl implements TransferService {

    final ReadWriteLock lock = new ReentrantReadWriteLock();
    final Lock writeLock = lock.writeLock();

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation =  Isolation.SERIALIZABLE)
    @Retryable()
    public boolean transfer(
            String info, String fromIban, String toIban, long cents, long waitTime) throws InterruptedException {
        boolean status = true;

        try {
            //writeLock.lock();


            System.out.println("Transfer from " + info);

            long fromBalance = accountRepository.getBalance(fromIban);

            if (fromBalance >= cents) {

                System.out.println("Transfer from " + info + " balanceBeforeDecreaseInBBDD =  " + fromBalance);

                status &= accountRepository.addBalance(fromIban, (-1) * cents) > 0;

                //leemos el balance...es visible desde esta transaccion
                long fromBalanceAfterDecrease = accountRepository.getBalance((fromIban));

                System.out.println("Transfer from " + info + " balanceAfterDecreaseInBBDD =  " + fromBalanceAfterDecrease);


                status &= accountRepository.addBalance(toIban, cents) > 0;
            }
        } finally {
            System.out.println("Release LOCK FROM: " + info);
            //writeLock.unlock();
        }

        System.out.println("Saliendo Transfer from " + info);
        return status;
    }
}
