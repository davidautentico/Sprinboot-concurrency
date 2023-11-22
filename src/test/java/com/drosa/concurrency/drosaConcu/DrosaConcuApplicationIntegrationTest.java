package com.drosa.concurrency.drosaConcu;

import com.drosa.concurrency.drosaConcu.domain.Account;
import com.drosa.concurrency.drosaConcu.infra.AccountRepository;
import com.drosa.concurrency.drosaConcu.service.TransferService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;


@ExtendWith(SpringExtension.class)
@SpringBootTest
class DrosaConcuApplicationIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferService transferService;

    @Autowired

    private void cleanDatabase() {
        accountRepository.deleteAll();
    }

    @Test
    public void save_and_read_account() {
        cleanDatabase();

        Account account = new Account();
        account.setBalance(150L);
        account.setIban("0000-0000-0000-0001");

        accountRepository.save(account);

        List<Account> accountList = accountRepository.findAll();

        System.out.println("totalAccounts: " + accountList.size());

        Optional<Account> foundAccount = accountRepository.findById(1L);

        Assertions.assertEquals(account, foundAccount.get());
    }

    @Test
    public void testTransfer(){
        final String iban1 = "0000-0000-0000-0001";
        final String iban2 = "0000-0000-0000-0002";

        cleanDatabase();

        Account account = new Account();
        account.setBalance(100L);
        account.setIban(iban1);

        accountRepository.save(account);

        Account account2 = new Account();
        account2.setBalance(50L);
        account2.setIban(iban2);

        accountRepository.save(account2);


        transferService.transfer(iban1,iban2,25L);

        Assertions.assertEquals(accountRepository.getBalance(iban2), accountRepository.getBalance(iban1));
    }
}