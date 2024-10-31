package mizdooni.controllers;

import mizdooni.exceptions.DuplicatedUsernameEmail;
import mizdooni.exceptions.InvalidEmailFormat;
import mizdooni.exceptions.InvalidUsernameFormat;
import mizdooni.model.Address;
import mizdooni.model.User;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import java.util.stream.Stream;

import java.util.HashMap;
import java.util.Map;

import static mizdooni.controllers.ControllerUtils.PARAMS_BAD_TYPE;
import static mizdooni.controllers.ControllerUtils.PARAMS_MISSING;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationControllerTest {
    private AutoCloseable closeable;
    private User user;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        user = new User("testUser", "password", "test@example.com", new Address("Country", "City", null), User.Role.client);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testUser_WhenUserIsLoggedIn_State() {
        when(userService.getCurrentUser()).thenReturn(user);

        Response response = authenticationController.user();

        assertEquals("current user", response.getMessage());
        assertEquals(user, response.getData());
    }

    @Test
    void testUser_WhenUserIsLoggedIn_Behavior() {
        when(userService.getCurrentUser()).thenReturn(user);

        Response response = authenticationController.user();;

        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    void testUser_WhenUserNotLoggedIn_State() {
        when(userService.getCurrentUser()).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.user());

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("no user logged in", exception.getMessage());
    }

    @Test
    void testUser_WhenUserNotLoggedIn_Behavior() {
        when(userService.getCurrentUser()).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.user());

        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    void testLogin_WithValidCredentials_State() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "testUser");
        params.put("password", "password");

        when(userService.login("testUser", "password")).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(user);

        Response response = authenticationController.login(params);

        assertEquals("login successful", response.getMessage());
        assertEquals(user, response.getData());
    }

    @Test
    void testLogin_WithValidCredentials_Behavior() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "testUser");
        params.put("password", "password");

        when(userService.login("testUser", "password")).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(user);

        try (MockedStatic<ControllerUtils> mockedControllerUtils = mockStatic(ControllerUtils.class)) {
            mockedControllerUtils.when(() -> ControllerUtils.doExist("testUser", "password")).thenReturn(true);

            authenticationController.login(params);

            mockedControllerUtils.verify(() -> ControllerUtils.doExist("testUser", "password"), times(1));
        }

        verify(userService, times(1)).login("testUser", "password");
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    void testLogin_WithInvalidCredentials_State() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "wrongUser");
        params.put("password", "wrongPass");

        when(userService.login("wrongUser", "wrongPass")).thenReturn(false);

        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.login(params));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("invalid username or password", exception.getMessage());
    }

    @Test
    void testLogin_WithInvalidCredentials_Behavior() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "wrongUser");
        params.put("password", "wrongPass");

        when(userService.login("wrongUser", "wrongPass")).thenReturn(false);

        try (MockedStatic<ControllerUtils> mockedControllerUtils = mockStatic(ControllerUtils.class)) {
            mockedControllerUtils.when(() -> ControllerUtils.doExist("wrongUser", "wrongPass")).thenReturn(true);

            try{
                authenticationController.login(params);
            } catch (ResponseException ignored) {
            }

            mockedControllerUtils.verify(() -> ControllerUtils.doExist("wrongUser", "wrongPass"), times(1));
        }

        verify(userService, times(1)).login("wrongUser", "wrongPass");
        verify(userService, times(0)).getCurrentUser();
    }

    @Test
    void testLogin_UserDoesNotExist_State() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "nonExistentUser");
        params.put("password", "password");

        try (MockedStatic<ControllerUtils> mockedControllerUtils = mockStatic(ControllerUtils.class)) {
            mockedControllerUtils.when(() -> ControllerUtils.doExist("nonExistentUser", "password")).thenReturn(false);

            ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.login(params));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals(PARAMS_MISSING, exception.getMessage());
        }
    }

    @Test
    void testLogin_UserDoesNotExist_Behavior() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "nonExistentUser");
        params.put("password", "password");

        try (MockedStatic<ControllerUtils> mockedControllerUtils = mockStatic(ControllerUtils.class)) {
            mockedControllerUtils.when(() -> ControllerUtils.doExist("nonExistentUser", "password")).thenReturn(false);

            ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.login(params));

            mockedControllerUtils.verify(() -> ControllerUtils.doExist("nonExistentUser", "password"), times(1));
        }

        verify(userService, times(0)).login("nonExistentUser", "password");
        verify(userService, times(0)).getCurrentUser();
    }

    @ParameterizedTest
    @MethodSource("provideIncompleteSignupParams")
    void testSignup_WithMissingParameters(Map<String, Object> params) {
        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.signup(params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_MISSING, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideBadTypeSignupParams")
    void testSignup_WithBadTypeParameters(Map<String, Object> params) {
        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.signup(params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());
    }

    @Test
    void testSignup_WithValidParameters() throws InvalidEmailFormat, InvalidUsernameFormat, DuplicatedUsernameEmail {
        Map<String, Object> params = new HashMap<>();
        params.put("username", "testUser");
        params.put("password", "password");
        params.put("email", "test@example.com");
        params.put("role", "client");

        Map<String, String> addressParams = new HashMap<>();
        addressParams.put("country", "Country");
        addressParams.put("city", "City");
        params.put("address", addressParams);

        doNothing().when(userService).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));
        when(userService.login("testUser", "password")).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(user);

        Response response = authenticationController.signup(params);

        assertEquals("signup successful", response.getMessage());
        assertEquals(user, response.getData());
    }

    @Test
    void testSignup_UserServiceThrowsException() throws InvalidEmailFormat, InvalidUsernameFormat, DuplicatedUsernameEmail {
        Map<String, Object> params = new HashMap<>();
        params.put("username", "testUser");
        params.put("password", "password");
        params.put("email", "test@example.com");
        params.put("role", "client");

        Map<String, String> addressParams = new HashMap<>();
        addressParams.put("country", "Country");
        addressParams.put("city", "City");
        params.put("address", addressParams);

        doThrow(new RuntimeException("Signup failed")).when(userService).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));

        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.signup(params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Signup failed", exception.getMessage());
    }

    @Test
    void testLogout_WhenUserIsLoggedIn() {
        when(userService.logout()).thenReturn(true);

        Response response = authenticationController.logout();

        assertEquals("logout successful", response.getMessage());
    }

    @Test
    void testLogout_WhenNoUserLoggedIn() {
        when(userService.logout()).thenReturn(false);

        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.logout());

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("no user logged in", exception.getMessage());
    }


    @Test
    void testValidateUsername_WithAvailableUsername() {
        when(userService.usernameExists("uniqueUser")).thenReturn(false);

        Response response = authenticationController.validateUsername("uniqueUser");

        assertEquals("username is available", response.getMessage());
    }

    @Test
    void testValidateUsername_WhenUsernameExists() {
        when(userService.usernameExists("existingUser")).thenReturn(true);

        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.validateUsername("existingUser"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("username already exists", exception.getMessage());
    }

    @Test
    void testValidateUsername_WhenUsernameIsInvalid() {
        when(userService.usernameExists("user.name")).thenReturn(false);

        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.validateUsername("user.name"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("invalid username format", exception.getMessage());
    }

    @Test
    void testValidateEmail_WithValidEmail() {
        when(userService.emailExists("unique@example.com")).thenReturn(false);

        Response response = authenticationController.validateEmail("unique@example.com");

        assertEquals("email not registered", response.getMessage());
    }

    @Test
    void testValidateEmail_WhenEmailExists() {
        when(userService.emailExists("existing@example.com")).thenReturn(true);

        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.validateEmail("existing@example.com"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("email already registered", exception.getMessage());
    }

    @Test
    void testValidateUsername_WhenEmailIsInvalid() {
        when(userService.emailExists("wrong email format")).thenReturn(false);

        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.validateEmail("wrong email format"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("invalid email format", exception.getMessage());
    }

    static Stream<Map<String, Object>> provideBadTypeSignupParams() {
        return Stream.of(
                new HashMap<String, Object>() {{
                    put("username", "testUser");
                    put("password", "password");
                    put("email", "test@example.com");
                    put("address", new HashMap<String, Integer>() {{
                        put("country", 1);
                        put("city", 2);
                    }});
                    put("role", "client");
                }},
                new HashMap<String, Object>() {{
                    put("username", true);
                    put("password", "password");
                    put("email", "test@example.com");
                    put("address", new HashMap<String, String>() {{
                        put("country", "Country");
                        put("city", "City");
                    }});
                    put("role", "client");
                }},
                new HashMap<String, Object>() {{
                    put("username", "testUser");
                    put("password", 1234);
                    put("email", "test@example.com");
                    put("address", new HashMap<String, String>() {{
                        put("country", "Country");
                        put("city", "City");
                    }});
                    put("role", "client");
                }},
                new HashMap<String, Object>() {{
                    put("username", "testUser");
                    put("password", "password");
                    put("email", 2345);
                    put("address", new HashMap<String, String>() {{
                        put("country", "Country");
                        put("city", "City");
                    }});
                    put("role", "client");
                }},
                new HashMap<String, Object>() {{
                    put("username", "testUser");
                    put("password", "password");
                    put("email", "test@example.com");
                    put("address", new HashMap<String, String>() {{
                        put("country", "Country");
                        put("city", "City");
                    }});
                    put("role", true);
                }}
        );
    }

    static Stream<Map<String, Object>> provideIncompleteSignupParams() {
        return Stream.of(
                new HashMap<String, Object>() {{
                    put("username", "testUser");
                    put("password", "password");
                    put("email", "test@example.com");
                    put("role", "client");
                }},
                new HashMap<String, Object>() {{
                    put("password", "password");
                    put("email", "test@example.com");
                    put("address", new HashMap<String, String>() {{
                        put("country", "Country");
                        put("city", "City");
                    }});
                    put("role", "client");
                }},
                new HashMap<String, Object>() {{
                    put("username", "testUser");
                    put("email", "test@example.com");
                    put("address", new HashMap<String, String>() {{
                        put("country", "Country");
                        put("city", "City");
                    }});
                    put("role", "client");
                }},
                new HashMap<String, Object>() {{
                    put("username", "testUser");
                    put("password", "password");
                    put("address", new HashMap<String, String>() {{
                        put("country", "Country");
                        put("city", "City");
                    }});
                    put("role", "client");
                }},
                new HashMap<String, Object>() {{
                    put("username", "testUser");
                    put("password", "password");
                    put("email", "test@example.com");
                    put("address", new HashMap<String, String>() {{
                        put("country", "Country");
                        put("city", "City");
                    }});
                }}
        );
    }
}