package domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionEngineTest {
    private TransactionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TransactionEngine();
    }

    @Test
    void testGetAverageTransactionAmountByAccount_NoTransactions() {
        int result = engine.getAverageTransactionAmountByAccount(1);
        assertEquals(0, result);
    }

    @Test
    void testGetAverageTransactionAmountByAccount_WithTransactions() {
        Transaction txn1 = new Transaction();
        txn1.accountId = 1;
        txn1.amount = 100;

        Transaction txn2 = new Transaction();
        txn2.accountId = 1;
        txn2.amount = 300;

        engine.transactionHistory.add(txn1);
        engine.transactionHistory.add(txn2);

        int result = engine.getAverageTransactionAmountByAccount(1);
        assertEquals(200, result);
    }

    @Test
    void testGetAverageTransactionAmountByAccount_AnotherAccount() {
        Transaction txn1 = new Transaction();
        txn1.accountId = 1;
        txn1.amount = 100;

        Transaction txn2 = new Transaction();
        txn2.accountId = 1;
        txn2.amount = 300;

        engine.transactionHistory.add(txn1);
        engine.transactionHistory.add(txn2);

        int result = engine.getAverageTransactionAmountByAccount(2);
        assertEquals(0, result);
    }

    @Test
    void testGetTransactionPatternAboveThreshold_EmptyHistory() {
        int result = engine.getTransactionPatternAboveThreshold(100);
        assertEquals(0, result);
    }

    @Test
    void testGetTransactionPatternAboveThreshold_NoPattern() {
        Transaction txn1 = new Transaction();
        txn1.transactionId = 1;
        txn1.amount = 100;

        Transaction txn2 = new Transaction();
        txn2.transactionId = 2;
        txn2.amount = 300;

        Transaction txn3 = new Transaction();
        txn3.transactionId = 3;
        txn3.amount = 400;

        engine.transactionHistory.add(txn1);
        engine.transactionHistory.add(txn2);
        engine.transactionHistory.add(txn3);

        int result = engine.getTransactionPatternAboveThreshold(50);
        assertEquals(0, result);
    }

    @Test
    void testGetTransactionPatternAboveThreshold_WithPattern() {
        Transaction txn1 = new Transaction();
        txn1.transactionId = 1;
        txn1.amount = 200;

        Transaction txn2 = new Transaction();
        txn2.transactionId = 2;
        txn2.amount = 1200;

        Transaction txn3 = new Transaction();
        txn3.transactionId = 3;
        txn3.amount = 2200;

        engine.transactionHistory.add(txn1);
        engine.transactionHistory.add(txn2);
        engine.transactionHistory.add(txn3);

        int result = engine.getTransactionPatternAboveThreshold(500);
        assertEquals(1000, result);  // Expected pattern difference
    }

    @Test
    void testGetTransactionPatternAboveThreshold_BelowThreshold() {
        Transaction txn1 = new Transaction();
        txn1.transactionId = 1;
        txn1.amount = 200;

        Transaction txn2 = new Transaction();
        txn2.transactionId = 2;
        txn2.amount = 300;

        Transaction txn3 = new Transaction();
        txn3.transactionId = 3;
        txn3.amount = 400;

        engine.transactionHistory.add(txn1);
        engine.transactionHistory.add(txn2);
        engine.transactionHistory.add(txn3);

        int result = engine.getTransactionPatternAboveThreshold(500);
        assertEquals(0, result);
    }

    @Test
    void testDetectFraudulentTransaction_NotDebitTransaction() {
        Transaction txn = new Transaction();
        txn.accountId = 1;
        txn.amount = 200;
        txn.isDebit = false;

        int result = engine.detectFraudulentTransaction(txn);
        assertEquals(0, result);
    }

    @Test
    void testDetectFraudulentTransaction_DebitTransactionAboveThreshold() {
        Transaction txn1 = new Transaction();
        txn1.accountId = 1;
        txn1.amount = 100;

        Transaction txn2 = new Transaction();
        txn2.accountId = 1;
        txn2.amount = 300;
        txn2.isDebit = true;

        engine.transactionHistory.add(txn1);

        int result = engine.detectFraudulentTransaction(txn2);
        assertEquals(100, result);
    }

    @Test
    void testDetectFraudulentTransaction_DebitTransactionBelowThreshold() {
        Transaction txn1 = new Transaction();
        txn1.accountId = 1;
        txn1.amount = 100;

        Transaction txn2 = new Transaction();
        txn2.accountId = 1;
        txn2.amount = 100;
        txn2.isDebit = true;

        engine.transactionHistory.add(txn1);

        int result = engine.detectFraudulentTransaction(txn2);
        assertEquals(0, result);
    }

    @Test
    void testAddTransactionAndDetectFraud_TransactionAlreadyExists() {
        Transaction txn = new Transaction();
        txn.transactionId = 1;

        engine.transactionHistory.add(txn);

        int result = engine.addTransactionAndDetectFraud(txn);
        assertEquals(0, result);
    }

    @Test
    void testAddTransactionAndDetectFraud_NoFraudAndNoPattern() {
        Transaction txn = new Transaction();
        txn.transactionId = 1;
        txn.accountId = 1;
        txn.amount = 500;
        txn.isDebit = false;

        int result = engine.addTransactionAndDetectFraud(txn);
        assertEquals(0, result);
    }

    @Test
    void testAddTransactionAndDetectFraud_FraudDetected() {
        Transaction txn1 = new Transaction();
        txn1.transactionId = 1;
        txn1.accountId = 1;
        txn1.amount = 100;

        Transaction txn2 = new Transaction();
        txn2.transactionId = 2;
        txn2.accountId = 1;
        txn2.amount = 400;
        txn2.isDebit = true;

        engine.transactionHistory.add(txn1);
        int result = engine.addTransactionAndDetectFraud(txn2);

        assertEquals(200, result);
    }

    @Test
    void testAddTransactionAndDetectFraud_PatternDetected() {
        Transaction txn1 = new Transaction();
        txn1.transactionId = 1;
        txn1.accountId = 1;
        txn1.amount = 200;

        Transaction txn2 = new Transaction();
        txn2.transactionId = 2;
        txn2.accountId = 1;
        txn2.amount = 1200;

        Transaction txn3 = new Transaction();
        txn3.transactionId = 3;
        txn3.accountId = 1;
        txn3.amount = 2200;

        engine.transactionHistory.add(txn1);
        engine.transactionHistory.add(txn2);
        int result = engine.addTransactionAndDetectFraud(txn3);

        assertEquals(1000, result);
    }
}
