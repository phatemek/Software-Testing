package mizdooni.controllers;

import mizdooni.exceptions.*;
import mizdooni.model.*;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.ReservationService;
import mizdooni.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReservationControllerTest {
    Restaurant restaurant;

    @InjectMocks
    private ReservationController reservationController;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ReservationService reservationService;

    public static Stream<Map<String, String>> provideMissingAddReservationParams() {
        return Stream.of(
                new HashMap<String, String>() {{
                    put("people", "4");
                }},
                new HashMap<String, String>() {{
                    put("datetime", "2024-11-01 10:10");
                }},
                new HashMap<String, String>() {{
                    put("nothing", "nothing");
                }}

        );
    }

    public static Stream<Map<String, String>> provideInvalidTypeAddReservationParams() {
        return Stream.of(
                new HashMap<String, String>() {{
                    put("people", "Very Good");
                    put("datetime", "2024-11-01 10:10");
                }},
                new HashMap<String, String>() {{
                    put("people", "4");
                    put("datetime", "Very Good");
                }},
                new HashMap<String, String>() {{
                    put("people", "Very Bad");
                    put("datetime", "Very Bad");
                }}

        );
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Address address = new Address("Iran", "Tehran", "Valiasr");
        User manager = new User("mina", "password", "mina@gmail.com", address, User.Role.manager);
        restaurant = new Restaurant("test restaurant", manager, "test type", LocalTime.now(), LocalTime.now().plusHours(1000),
                "description", address, "test image");
    }

    @Test
    void testGetReservations_whenReturnsOkResponse() throws UserNotManager, TableNotFound, InvalidManagerRestaurant, RestaurantNotFound {
        int restaurantId = restaurant.getId();
        Table table = new Table(1, restaurantId, 6);
        Address address = new Address("Iran", "Tehran", "Valiasr");
        User customer = new User("ali", "password", "ali@gmail.com", address, User.Role.client);
        String datetime = "2024-11-01T10:15:30";
        String date = "2024-11-01";
        LocalDateTime localDateTime = LocalDateTime.parse(datetime);
        Reservation reservation = new Reservation(customer, restaurant, table, localDateTime);
        List<Reservation> reservations = List.of(reservation);

        when(reservationService.getReservations(restaurantId, table.getTableNumber(), localDateTime.toLocalDate())).thenReturn(reservations);
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

        Response response = reservationController.getReservations(restaurantId, table.getTableNumber(), date);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("restaurant table reservations", response.getMessage());
        assertEquals(reservations, response.getData());
    }

    @Test
    void testGetReservations_whenDateIsInvalid() {
        int restaurantId = restaurant.getId();
        Table table = new Table(1, restaurantId, 6);
        String date = "invalid-date";

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getReservations(restaurantId, table.getTableNumber(), date));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ControllerUtils.PARAMS_BAD_TYPE, exception.getMessage());
    }

    @Test
    void testGetReservations_whenRestaurantIsInvalid() {
        int restaurantId = 999;
        Table table = new Table(1, restaurantId, 6);
        String date = "2024-11-01";

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getReservations(restaurantId, table.getTableNumber(), date));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
    }

    @Test
    void testGetReservations_whenTableIsInvalid() throws UserNotManager, TableNotFound, InvalidManagerRestaurant, RestaurantNotFound {
        int restaurantId = restaurant.getId();
        int tableId = 999;
        String date = "2024-11-01";

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        when(reservationService.getReservations(restaurantId, tableId, LocalDate.parse(date))).thenThrow(new TableNotFound());

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getReservations(restaurantId, tableId, date));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Table not found.", exception.getMessage());
    }


    @Test
    void testGetCustomerReservations_whenReturnsOkResponse() throws UserNotFound, UserNoAccess {
        Table table = new Table(1, restaurant.getId(), 6);
        Address address = new Address("Iran", "Tehran", "Valiasr");
        User customer = new User("ali", "password", "ali@gmail.com", address, User.Role.client);
        String datetime = "2024-11-01T10:15:30";
        LocalDateTime localDateTime = LocalDateTime.parse(datetime);
        Reservation reservation = new Reservation(customer, restaurant, table, localDateTime);
        List<Reservation> reservations = List.of(reservation);
        int customerId = customer.getId();

        when(reservationService.getCustomerReservations(customerId)).thenReturn(reservations);

        Response response = reservationController.getCustomerReservations(customerId);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("user reservations", response.getMessage());
        assertEquals(reservations, response.getData());
    }

    @Test
    void testGetCustomerReservations_whenUserNotFound() throws UserNotFound, UserNoAccess {
        int customerId = 999;

        when(reservationService.getCustomerReservations(customerId)).thenThrow(new UserNotFound());

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getCustomerReservations(customerId));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    void testGetCustomerReservations_whenUserHasNoAccess() throws UserNotFound, UserNoAccess {
        int customerId = 999;

        when(reservationService.getCustomerReservations(customerId)).thenThrow(new UserNoAccess());

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getCustomerReservations(customerId));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("User has no access to this resource.", exception.getMessage());
    }

    @Test
    void testGetAvailableTimes_whenReturnsOkResponse() throws DateTimeInThePast, RestaurantNotFound, BadPeopleNumber {
        int restaurantId = restaurant.getId();
        int peopleCount = 4;
        String datetime = "2024-11-01T10:15:30";
        LocalDateTime localDateTime = LocalDateTime.parse(datetime);
        List<LocalTime> availableTimes = List.of(localDateTime.toLocalTime());
        LocalDate date = localDateTime.toLocalDate();

        when(reservationService.getAvailableTimes(restaurantId, peopleCount, date)).thenReturn(availableTimes);
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

        Response response = reservationController.getAvailableTimes(restaurantId, peopleCount, date.toString());

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("available times", response.getMessage());
        assertEquals(availableTimes, response.getData());
    }

    @Test
    void testGetAvailableTimes_whenDateTimeIsInThePast() throws DateTimeInThePast, RestaurantNotFound, BadPeopleNumber {
        int restaurantId = restaurant.getId();
        int peopleCount = 4;
        String datetime = "2024-11-01T10:15:30";
        LocalDateTime localDateTime = LocalDateTime.parse(datetime);
        LocalDate date = localDateTime.toLocalDate();

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        when(reservationService.getAvailableTimes(restaurantId, peopleCount, date)).thenThrow(new DateTimeInThePast());

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getAvailableTimes(restaurantId, peopleCount, date.toString()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Date time is before current time.", exception.getMessage());
    }

    @Test
    void testGetAvailableTimes_whenRestaurantNotFound() {
        int restaurantId = restaurant.getId();
        int peopleCount = 4;
        String datetime = "2024-11-01T10:15:30";
        LocalDateTime localDateTime = LocalDateTime.parse(datetime);
        LocalDate date = localDateTime.toLocalDate();

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getAvailableTimes(restaurantId, peopleCount, date.toString()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
    }

    @Test
    void testGetAvailableTimes_whenBadPeopleNumber() throws DateTimeInThePast, RestaurantNotFound, BadPeopleNumber {
        int restaurantId = restaurant.getId();
        int peopleCount = -4;
        String datetime = "2024-11-01T10:15:30";
        LocalDateTime localDateTime = LocalDateTime.parse(datetime);
        LocalDate date = localDateTime.toLocalDate();

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        when(reservationService.getAvailableTimes(restaurantId, peopleCount, date)).thenThrow(new BadPeopleNumber());

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.getAvailableTimes(restaurantId, peopleCount, date.toString()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("People number should be positive.", exception.getMessage());
    }

    @Test
    void testAddReservation_whenReturnsOkResponse() throws UserNotFound, DateTimeInThePast, TableNotFound, ReservationNotInOpenTimes, ManagerReservationNotAllowed, RestaurantNotFound, InvalidWorkingTime {
        int restaurantId = restaurant.getId();
        int people = 4;
        Table table = new Table(1, restaurantId, 6);
        Address address = new Address("Iran", "Tehran", "Valiasr");
        User customer = new User("ali", "password", "ali@gmail.com", address, User.Role.client);
        String datetime = "2024-11-01 10:10";
        LocalDateTime localDateTime = LocalDateTime.parse(datetime, ControllerUtils.DATETIME_FORMATTER);
        Reservation reservation = new Reservation(customer, restaurant, table, localDateTime);
        Map<String, String> params = new HashMap<>();
        params.put("people", "4");
        params.put("datetime", datetime);

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        when(reservationService.reserveTable(restaurantId, people, localDateTime)).thenReturn(reservation);

        Response response = reservationController.addReservation(restaurantId, params);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("reservation done", response.getMessage());
        assertEquals(reservation, response.getData());
    }

    @Test
    void testAddReservation_whenRestaurantNotFound() {
        int restaurantId = restaurant.getId();
        Map<String, String> params = new HashMap<>();
        params.put("people", "4");
        params.put("datetime", "2024-11-01 10:10");

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.addReservation(restaurantId, params));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideMissingAddReservationParams")
    void testAddReservation_whenParamsAreMissing(Map<String, String> params) {
        int restaurantId = restaurant.getId();

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.addReservation(restaurantId, params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ControllerUtils.PARAMS_MISSING, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidTypeAddReservationParams")
    void testAddReservation_whenParamsAreInvalid(Map<String, String> params) {
        int restaurantId = restaurant.getId();

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.addReservation(restaurantId, params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ControllerUtils.PARAMS_BAD_TYPE, exception.getMessage());
    }

    @Test
    void testCancelReservation_whenReturnsOkResponse() throws ReservationCannotBeCancelled, UserNotFound, ReservationNotFound {
        int reservationNumber = 1;
        doNothing().when(reservationService).cancelReservation(reservationNumber);

        Response response = reservationController.cancelReservation(reservationNumber);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("reservation cancelled", response.getMessage());
    }

    @Test
    void testCancelReservation_whenReservationNotFound() throws ReservationCannotBeCancelled, UserNotFound, ReservationNotFound {
        int reservationNumber = 1;
        doThrow(new ReservationNotFound()).when(reservationService).cancelReservation(reservationNumber);

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.cancelReservation(reservationNumber));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Reservation not found.", exception.getMessage());
    }

    @Test
    void testCancelReservation_whenReservationCannotBeCancelled() throws ReservationCannotBeCancelled, UserNotFound, ReservationNotFound {
        int reservationNumber = 1;
        doThrow(new ReservationCannotBeCancelled()).when(reservationService).cancelReservation(reservationNumber);

        ResponseException exception = assertThrows(ResponseException.class, () -> reservationController.cancelReservation(reservationNumber));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Reservation cannot be cancelled.", exception.getMessage());
    }
}
