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

public class GetAverageRatingSteps {
    Restaurant restaurant = new Restaurant(
            "Shila",
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
    Rating averageRating;

    @Given("a restaurant with the following reviews:")
    public void a_restaurant_with_the_following_reviews(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Rating rating = new Rating();
            rating.ambiance = Double.parseDouble(row.get("ambiance"));
            rating.food = Double.parseDouble(row.get("food"));
            rating.service = Double.parseDouble(row.get("service"));
            rating.overall = Double.parseDouble(row.get("overall"));
            Review review = new Review(
                    new User(
                            "ali",
                            "password",
                            "ali@gmail.com",
                            new Address("Iran", "Tehran", "Valiasr"),
                            User.Role.client
                    ),
                    rating,
                    "Nice",
                    LocalDateTime.now()
            );
            restaurant.addReview(review);
        }
    }

    @Given("a restaurant with no reviews")
    public void a_restaurant_with_no_reviews() {
    }

    @When("the average rating is calculated")
    public void the_average_rating_is_calculated() {
        averageRating = restaurant.getAverageRating();
    }

    @Then("the average rating should be:")
    public void the_average_rating_should_be(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            assertEquals(Double.parseDouble(row.get("ambiance")), averageRating.ambiance);
            assertEquals(Double.parseDouble(row.get("food")), averageRating.food);
            assertEquals(Double.parseDouble(row.get("service")), averageRating.service);
            assertEquals(Double.parseDouble(row.get("overall")), averageRating.overall);
        }
    }

}
