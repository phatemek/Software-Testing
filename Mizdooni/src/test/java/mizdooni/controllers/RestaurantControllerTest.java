package mizdooni.controllers;

import mizdooni.model.Address;
import mizdooni.model.Restaurant;
import mizdooni.model.RestaurantSearchFilter;
import mizdooni.model.User;
import mizdooni.response.PagedList;
import mizdooni.response.Response;
import mizdooni.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RestaurantControllerTest {
    Restaurant restaurant = new Restaurant("test_restaurant", null, null, null, null, null, null, null);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        Mockito.reset(restaurantService);
    }

    @Test
    void getRestaurant_RestaurantExists_Ok() throws Exception{
        when(restaurantService.getRestaurant(1)).thenReturn(restaurant);

        mockMvc.perform(get("/restaurants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant found"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").value(restaurant.getName()));
    }

    @Test
    void getRestaurant_RestaurantNotFound_Exception() throws Exception{
        when(restaurantService.getRestaurant(1)).thenReturn(null);

        mockMvc.perform(get("/restaurants/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRestaurants_RestaurantsExist_Ok() throws Exception {
        var restaurants = new PagedList<>(List.of(restaurant), 1, 10);

        when(restaurantService.getRestaurants(eq(1), any(RestaurantSearchFilter.class)))
                .thenReturn(restaurants);

        mockMvc.perform(get("/restaurants?page=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurants listed"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.page").value(1));
    }

    @Test
    void getRestaurants_GetRestaurantsException_Exception() throws Exception {
        when(restaurantService.getRestaurants(anyInt(), any(RestaurantSearchFilter.class)))
                .thenThrow(new RuntimeException("test-exception"));

        mockMvc.perform(get("/restaurants?page=3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("test-exception"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getManagerRestaurants_ManagerExists_Ok() throws Exception {
        Mockito.when(restaurantService.getManagerRestaurants(eq(1)))
                .thenReturn(List.of(restaurant));

        mockMvc.perform(get("/restaurants/manager/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("manager restaurants listed"))
                .andExpect(jsonPath("$.data[0].name").value("test_restaurant"));
    }

    @Test
    void getManagerRestaurants_GetManagerRestaurantsException_Exception() throws Exception {
        Mockito.when(restaurantService.getManagerRestaurants(eq(1)))
                .thenThrow(new RuntimeException("test-exception"));

        mockMvc.perform(get("/restaurants/manager/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("test-exception"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addRestaurant_ParamsValid_Ok() throws Exception {
        String validRequest = """
        {
            "name": "Test Restaurant",
            "type": "Fast Food",
            "startTime": "08:00",
            "endTime": "22:00",
            "description": "A test restaurant",
            "address": {
                "country": "Iran",
                "city": "Tehran",
                "street": "Valiasr"
            }
        }
        """;

        Mockito.when(restaurantService.addRestaurant(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant added"))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void addRestaurant_missingParams_Exception() throws Exception {
        String missingParamsRequest = """
            {
                "name": "Test Restaurant",
                "type": "Fast Food"
            }
            """;

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingParamsRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("parameters missing"));
    }

    @Test
    void addRestaurant_badParamTypes_Exception() throws Exception {
        String badParamsRequest = """
        {
            "name": "Test Restaurant",
            "type": "Fast Food",
            "startTime": "invalid-time",
            "endTime": "22:00",
            "description": "A test restaurant",
            "address": {
                "country": "Iran",
                "city": "Tehran",
                "street": "Valiasr"
            }
        }
        """;
        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badParamsRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("bad parameter type"));
    }

    @Test
    void addRestaurant_invalidParams_Exception() throws Exception {
        String invalidParamsRequest = """
        {
            "name": "Test Restaurant",
            "type": "Fast Food",
            "startTime": "10:00",
            "endTime": "22:00",
            "description": "A test restaurant",
            "address": {
                "country": "",
                "city": "",
                "street": ""
            }
        }
        """;

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidParamsRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("parameters missing"));
    }

    @Test
    void addRestaurant_AddRestaurantServiceExcaption_Exception() throws Exception {
        String validRequest = """
        {
            "name": "Test Restaurant",
            "type": "Fast Food",
            "startTime": "08:00",
            "endTime": "22:00",
            "description": "A test restaurant",
            "address": {
                "country": "Iran",
                "city": "Tehran",
                "street": "Valiasr"
            }
        }
        """;

        Mockito.when(restaurantService.addRestaurant(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Service failure"));

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Service failure"));
    }

    @Test
    void validateRestaurantName_NewRestaurantName_Ok() throws Exception {
        Mockito.when(restaurantService.restaurantExists("UniqueName")).thenReturn(false);

        mockMvc.perform(get("/validate/restaurant-name")
                        .param("data", "UniqueName")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant name is available"));
    }

    @Test
    void validateRestaurantName_NameAlreadyExists_Exception() throws Exception {
        Mockito.when(restaurantService.restaurantExists("TakenName")).thenReturn(true);

        mockMvc.perform(get("/validate/restaurant-name")
                        .param("data", "TakenName")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("restaurant name is taken"));
    }

    @Test
    void getRestaurantTypes_TypesExist_Ok() throws Exception {
        Mockito.when(restaurantService.getRestaurantTypes())
                .thenReturn(Set.of("Italian", "Chinese", "Mexican"));

        mockMvc.perform(get("/restaurants/types")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant types"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasItems("Italian", "Chinese", "Mexican")));
    }

    @Test
    void getRestaurantTypes_GetRestaurantTypesServiceException_Exception() throws Exception {
        Mockito.when(restaurantService.getRestaurantTypes())
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/restaurants/types")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Service error"));
    }

    @Test
    void getRestaurantLocations_LocationsExist_Ok() throws Exception {
        Map<String, Set<String>> locations = Map.of(
                "USA", Set.of("New York", "Los Angeles"),
                "Italy", Set.of("Rome", "Milan")
        );
        Mockito.when(restaurantService.getRestaurantLocations()).thenReturn(locations);

        mockMvc.perform(get("/restaurants/locations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant locations"))
                .andExpect(jsonPath("$.data.USA").isArray())
                .andExpect(jsonPath("$.data.USA", org.hamcrest.Matchers.hasItems("New York", "Los Angeles")))
                .andExpect(jsonPath("$.data.Italy").isArray())
                .andExpect(jsonPath("$.data.Italy", org.hamcrest.Matchers.hasItems("Rome", "Milan")));
    }

    @Test
    void getRestaurantLocations_GetRestaurantLocationsServiceException_Exception() throws Exception {
        Mockito.when(restaurantService.getRestaurantLocations())
                .thenThrow(new RuntimeException("Test Exception"));

        mockMvc.perform(get("/restaurants/locations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Test Exception"));
    }
}
