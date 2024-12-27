Feature: Add reservations for a user

  Scenario: Add a single reservation for a user
    Given a user with the username "john_doe" and email "john.doe@example.com"
    When a reservation is added with the following details:
      | restaurantName | datetime           | isCancelled |
      | Restaurant A   | 2024-12-27T19:00   | false       |
    Then the user should have 1 reservation
    And the reservation details should be:
      | reservationNumber | restaurantName | tableNumber | datetime           | isCancelled |
      | 0                 | Restaurant A   | 1           | 2024-12-27T19:00   | false       |

  Scenario: Add multiple reservations for a user
    Given a user with the username "jane_smith" and email "jane.smith@example.com"
    When the following reservations are added:
      | restaurantName  | datetime           | isCancelled |
      | Restaurant B    | 2024-12-28T12:00   | false       |
      | Restaurant C    | 2024-12-28T15:00   | false       |
    Then the user should have 2 reservations
    And the reservation details should include:
      | reservationNumber | restaurantName  | datetime           | isCancelled |
      | 0                 | Restaurant B    | 2024-12-28T12:00   | false       |
      | 1                 | Restaurant C    | 2024-12-28T15:00   | false       |

  Scenario: Add a reservation for the same restaurant at different times
    Given a user with the username "bob_brown" and email "bob.brown@example.com"
    When the following reservations are added:
      | restaurantName | datetime           | isCancelled |
      | Restaurant D   | 2024-12-29T18:00   | false       |
      | Restaurant D   | 2024-12-30T18:00   | false       |
    Then the user should have 2 reservations
    And the reservation details should include:
      | reservationNumber | restaurantName | datetime           | isCancelled |
      | 0                 | Restaurant D   | 2024-12-29T18:00   | false       |
      | 1                 | Restaurant D   | 2024-12-30T18:00   | false       |

  Scenario: Add a reservation and verify reservation number increments
    Given a user with the username "alice_jones" and email "alice.jones@example.com"
    When the following reservations are added:
      | restaurantName | datetime           | isCancelled |
      | Restaurant E   | 2024-12-31T20:00   | false       |
      | Restaurant F   | 2025-01-01T19:00   | false       |
    Then the reservation numbers should be sequential
    And the reservation details should include:
      | reservationNumber | restaurantName | datetime           | isCancelled |
      | 0                 | Restaurant E   | 2024-12-31T20:00   | false       |
      | 1                 | Restaurant F   | 2025-01-01T19:00   | false       |
