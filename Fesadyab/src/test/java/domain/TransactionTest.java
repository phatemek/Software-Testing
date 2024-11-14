package domain;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {
    @Test
    void testEquals_SameTransactionId() {
        Transaction txn1 = new Transaction();
        Transaction txn2 = new Transaction();
        txn1.transactionId = 1;
        txn2.transactionId = 1;

        assertTrue(txn1.equals(txn2));
    }

    @Test
    void testEquals_DifferentTransactionId() {
        Transaction txn1 = new Transaction();
        Transaction txn2 = new Transaction();
        txn1.transactionId = 1;
        txn2.transactionId = 2;

        assertFalse(txn1.equals(txn2));
    }

    @Test
    void testEquals_NonTransactionObject() {
        Transaction txn = new Transaction();
        txn.transactionId = 1;

        assertFalse(txn.equals("Non-Transaction Object"));
    }

    @Test
    void testEquals_NullObject() {
        Transaction txn = new Transaction();
        txn.transactionId = 1;

        assertFalse(txn.equals(null));
    }
}
