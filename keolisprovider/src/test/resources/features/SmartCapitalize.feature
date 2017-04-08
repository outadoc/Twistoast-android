Feature: Smart capitalization
  Test bus stop name re-capitalization, to make correct and good-looking labels

  Scenario: Single word
    When I capitalize 'ACADEMIE'
    Then I get 'Academie'

  Scenario: Two words
    When I capitalize 'CUVERVILLE MAIRIE'
    Then I get 'Cuverville Mairie'

  Scenario: Determinant
    When I capitalize 'CAEN GRACE DE DIEU'
    Then I get 'Caen Grace de Dieu'

  Scenario: Trim spaces
    When I capitalize 'B  CAEN GRACE DE DIEU'
    Then I get 'B Caen Grace de Dieu'

  Scenario: Hyphen
    When I capitalize 'IFS - GRACE DE DIEU'
    Then I get 'Ifs - Grace de Dieu'

  Scenario: Capitalize acronym
    When I capitalize 'GARE SNCF'
    Then I get 'Gare SNCF'

  Scenario: Double hyphenated acronym
    When I capitalize 'CROUS-SUAPS'
    Then I get 'CROUS-SUAPS'
