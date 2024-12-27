package mizdooni.stepDefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mizdooni.model.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddReservationsSteps {
    User client;
    Restaurant restaurant;

    @Given("a user with the username {string} and email {string}")
    public void a_user_with_the_username_and_email(String username, String email) {
        client = new User(
                username,
                "12345",
                email,
                new Address("Iran", "Tehran", "Valiasr"),
                User.Role.client
        );
    }

    @Given("a restaurant with the name {string}")
    public void a_restaurant_with_the_name(String name) {
        restaurant = new Restaurant(
                name,
                new User(
                        "mina",
                        "password",
                        "mina@gmail.com",
                        new Address("Iran", "Tehran", "Valiasr"),
                        User.Role.manager
                ),
                "test type",
                LocalTime.now(),
                LocalTime.now().plusHours(1000),
                "description",
                new Address("Iran", "Tehran", "Valiasr"),
                "test image"
        );
    }

    @When("the following reservations are added:")
    public void the_following_reservations_are_added(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Reservation reservation = new Reservation(
                    client,
                    restaurant,
                    new Table(Integer.parseInt(row.get("tableNumber")), restaurant.getId(), 10),
                    LocalDateTime.parse(row.get("datetime"))
            );
            client.addReservation(reservation);
        }
    }

    @Then("the user should have {int} reservations")
    public void the_user_should_have_reservations(Integer reservationCount) {
        List<Reservation> reservations = client.getReservations();
        assertEquals(reservationCount, reservations.size());
    }

    @Then("the reservation details should include:")
    public void the_reservation_details_should_include(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        List<Reservation> actualReservations = client.getReservations();

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> expected = rows.get(i);
            Reservation actual = actualReservations.get(i);
            assertEquals(Integer.parseInt(expected.get("reservationNumber")), actual.getReservationNumber());
            assertEquals(expected.get("restaurantName"), actual.getRestaurant().getName());
            assertEquals(Integer.parseInt(expected.get("tableNumber")), actual.getTable().getTableNumber());
            assertEquals(LocalDateTime.parse(expected.get("datetime")), actual.getDateTime());

        }
    }

    @Then("the reservation numbers should be sequential")
    public void the_reservation_numbers_should_be_sequential() {
        List<Reservation> actualReservations = client.getReservations();
        int expectedReservationNumber = 0;
        for (Reservation reservation : actualReservations){
            assertEquals(expectedReservationNumber, reservation.getReservationNumber());
            expectedReservationNumber++;
        }
    }
}
