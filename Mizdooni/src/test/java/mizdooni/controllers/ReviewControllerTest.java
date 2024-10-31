package mizdooni.controllers;

import mizdooni.exceptions.*;
import mizdooni.model.*;
import mizdooni.response.PagedList;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.RestaurantService;
import mizdooni.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewControllerTest {
    Restaurant restaurant;

    @InjectMocks
    private ReviewController reviewController;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Address address = new Address("Iran", "Tehran", "Valiasr");
        User manager = new User("mina", "password", "mina@gmail.com", address, User.Role.manager);
        restaurant = new Restaurant("test restaurant", manager, "test type", LocalTime.now(), LocalTime.now().plusHours(1000),
                "description", address, "test image");
    }

    @Test
    void testGetReviews_ReturnsOkResponse() throws RestaurantNotFound {
        int restaurantId = restaurant.getId();
        int page = 1;

        Address address = new Address("Iran", "Tehran", "Marzdaran");
        User user = new User("ali", "password", "ali@gmail.com", address, User.Role.client);
        Review review = new Review(user, new Rating(), "testComment", null);
        List<Review> original = List.of(review);
        PagedList<Review> pagedReviews = new PagedList<>(original, 1, 1);

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        when(reviewService.getReviews(restaurantId, page)).thenReturn(pagedReviews);

        Response response = reviewController.getReviews(restaurantId, page);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(pagedReviews, response.getData());
        assertEquals("reviews for restaurant (" + restaurantId + "): test restaurant", response.getMessage());
    }

    @Test
    void testGetReviews_WhenRestaurantNotFound() {
        int restaurantId = restaurant.getId();
        int page = 1;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> reviewController.getReviews(restaurantId, page));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
    }

    @Test
    void testAddReview_WhenRestaurantNotFound() {
        int restaurantId = restaurant.getId();
        int page = 1;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> reviewController.getReviews(restaurantId, page));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideIncompleteAddReviewParams")
    void testAddReview_WithMissingParameters(Map<String, Object> params) {
        int restaurantId = restaurant.getId();
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

        ResponseException exception = assertThrows(ResponseException.class, () -> reviewController.addReview(restaurantId, params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ControllerUtils.PARAMS_MISSING, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideBadTypeAddReviewParams")
    void testAddReview_WithBadTypeParameters(Map<String, Object> params) {
        int restaurantId = restaurant.getId();
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

        ResponseException exception = assertThrows(ResponseException.class, () -> reviewController.addReview(restaurantId, params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ControllerUtils.PARAMS_BAD_TYPE, exception.getMessage());
    }

    @Test
    void testAddReview_Successful() throws UserNotFound, ManagerCannotReview, RestaurantNotFound, UserHasNotReserved, InvalidReviewRating {
        int restaurantId = restaurant.getId();
        Map<String, Object> params = new HashMap<>();
        params.put("comment", "testComment");
        Map<String, Number> ratingMap = new HashMap<>() {{
            put("food", 5);
            put("service", 4);
            put("ambiance", 4);
            put("overall", 5);
        }};
        params.put("rating", ratingMap);

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        doNothing().when(reviewService).addReview(anyInt(), any(Rating.class), anyString());

        Response response = reviewController.addReview(restaurantId, params);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("review added successfully", response.getMessage());
    }

    @Test
    void testAddReview_ReviewServiceFailure() throws UserNotFound, ManagerCannotReview, RestaurantNotFound, InvalidReviewRating, UserHasNotReserved{
        int restaurantId = 1;
        Map<String, Object> params = new HashMap<>();
        params.put("comment", "testComment");
        Map<String, Number> ratingMap = new HashMap<>() {{
            put("food", 5);
            put("service", 4);
            put("ambiance", 4);
            put("overall", 5);
        }};
        params.put("rating", ratingMap);

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        doThrow(new UserNotFound()).when(reviewService).addReview(anyInt(), any(Rating.class), anyString());

        ResponseException exception = assertThrows(ResponseException.class, () -> reviewController.addReview(restaurantId, params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("User not found.", exception.getMessage());
    }

    static Stream<Map<String, Object>> provideIncompleteAddReviewParams() {
        return Stream.of(
                new HashMap<String, Object>() {{
                    put("comment", "testComment");
                }},
                new HashMap<String, Object>() {{
                    put("rating", "testRating");
                }}
        );
    }

    static Stream<Map<String, Object>> provideBadTypeAddReviewParams() {
        return Stream.of(
                new HashMap<String, Object>() {{
                    put("comment", "testComment");
                    put("rating", 3);
                }},
                new HashMap<String, Object>() {{
                    put("comment", true);
                    put("rating", "testRating");
                }},
                new HashMap<String, Object>() {{
                    put("comment", "testComment");
                    put("rating", new HashMap<String, String>(){{
                        put("food", "5");
                        put("service", "4");
                        put("ambiance", "4");
                        put("overall", "5");
                    }});
                }}
        );
    }
}
