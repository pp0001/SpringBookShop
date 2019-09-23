package com.sap.bookshop.config;

import com.sap.cds.transaction.TransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class SpringTransactionManager implements TransactionManager {
    private PlatformTransactionManager ptm;

    public SpringTransactionManager(PlatformTransactionManager ptm) {
        this.ptm = ptm;
    }

    @Override
    public boolean isActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    @Override
    public void setRollbackOnly() {
    }
}