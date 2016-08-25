Feature: Parser
  Tests for the methods that parse the data returned by the API

  Scenario: Get a list of lines and directions
    Given a connection to the API
    When I request a list of lines
    Then I get the following lines
    | Id   | Name     | direction.id | direction.name      | Network Code |
