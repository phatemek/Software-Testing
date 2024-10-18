package mozdooni.model;

import mizdooni.model.*;
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
    private Restaurant restaurant;
    private Restaurant secondRestaurant;
    private Table table;
    private Reservation correctReservation;

    @BeforeEach
    void setUp() {
        Address address = new Address("Iran", "Tehran", "Valiasr");
        client = new User("ali", "12345", "ali@gmail.com", address, User.Role.client);
        User manager = new User("mina", "password", "mina@gmail.com", address, User.Role.manager);
        restaurant = new Restaurant("test restaurant", manager, "test type", LocalTime.now(), LocalTime.now().plusHours(1000),
                "description", address, "test image");
        secondRestaurant = new Restaurant("test restaurant", manager, "test type", LocalTime.now(), LocalTime.now().plusHours(1000),
                "description", address, "test image");
        table = new Table(5, restaurant.getId(), 10);
        correctReservation = new Reservation(client, restaurant, table, LocalDateTime.now().minusDays(1));
    }

    @Test
    void testAddReservation_ReservationNumber() {
        client.addReservation(correctReservation);
        assertEquals(0, correctReservation.getReservationNumber());
    }

    @Test
    void testAddReservation_AddsReservation() {
        client.addReservation(correctReservation);

        List<Reservation> reservations = client.getReservations();
        assertEquals(1, reservations.size());
        assertEquals(correctReservation, reservations.getFirst());
    }

    @Test
    void testCheckReserved_ReturnsTrue() {
        client.addReservation(correctReservation);
        assertTrue(client.checkReserved(restaurant));
    }

    @Test
    void testCheckReserved_Now() {
        Reservation reservation = new Reservation(client, restaurant, table, LocalDateTime.now());
        client.addReservation(reservation);
        assertTrue(client.checkReserved(restaurant));
    }

    @Test
    void testCheckReserved_Cancelled() {
        client.addReservation(correctReservation);
        correctReservation.cancel();
        assertFalse(client.checkReserved(restaurant));
    }

    @Test
    void testCheckReserved_Future() {
        Reservation reservation = new Reservation(client, restaurant, table, LocalDateTime.now().plusHours(1));
        client.addReservation(reservation);
        assertFalse(client.checkReserved(restaurant));
    }

    @Test
    void testCheckReserved_DifferentRestaurant() {
        client.addReservation(correctReservation);
        assertFalse(client.checkReserved(secondRestaurant));
    }

    @Test
    void testGetReservation_Found() {
        client.addReservation(correctReservation);
        Reservation result = client.getReservation(correctReservation.getReservationNumber());
        assertEquals(correctReservation, result);
    }

    @Test
    void testGetReservation_Cancelled() {
        client.addReservation(correctReservation);
        correctReservation.cancel();

        Reservation result = client.getReservation(correctReservation.getReservationNumber());
        assertNull(result);
    }

    @Test
    void testGetReservation_WrongNumber() {
        client.addReservation(correctReservation);
        Reservation result = client.getReservation(correctReservation.getReservationNumber() + 1);
        assertNull(result);
    }

    @Test
    void testGetReservation_NoReservation() {
        Reservation result = client.getReservation(correctReservation.getReservationNumber());
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
