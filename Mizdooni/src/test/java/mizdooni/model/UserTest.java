package mizdooni.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private User client;
    private Restaurant restaurant1;
    private Restaurant restaurant2;
    private Table table;
    private Reservation reservation1;

    @BeforeEach
    void setUp() {
        Address address = new Address("Iran", "Tehran", "Valiasr");
        client = new User("ali", "12345", "ali@gmail.com", address, User.Role.client);
        User manager = new User("mina", "password", "mina@gmail.com", address, User.Role.manager);
        restaurant1 = new Restaurant("test restaurant", manager, "test type", LocalTime.now(), LocalTime.now().plusHours(1000),
                "description", address, "test image");
        restaurant2 = new Restaurant("test restaurant", manager, "test type", LocalTime.now(), LocalTime.now().plusHours(1000),
                "description", address, "test image");
        table = new Table(5, restaurant1.getId(), 10);
        reservation1 = new Reservation(client, restaurant1, table, LocalDateTime.now().minusDays(1));
    }

    @Test
    void testAddReservation_ReservationNumber() {
        client.addReservation(reservation1);
        assertEquals(0, reservation1.getReservationNumber());
    }

    @Test
    void testAddReservation_AddsReservation() {
        client.addReservation(reservation1);
        List<Reservation> reservations = client.getReservations();
        assertEquals(1, reservations.size());
        assertEquals(reservation1, reservations.getFirst());
    }

    @Test
    void testCheckReserved_ReturnsTrue() {
        client.addReservation(reservation1);
        assertTrue(client.checkReserved(restaurant1));
    }

    @Test
    void testCheckReserved_Now() {
        Reservation reservation2 = new Reservation(client, restaurant1, table, LocalDateTime.now());
        client.addReservation(reservation2);
        assertTrue(client.checkReserved(restaurant1));
    }

    @Test
    void testCheckReserved_Cancelled() {
        client.addReservation(reservation1);
        reservation1.cancel();
        assertFalse(client.checkReserved(restaurant1));
    }

    @Test
    void testCheckReserved_Future() {
        Reservation reservation2 = new Reservation(client, restaurant1, table, LocalDateTime.now().plusHours(1));
        client.addReservation(reservation2);
        assertFalse(client.checkReserved(restaurant1));
    }

    @Test
    void testCheckReserved_DifferentRestaurant() {
        client.addReservation(reservation1);
        assertFalse(client.checkReserved(restaurant2));
    }

    @Test
    void testGetReservation_Found() {
        client.addReservation(reservation1);
        Reservation result = client.getReservation(reservation1.getReservationNumber());
        assertEquals(reservation1, result);
    }

    @Test
    void testGetReservation_Cancelled() {
        client.addReservation(reservation1);
        reservation1.cancel();

        Reservation result = client.getReservation(reservation1.getReservationNumber());
        assertNull(result);
    }

    @Test
    void testGetReservation_WrongNumber() {
        client.addReservation(reservation1);
        Reservation result = client.getReservation(reservation1.getReservationNumber() + 1);
        assertNull(result);
    }

    @Test
    void testGetReservation_NoReservation() {
        Reservation result = client.getReservation(reservation1.getReservationNumber());
        assertNull(result);
    }

    @Test
    void testCheckPassword_ValidPassword() {
        assertTrue(client.checkPassword("12345"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234" ,"password", "123456", "1245", "random12345", "12345random"})
    void testCheckPassword_InvalidPasswords(String password) {
        assertFalse(client.checkPassword(password));
    }
}
