package mizdooni.stepDefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mizdooni.model.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddReviewSteps {
    Restaurant restaurant;
    List <User> users = new ArrayList<>();

    @Given("a restaurant named {string} managed by {string}")
    public void a_restaurant_named_managed_by(String restaurantName, String managerName) {
        restaurant = new Restaurant(
                restaurantName,
                new User(
                        managerName,
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

    @When("a new review is added by {string} with the following details:")
    public void a_new_review_is_added_by_with_the_following_details(String username, io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        User reviewer = null;
        for (User user : users)
            if (user.getUsername().equals(username))
                reviewer = user;

        if (reviewer == null){
            reviewer = new User(
                    username,
                    "password",
                    "user@gmail.com",
                    new Address("Iran", "Tehran", "Valiasr"),
                    User.Role.client
            );
            users.add(reviewer);
        }

        for (Map<String, String> row : rows) {
            Rating rating = new Rating();
            rating.ambiance = Double.parseDouble(row.get("ambiance"));
            rating.food = Double.parseDouble(row.get("food"));
            rating.service = Double.parseDouble(row.get("service"));
            rating.overall = Double.parseDouble(row.get("overall"));
            Review review = new Review(
                    reviewer,
                    rating,
                    "Nice",
                    LocalDateTime.now()
            );
            restaurant.addReview(review);
        }
    }

    @Then("the restaurant should have {int} reviews")
    public void the_restaurant_should_have_reviews(Integer reviewsCount) {
        assertEquals(restaurant.getReviews().size(), reviewsCount);
    }

    @Then("the restaurants average rating should be:")
    public void the_restaurants_average_rating_should_be(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            assertEquals(Double.parseDouble(row.get("ambiance")), restaurant.getAverageRating().ambiance);
            assertEquals(Double.parseDouble(row.get("food")), restaurant.getAverageRating().food);
            assertEquals(Double.parseDouble(row.get("service")), restaurant.getAverageRating().service);
            assertEquals(Double.parseDouble(row.get("overall")), restaurant.getAverageRating().overall);
        }
    }


    @Then("the reviews should include:")
    public void the_reviews_should_include(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            boolean reviewIsVisited = false;
            for (Review review : restaurant.getReviews()) {
                if (review.getUser().getUsername().equals(row.get("username"))) {
                    assertEquals(Double.parseDouble(row.get("ambiance")), review.getRating().ambiance);
                    assertEquals(Double.parseDouble(row.get("food")), review.getRating().food);
                    assertEquals(Double.parseDouble(row.get("service")), review.getRating().service);
                    assertEquals(Double.parseDouble(row.get("overall")), review.getRating().overall);
                    reviewIsVisited = true;
                }
            }
            assertTrue(reviewIsVisited);
        }
    }

}
