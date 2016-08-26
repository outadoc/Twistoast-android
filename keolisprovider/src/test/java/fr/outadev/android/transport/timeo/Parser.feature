Feature: Parser
  Tests for the methods that parse the data returned by the API

  Scenario: Get a basic list of lines and directions
    Given a connection to the API
    When I request a list of lines with mock test_lines_list.xml
    Then I get the following lines
      | Id   | Name     | direction.id | direction.name      | Network Code |

  Scenario: Get a basic list of stops for line and direction
    Given a connection to the API
    When I request a list of stops for line 'TRAM' and direction 'A' with mock test_stops_list_TRAM_A.xml
    Then I get the following stops
      | Id | Name | Reference |

  Scenario: Get schedules for single stop
    Given a connection to the API
    When I request a list of schedules for the following stop references with mock test_single_schedule_ok.xml
      | Reference   |
      | -1493165598 |
    Then I get a list of schedules for the following stop
      | Id | Name | Reference |
    And the list of schedules is
      | Schedule Time | Direction |
