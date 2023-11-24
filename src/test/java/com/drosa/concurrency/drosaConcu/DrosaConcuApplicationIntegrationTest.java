package com.drosa.concurrency.drosaConcu;

import com.drosa.concurrency.drosaConcu.domain.Account;
import com.drosa.concurrency.drosaConcu.infra.AccountRepository;
import com.drosa.concurrency.drosaConcu.service.TransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;


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

        assertEquals(account, foundAccount.get());
    }

    @Test
    public void testTransfer() throws InterruptedException {
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


        transferService.transfer("transfer test", iban1, iban2, 25L,0);

        assertEquals(accountRepository.getBalance(iban2), accountRepository.getBalance(iban1));
    }

    @Test
    /***
     * Si el servicio no tiene @Transactional, el transfer del service no es atómico y da resultados insospechados
     * @tRANSACTIONAL hace un lock en los updates -> timeout si otra transacción intenta cogerla, si se añade retryable
     * + ISOLATION.SERIALIZABLE, funciona porque al final se van liberando
     * READ_COMMITTED: EVITA LOS LOSTUPDATE, sólo leera lo commiteado y no operaciones intermedias
     * READ_UNCOMMITED: lee OPERACIONES NO COMMITEADAS POR OTRAS TRANSACCIONES
     * lO MEJOR Y MÁS LESIVO ES un ReadwriteLock a nivel de método para evitar estos problemas si hay escrituras
     */
    public void testParallelTransfer() throws InterruptedException {
        final String iban1 = "Alice-123";
        final String iban2 = "Bob-456";

        cleanDatabase();

        Account account = new Account();
        account.setBalance(10L);
        account.setIban(iban1);

        accountRepository.save(account);

        Account account2 = new Account();
        account2.setBalance(0L);
        account2.setIban(iban2);

        accountRepository.save(account2);

        int threadCount = 30;
        assertEquals(10L, accountRepository.getBalance("Alice-123"));
        assertEquals(0L, accountRepository.getBalance("Bob-456"));

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            int waitTime;
            if (i == 1) waitTime = 0;
            else {
                waitTime = 5000;
            }
            new Thread(() -> {
                try {
                    startLatch.await();

                    transferService.transfer("thread " + finalI,
                            "Alice-123", "Bob-456", 5L, waitTime
                    );
                } catch (Exception e) {
                    System.out.println("Transfer failed, Msg: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }
        startLatch.countDown();
        endLatch.await();

        Long aliceBalance = accountRepository.getBalance("Alice-123");
        Long bobBalance = accountRepository.getBalance("Bob-456");

        System.out.println(
                "Alice's balance = " + aliceBalance
        );
        System.out.println(
                "Bob's balance = " + bobBalance
        );

        assertEquals(0L, aliceBalance);
        assertEquals(10L, bobBalance);
    }
}