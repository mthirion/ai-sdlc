Feature: Order Discount Calculation
  As an e-commerce platform
  I want to calculate order discounts accurately
  So that customers are charged the correct price

  Scenario: AC-1 - Basic calculation with no discounts
    Given an order with the following items:
      | productId | quantity | unitPrice |
      | PROD-A    | 3        | 10.00     |
      | PROD-B    | 2        | 25.00     |
    And the customer has "NONE" loyalty status
    And no promo code is applied
    When I submit the order to calculate the price
    Then the response status should be 200
    And the subtotal should be 80.00
    And the loyalty discount should be 0.00
    And the volume discount should be 0.00
    And the promo discount should be 0.00
    And the total discount should be 0.00
    And the final price should be 80.00

  Scenario: AC-2 - Loyalty and volume discounts combined
    Given an order with the following items:
      | productId | quantity | unitPrice |
      | PROD-A    | 4        | 50.00     |
    And the customer has "GOLD" loyalty status
    And no promo code is applied
    When I submit the order to calculate the price
    Then the response status should be 200
    And the loyalty discount should be 20.00
    And the volume discount should be 6.00
    And the total discount should be 26.00
    And the final price should be 174.00

  Scenario: AC-3 - Promo code with volume discount
    Given an order with the following items:
      | productId | quantity | unitPrice |
      | PROD-A    | 4        | 50.00     |
    And the customer has "NONE" loyalty status
    And the promo code "SAVE10" is applied
    When I submit the order to calculate the price
    Then the response status should be 200
    And the promo discount should be 20.00
    And the volume discount should be 6.00
    And the total discount should be 26.00
    And the final price should be 174.00

  Scenario: AC-4 - Discount cap enforced with priority ordering
    Given an order with the following items:
      | productId | quantity | unitPrice |
      | PROD-A    | 6        | 100.00    |
    And the customer has "PLATINUM" loyalty status
    And the promo code "SUMMER25" is applied
    When I submit the order to calculate the price
    Then the response status should be 200
    And the promo discount should be 150.00
    And the loyalty discount should be 30.00
    And the volume discount should be 0.00
    And the total discount should be 180.00
    And the final price should be 420.00

  Scenario: AC-5 - Expired promo code is rejected
    Given an order with the following items:
      | productId | quantity | unitPrice |
      | PROD-A    | 2        | 50.00     |
    And the customer has "NONE" loyalty status
    And the promo code "SUMMER25" is applied
    And today's date is after "2026-09-30"
    When I submit the order to calculate the price
    Then the response status should be 400
    And the error message should be "Promo code SUMMER25 expired on 2026-09-30"

  Scenario: AC-6 - Promo code minimum order not met
    Given an order with the following items:
      | productId | quantity | unitPrice |
      | PROD-A    | 3        | 10.00     |
    And the customer has "NONE" loyalty status
    And the promo code "SUMMER25" is applied
    When I submit the order to calculate the price
    Then the response status should be 400
    And the error message should be "Promo code SUMMER25 requires a minimum order of $50.00"

  Scenario: AC-7 - Empty items list is rejected
    Given an order with no items
    And the customer has "NONE" loyalty status
    When I submit the order to calculate the price
    Then the response status should be 400

  Scenario: AC-8 - Invalid quantity is rejected
    Given an order with the following items:
      | productId | quantity | unitPrice |
      | PROD-A    | 0        | 10.00     |
    And the customer has "NONE" loyalty status
    When I submit the order to calculate the price
    Then the response status should be 400

  Scenario: AC-9 - Monetary precision with no floating-point errors
    Given an order with the following items:
      | productId | quantity | unitPrice |
      | PROD-A    | 3        | 19.99     |
    And the customer has "SILVER" loyalty status
    And no promo code is applied
    When I submit the order to calculate the price
    Then the response status should be 200
    And the subtotal should be 59.97
    And the loyalty discount should be 3.00
    And the final price should be 56.97

  Scenario: AC-10 - Unknown promo code is rejected
    Given an order with the following items:
      | productId | quantity | unitPrice |
      | PROD-A    | 2        | 50.00     |
    And the customer has "NONE" loyalty status
    And the promo code "FAKECODE" is applied
    When I submit the order to calculate the price
    Then the response status should be 400
    And the error message should be "Unknown promo code: FAKECODE"
