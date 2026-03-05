package org.example.sbdbaspectscourse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class ExtractedTransactionService {

    @Transactional
    public boolean checkTransactionActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }
}