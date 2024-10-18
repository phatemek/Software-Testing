package mizdooni.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {
    private Table table;
    private LocalDateTime reservationTime = LocalDateTime.now().minusDays(1);
    private Reservation reservation1;

    @BeforeEach
    void setUp() {
        table = new Table(1, 101, 4);
        Address address = new Address("Iran", "Tehran", "Valiasr");
        User client = new User("ali", "12345", "ali@gmail.com", address, User.Role.client);
        User manager = new User("mina", "password", "mina@gmail.com", address, User.Role.manager);
        Restaurant restaurant1 = new Restaurant("test restaurant", manager, "test type", LocalTime.now(), LocalTime.now().plusHours(1000),
                "description", address, "test image");
        reservation1 = new Reservation(client, restaurant1, table, reservationTime);
    }

    @Test
    void testAddReservation() {
        table.addReservation(reservation1);
        List<Reservation> reservations = table.getReservations();
        assertEquals(1, reservations.size());
        assertEquals(reservation1, reservations.getFirst());
    }

    @Test
    void testIsReserved_ReturnsTrue() {
        table.addReservation(reservation1);
        assertTrue(table.isReserved(reservationTime));
    }

    @Test
    void testIsReserved_Cancelled() {
        table.addReservation(reservation1);
        reservation1.cancel();
        assertFalse(table.isReserved(reservationTime));
    }

    @Test
    void testIsReserved_LaterTime() {
        table.addReservation(reservation1);
        assertFalse(table.isReserved(reservationTime.plusHours(1)));
    }

    @Test
    void testIsReserved_EarlierTime() {
        table.addReservation(reservation1);
        assertFalse(table.isReserved(reservationTime.minusHours(1)));
    }

    @Test
    void testIsReserved_NoReservation() {
        assertFalse(table.isReserved(reservationTime));
    }

    @Test
    void testSetTableNumber() {
        table.setTableNumber(2);
        assertEquals(2, table.getTableNumber());
    }
}
