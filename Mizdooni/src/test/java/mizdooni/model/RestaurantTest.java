package mizdooni.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RestaurantTest {
    private Restaurant restaurant;
    private User manager;
    private Address address;

    @BeforeEach
    void setUp() {
        address = new Address("Iran", "Tehran", "Valiasr");
        manager = new User("ali", "12345", "ali@gmail.com", address, User.Role.manager);
        restaurant = new Restaurant(
                "test restaurant",
                manager, "test type",
                LocalTime.now(),
                LocalTime.now().plusHours(10),
                "description",
                address,
                "test image"
        );
    }

    @Test
    void givenTables_whenTablesAreAddedToRestaurant_thenTablesMustBeUpdated() {
        Table table1 = new Table(0, restaurant.getId(), 4);
        Table table2 = new Table(0, restaurant.getId(), 4);

        restaurant.addTable(table1);
        restaurant.addTable(table2);

        List<Table> tables = restaurant.getTables();

        assertEquals(2, tables.size(), "There should be 2 tables added.");
        assertEquals(1, table1.getTableNumber(), "First table should have number 1.");
        assertEquals(2, table2.getTableNumber(), "Second table should have number 2.");
    }

    @Test
    void givenTables_whenTablesAreAddedToRestaurant_thenTableMustBeReturned() {
        Table table1 = new Table(0, restaurant.getId(), 4);
        Table table2 = new Table(0, restaurant.getId(), 2);


        restaurant.addTable(table1);
        restaurant.addTable(table2);

        assertEquals(4, restaurant.getTable(1).getSeatsNumber(), "Restaurant first table should have 4 seats.");
        assertEquals(2, restaurant.getTable(2).getSeatsNumber(), "Restaurant second table should have 2 seats.");
        assertEquals(table1, restaurant.getTable(1), "Table1 number should be 1 when added to restaurant.");
        assertEquals(table2, restaurant.getTable(2), "Table2 number should be 2 when added to restaurant.");
    }

    @Test
    void whenThereIsNoTable_thenMaximumSeatsShouldReturnZero(){
        assertEquals(0, restaurant.getMaxSeatsNumber(), "Maximum seats numbers should be equal to zero");
    }

    @Test
    void givenTables_whenTablesAreAdded_thenMaximumSeatsShouldBeUpdated(){
        Table table1 = new Table(0, restaurant.getId(), 4);
        Table table2 = new Table(0, restaurant.getId(), 2);
        Table table3 = new Table(0, restaurant.getId(), 6);

        restaurant.addTable(table1);
        restaurant.addTable(table2);
        restaurant.addTable(table3);

        assertEquals(6, restaurant.getMaxSeatsNumber());
    }

    @Test
    void whenThereIsNoReview_thenRestaurantRatingShouldBeZero() {
        assertEquals(0, restaurant.getAverageRating().ambiance);
        assertEquals(0, restaurant.getAverageRating().food);
        assertEquals(0, restaurant.getAverageRating().service);
        assertEquals(0, restaurant.getAverageRating().overall);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2, 3, 2.5",
            "2.7, 3.4, 5, 3",
            "3, 4, 4,5, 3.75",
            "2, 2.2, 3.2, 3"
    })
    void givenReview_whenThereIsNoOldReviewAndReviewAdded_thenRestaurantRatingShouldBeUpdated(
            double ambiance,
            double food,
            double service,
            double overall
    ) {
        User client = new User("mina", "password", "mina@gmail.com", address, User.Role.client);
        Rating rating = new Rating();
        rating.ambiance = ambiance;
        rating.food = food;
        rating.service = service;
        rating.overall = overall;
        Review review = new Review(
                client,
                rating,
                "Nice",
                LocalDateTime.now()
        );
        restaurant.addReview(review);
        assertEquals(ambiance, restaurant.getAverageRating().ambiance);
        assertEquals(food, restaurant.getAverageRating().food);
        assertEquals(service, restaurant.getAverageRating().service);
        assertEquals(overall, restaurant.getAverageRating().overall);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2, 3, 2.5",
            "2.7, 3.4, 5, 3",
            "3, 4, 4,5, 3.75",
            "2, 2.2, 3.2, 3"
    })
    void givenReview_whenThereIsOldReviewFromSameUserAndReviewAdded_thenRestaurantRatingShouldBeUpdated(
            double ambiance,
            double food,
            double service,
            double overall
    ) {
        User client = new User("mina", "password", "mina@gmail.com", address, User.Role.client);
        Rating rating1 = new Rating();
        rating1.ambiance = 2;
        rating1.food = 3;
        rating1.service = 4;
        rating1.overall = 3.5;
        Review review1 = new Review(
                client,
                rating1,
                "Nice",
                LocalDateTime.now()
        );
        restaurant.addReview(review1);

        Rating rating2 = new Rating();
        rating2.ambiance = ambiance;
        rating2.food = food;
        rating2.service = service;
        rating2.overall = overall;
        Review review2 = new Review(
                client,
                rating2,
                "Nice",
                LocalDateTime.now()
        );
        restaurant.addReview(review2);
        assertEquals(ambiance, restaurant.getAverageRating().ambiance);
        assertEquals(food, restaurant.getAverageRating().food);
        assertEquals(service, restaurant.getAverageRating().service);
        assertEquals(overall, restaurant.getAverageRating().overall);
    }

    @ParameterizedTest
    @CsvSource({
            "4.5, 1.6, 2.2, 1.2, 3.5, 3.4, 3.7, 2.7",
            "1.9, 1.2, 2.7, 4.3, 2.4, 3.3, 2.3, 1.6",
            "3.5, 4.4, 2.0, 2.0, 3.8, 2.6, 3.3, 1.8",
            "4.4, 4.3, 1.8, 2.6, 5.0, 2.5, 4.1, 1.1"
    })
    void givenTwoReviews_whenTwoReviewsAreAdded_thenRestaurantRatingShouldBeUpdated(
            double ambiance1,
            double food1,
            double service1,
            double overall1,
            double ambiance2,
            double food2,
            double service2,
            double overall2
    ) {
        User client1 = new User("mina", "password", "mina@gmail.com", address, User.Role.client);
        Rating rating1 = new Rating();
        rating1.ambiance = ambiance1;
        rating1.food = food1;
        rating1.service = service1;
        rating1.overall = overall1;
        Review review1 = new Review(
                client1,
                rating1,
                "Nice",
                LocalDateTime.now()
        );
        restaurant.addReview(review1);

        User client2 = new User("masoud", "password", "masoud@gmail.com", address, User.Role.client);
        Rating rating2 = new Rating();
        rating2.ambiance = ambiance2;
        rating2.food = food2;
        rating2.service = service2;
        rating2.overall = overall2;
        Review review2 = new Review(
                client2,
                rating2,
                "Very good",
                LocalDateTime.now()
        );
        restaurant.addReview(review2);
        assertEquals((ambiance1 + ambiance2) / 2, restaurant.getAverageRating().ambiance);
        assertEquals((food1 + food2) / 2, restaurant.getAverageRating().food);
        assertEquals((service1 + service2) / 2, restaurant.getAverageRating().service);
        assertEquals((overall1 + overall2) / 2, restaurant.getAverageRating().overall);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2, 3, 2.5",
            "2.7, 3.4, 5, 3",
            "3, 4, 4,5, 3.75",
            "2, 2.2, 3.2, 3"
    })
    void givenReview_whenThereIsNoOldReviewAndReviewAdded_thenRestaurantStarsShouldBeUpdated(
            double ambiance,
            double food,
            double service,
            double overall
    ) {
        User client = new User("mina", "password", "mina@gmail.com", address, User.Role.client);
        Rating rating = new Rating();
        rating.ambiance = ambiance;
        rating.food = food;
        rating.service = service;
        rating.overall = overall;
        Review review = new Review(
                client,
                rating,
                "Nice",
                LocalDateTime.now()
        );
        restaurant.addReview(review);
        assertEquals(rating.getStarCount(), restaurant.getStarCount());
    }
}
