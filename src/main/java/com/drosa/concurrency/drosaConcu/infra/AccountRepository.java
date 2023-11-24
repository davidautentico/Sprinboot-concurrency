package com.drosa.concurrency.drosaConcu.infra;

import com.drosa.concurrency.drosaConcu.domain.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query(value = """ 
            SELECT balance FROM account WHERE iban = :iban 
            """, nativeQuery = true)
    Long getBalance(@Param("iban") String iban);

    @Query(value = """
            UPDATE account
            SET balance = balance + :cents
            WHERE iban = :iban
            """,
            nativeQuery = true)
    @Modifying
    @Transactional
    int addBalance(@Param("iban") String iban, @Param("cents") Long cents);
}
