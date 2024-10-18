package mizdooni.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class RatingTest {
    private Rating rating;

    @BeforeEach
    void setUp() {
        rating = new Rating();
        rating.food = 4;
        rating.service = 4.5;
        rating.ambiance = 3.2;
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.2, 1.4, 2.3, 4.2, 5.4})
    void testGetStarCount_RoundDown(double value) {
        rating.overall = value;
        assertEquals(Math.floor(value), rating.getStarCount());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.5, 0.7, 1.6, 2.8, 3.9, 4.9})
    void testGetStarCount_RoundUp(double value) {
        rating.overall = value;
        assertEquals(Math.ceil(value), rating.getStarCount());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0})
    void testGetStarCount_Exact(double value) {
        rating.overall = value;
        assertEquals(value, rating.getStarCount());
    }

    @ParameterizedTest
    @ValueSource(doubles = {5.1, 6.0, 7.3, 8.9, 9.5, 100000.6, 999999999, 8})
    void testGetStarCount_MinTo5(double value) {
        rating.overall = value;
        assertEquals(5, rating.getStarCount());
    }
}
