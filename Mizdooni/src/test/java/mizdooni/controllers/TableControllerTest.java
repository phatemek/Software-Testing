package mizdooni.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import mizdooni.model.*;
import mizdooni.service.RestaurantService;
import mizdooni.service.TableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TableControllerTest {

    Restaurant restaurant;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private TableService tableService;

    @BeforeEach
    void setup() {
        restaurant = new Restaurant("test_restaurant", null, null, null, null, null, null, null);
    }

    @Test
    void getTables_RestaurantExists_Ok() throws Exception {
        int restaurantId = 1;
        List<Table> tables = List.of(new Table(1, 4, 3), new Table(2, 4, 4));

        Mockito.when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        Mockito.when(tableService.getTables(restaurantId)).thenReturn(tables);

        mockMvc.perform(get("/tables/{restaurantId}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("tables listed"))
                .andExpect(jsonPath("$.data[0].tableNumber").value(1))
                .andExpect(jsonPath("$.data[0].seatsNumber").value(3))
                .andExpect(jsonPath("$.data[1].tableNumber").value(2))
                .andExpect(jsonPath("$.data[1].seatsNumber").value(4));
    }

    @Test
    void getTables_RestaurantNotFound_Exception() throws Exception {
        int restaurantId = 1;

        Mockito.when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

        mockMvc.perform(get("/tables/{restaurantId}", restaurantId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTables_GetTablesServiceException_Exception() throws Exception {
        int restaurantId = 1;
        List<Table> tables = List.of(new Table(1, 4, 3), new Table(2, 4, 4));

        Mockito.when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        Mockito.when(tableService.getTables(restaurantId)).thenThrow(new RuntimeException("Service Exception"));

        mockMvc.perform(get("/tables/{restaurantId}", restaurantId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Service Exception"));
    }

    @Test
    void addTable_ValidParams_Ok() throws Exception {
        int restaurantId = 1;
        Map<String, String> params = Map.of("seatsNumber", "4");

        Mockito.when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        Mockito.doNothing().when(tableService).addTable(restaurantId, 4);

        mockMvc.perform(post("/tables/{restaurantId}", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(params)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("table added"));
    }

    @Test
    void addTable_RestaurantNotFound_Exception() throws Exception {
        int restaurantId = 1;
        Map<String, String> params = Map.of("seatsNumber", "4");

        Mockito.when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

        mockMvc.perform(post("/tables/{restaurantId}", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(params)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTable_MissingParameter_Exception() throws Exception {
        int restaurantId = 1;
        Map<String, String> params = Map.of();

        Mockito.when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

        mockMvc.perform(post("/tables/{restaurantId}", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(params)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("parameters missing"));
    }

    @Test
    void addTable_InvalidParameterType_Exception() throws Exception {
        int restaurantId = 1;
        Map<String, String> params = Map.of("seatsNumber", "invalid");

        Mockito.when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

        mockMvc.perform(post("/tables/{restaurantId}", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(params)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("bad parameter type"));
    }

    @Test
    void addTable_AddTableServiceException_Exception() throws Exception {
        int restaurantId = 1;
        Map<String, String> params = Map.of("seatsNumber", "4");

        Mockito.when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        Mockito.doThrow(new RuntimeException("Service Exception"))
                .when(tableService).addTable(restaurantId, 4);

        mockMvc.perform(post("/tables/{restaurantId}", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(params)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Service Exception"));
    }
}
