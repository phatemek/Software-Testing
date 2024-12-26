Feature: Calculate average rating of a restaurant

  Scenario: Calculate average rating with multiple reviews
    Given a restaurant with the following reviews:
      | food | service | ambiance | overall |
      | 4    | 5       | 3        | 4       |
      | 3    | 4       | 4        | 4       |
      | 5    | 3       | 5        | 5       |
    When the average rating is calculated
    Then the average rating should be:
      | food | service | ambiance | overall |
      | 4    | 4       | 4        | 4.33    |

  Scenario: Calculate average rating with no reviews
    Given a restaurant with no reviews
    When the average rating is calculated
    Then the average rating should be:
      | food | service | ambiance | overall |
      | 0    | 0       | 0        | 0       |

  Scenario: Calculate average rating with one review
    Given a restaurant with the following review:
      | food | service | ambiance | overall |
      | 5    | 4       | 3        | 5       |
    When the average rating is calculated
    Then the average rating should be:
      | food | service | ambiance | overall |
      | 5    | 4       | 3        | 5       |
