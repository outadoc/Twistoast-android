Feature: Time arithmetic
  Going from a time in the form of a string ("22:33"), determine its exact time and date.
  Can be tricky because of the day boundary.

  Scenario: Bus coming in 10 minutes
    Given it's 10:05
    When a bus is coming at 10:15
    Then the bus is coming in 10 minutes

  Scenario: Bus coming in 2 minutes
    Given it's 10:05
    When a bus is coming at 10:07
    Then the bus is coming in 2 minutes

  Scenario: Bus coming in 10 minutes across midnight
    Given it's 23:55
    When a bus is coming at 00:05
    Then the bus is coming in 10 minutes

  Scenario: Bus coming in 70 minutes across midnight
    Given it's 23:55
    When a bus is coming at 01:05
    Then the bus is coming in 70 minutes

  Scenario: Bus is late by 3 minutes
    Given it's 10:05
    When a bus is coming at 10:02
    Then the bus is late by 3 minutes
