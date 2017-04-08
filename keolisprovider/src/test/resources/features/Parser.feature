Feature: Parser
  Tests for the methods that parse the data returned by the API

  Scenario: Get a basic list of lines and directions
    Given a connection to the API
    When I request a list of lines with mock test_lines_list.xml
    Then I get the following lines
      | Id   | Name     | Direction |
      | TRAM | Tram     | A         |
      | TRAM | Tram     | R         |
      | 1    | Lianes 1 | A         |
      | 1    | Lianes 1 | R         |
      | 2    | Lianes 2 | A         |
      | 2    | Lianes 2 | R         |
      | 3    | Lianes 3 | A         |
      | 3    | Lianes 3 | R         |
      | 4    | Lianes 4 | A         |
      | 4    | Lianes 4 | R         |
      | 5    | Ligne 5  | A         |
      | 5    | Ligne 5  | R         |
      | 6    | Ligne 6  | A         |
      | 6    | Ligne 6  | R         |
      | 7    | Ligne 7  | A         |
      | 7    | Ligne 7  | R         |
      | 8    | Ligne 8  | A         |
      | 8    | Ligne 8  | R         |
      | 9    | Ligne 9  | A         |
      | 9    | Ligne 9  | R         |
      | 10   | Ligne 10 | A         |
      | 10   | Ligne 10 | R         |
      | 11   | Ligne 11 | A         |
      | 11   | Ligne 11 | R         |
      | 14   | Ligne 14 | A         |
      | 14   | Ligne 14 | R         |
      | 15   | Ligne 15 | A         |
      | 15   | Ligne 15 | R         |
      | 16   | Ligne 16 | A         |
      | 16   | Ligne 16 | R         |
      | 17   | Ligne 17 | A         |
      | 17   | Ligne 17 | R         |
      | 18   | Ligne 18 | A         |
      | 18   | Ligne 18 | R         |
      | 19   | Ligne 19 | A         |
      | 19   | Ligne 19 | R         |
      | 20   | Ligne 20 | A         |
      | 20   | Ligne 20 | R         |
      | 21   | Ligne 21 | A         |
      | 21   | Ligne 21 | R         |
      | 22   | Ligne 22 | A         |
      | 22   | Ligne 22 | R         |
      | 23   | Ligne 23 | A         |
      | 23   | Ligne 23 | R         |
      | 24   | Ligne 24 | A         |
      | 24   | Ligne 24 | R         |
      | 25   | Ligne 25 | A         |
      | 25   | Ligne 25 | R         |
      | 26   | Ligne 26 | A         |
      | 26   | Ligne 26 | R         |
      | 27   | Ligne 27 | A         |
      | 27   | Ligne 27 | R         |
      | 28   | Ligne 28 | A         |
      | 28   | Ligne 28 | R         |
      | 29   | Ligne 29 | A         |
      | 29   | Ligne 29 | R         |
      | 32   | Ligne 32 | A         |
      | 32   | Ligne 32 | R         |
      | 62   | Ligne 62 | A         |
      | 62   | Ligne 62 | R         |
      | NZCA | Navca    | A         |

  Scenario: Get a basic list of stops for line and direction
    Given a connection to the API
    When I request a list of stops for line 'TRAM' and direction 'A' with mock test_stops_list_TRAM_A.xml
    Then I get the following stops
      | Id   | Name                  |
      | 2,523 | Academie              |
      | 2,063 | Aviation              |
      | 131   | Bernieres             |
      | 1,991 | Boulevard Leroy       |
      | 2,581 | Cafe des Images       |
      | 1,093 | Calvaire St-Pierre    |
      | 2,593 | Chateau d'Eau         |
      | 1,401 | CHU                   |
      | 1,611 | Cite U Lebisey        |
      | 2,601 | Citis                 |
      | 1,481 | Claude Bloch/Campus 4 |
      | 2,083 | Concorde              |
      | 1,081 | Copernic              |
      | 1,513 | Cote de Nacre         |
      | 1,001 | CROUS-SUAPS           |
      | 1,871 | Gare SNCF             |
      | 2,213 | Grace de Dieu         |
      | 1,971 | Guynemer              |
      | 4,867 | Ifs Jean Vilar        |
      | 2,091 | Liberte               |
      | 2,001 | Lux Victor Lepine     |
      | 1,504 | Marechal Juin         |
      | 4,913 | Modigliani            |
      | 1,631 | Pierre Heuze          |
      | 971   | Place de la Mare      |
      | 1,981 | Poincare              |
      | 201   | Quai de Juillet       |
      | 151   | Quatrans              |
      | 111   | Resistance            |
      | 2,233 | Rostand/Fresnel       |
      | 2,928 | St-Clair              |
      | 141   | St-Pierre             |
      | 981   | Universite            |

  Scenario: Get schedules for single stop
    Given a connection to the API
    When I request a list of schedules for the following stop references with mock test_single_schedule_ok.xml
      | Reference   |
      | -1493165598 |
    Then I get a list of schedules for the following stop
      | Id | Name | Reference |
    And the list of schedules is
      | Schedule Time | Direction |
