Feature: Add and update reviews for a restaurant

  Scenario: Add a new review to a restaurant
    Given a restaurant named "Shila" managed by "Mina"
    When a new review is added by "Ali" with the following details:
      | food | service | ambiance | overall |
      | 4    | 5       | 3        | 4       |
    Then the restaurant should have 1 reviews
    And the reviews should include:
      | username | food | service | ambiance | overall |
      | Ali      | 4    | 5       | 3        | 4       |

  Scenario: Update an existing review by the same user
    Given a restaurant named "Shila" managed by "Mina"
    When a new review is added by "Ali" with the following details:
      | food | service | ambiance | overall |
      | 3    | 4       | 4        | 3       |
    And a new review is added by "Ali" with the following details:
      | food | service | ambiance | overall |
      | 5    | 5       | 5        | 5       |
    Then the restaurant should have 1 reviews
    And the reviews should include:
      | username     | food | service | ambiance | overall |
      | Ali  | 5    | 5       | 5        | 5       |

  Scenario: Add multiple reviews by different users
    Given a restaurant named "Shila" managed by "Mina"
    When a new review is added by "Ali" with the following details:
      | food | service | ambiance | overall |
      | 4    | 3       | 5        | 4       |
    And a new review is added by "Masoud" with the following details:
      | food | service | ambiance | overall |
      | 5    | 4       | 4        | 4       |
    Then the restaurant should have 2 reviews
    And the reviews should include:
      | username     | food | service | ambiance | overall |
      | Ali  | 4    | 3       | 5        | 4       |
      | Masoud  | 5    | 4       | 4        | 4       |

  Scenario: Add multiple reviews and calculate the average rating
    Given a restaurant named "Shila" managed by "Mina"
    When a new review is added by "Ali" with the following details:
      | food | service | ambiance | overall |
      | 4    | 4       | 5        | 4       |
    And a new review is added by "Masoud" with the following details:
      | food | service | ambiance | overall |
      | 5    | 5       | 4        | 5       |
    Then the restaurants average rating should be:
      | food | service | ambiance | overall |
      | 4.5  | 4.5     | 4.5      | 4.5     |

  Scenario: Replace an existing user's review and verify average rating
    Given a restaurant named "Shila" managed by "Mina"
    When a new review is added by "Ali" with the following details:
      | food | service | ambiance | overall |
      | 4    | 3       | 4        | 3       |
    And a new review is added by "Masoud" with the following details:
      | food | service | ambiance | overall |
      | 5    | 5       | 5        | 5       |
    And a new review is added by "Ali" with the following details:
      | food | service | ambiance | overall |
      | 5    | 4       | 4        | 4       |
    Then the restaurant should have 2 reviews
    Then the restaurants average rating should be:
      | food | service | ambiance | overall |
      | 5.0  | 4.5     | 4.5      | 4.5     |
