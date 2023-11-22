package com.drosa.concurrency.drosaConcu.service;

import com.drosa.concurrency.drosaConcu.infra.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransferServiceImpl implements TransferService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public boolean transfer(
            String fromIban, String toIban, long cents) {
        boolean status = true;

        long fromBalance = accountRepository.getBalance(fromIban);

        if (fromBalance >= cents) {
            status &= accountRepository.addBalance(fromIban, (-1) * cents) > 0;

            status &= accountRepository.addBalance(toIban, cents) > 0;
        }

        return status;
    }
}
