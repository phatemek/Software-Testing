Feature: Add reservations for a user

  Scenario: Add a single reservation for a user
    Given a user with the username "ali" and email "ali@gmail.com"
    And a restaurant with the name "Shila"
    When the following reservations are added:
      | restaurantName | tableNumber | datetime         |
      | Shila          | 1           | 2024-12-27T19:00 |
    Then the user should have 1 reservations
    And the reservation details should include:
      | reservationNumber | restaurantName | tableNumber | datetime         |
      | 0                 | Shila          | 1           | 2024-12-27T19:00 |

  Scenario: Add multiple reservations for a user
    Given a user with the username "ali" and email "ali@gmail.com"
    And a restaurant with the name "Shila"
    When the following reservations are added:
      | restaurantName | tableNumber | datetime         |
      | Shila          | 1           | 2024-12-27T19:00 |
      | Shila          | 3           | 2024-12-28T15:00 |
    Then the user should have 2 reservations
    And the reservation details should include:
      | reservationNumber | restaurantName | tableNumber | datetime         |
      | 0                 | Shila          | 1           | 2024-12-27T19:00 |
      | 1                 | Shila          | 3           | 2024-12-28T15:00 |

  Scenario: Add a reservation and verify reservation number increments
    Given a user with the username "ali" and email "ali@gmail.com"
    And a restaurant with the name "Shila"
    When the following reservations are added:
      | restaurantName | tableNumber | datetime         |
      | Shila          | 1           | 2024-12-27T19:00 |
      | Shila          | 3           | 2024-12-28T15:00 |
    Then the reservation numbers should be sequential
    And the reservation details should include:
      | reservationNumber | restaurantName | tableNumber | datetime         |
      | 0                 | Shila          | 1           | 2024-12-27T19:00 |
      | 1                 | Shila          | 3           | 2024-12-28T15:00 |
