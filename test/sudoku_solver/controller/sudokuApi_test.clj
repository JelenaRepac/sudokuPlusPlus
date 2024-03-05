(ns sudoku-solver.controller.sudokuApi_test
  (:require [midje.sweet :refer :all]
            [clojure.java.jdbc :as jdbc]
            [compojure.core :refer :all]
    [sudoku-solver.controller.sudokuApi :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as json]
            ))

;; Table already exists
(let [db-spec
      {:classname "org.h2.Driver"
       :subprotocol "h2:mem"
       :subname "test-db;DB_CLOSE_DELAY=-1"
       :user "sa"
       :password ""
       }]

  (fact "Testing table creation"
        (jdbc/execute! db-spec
                       ["CREATE TABLE sudoku_boards
                 (id SERIAL PRIMARY KEY,
                 board VARCHAR(200),
                 solved_board VARCHAR(200),
                 difficulty VARCHAR(50))"]) => throws)

  (fact "Testing table creation"
        (jdbc/execute! db-spec
                       ["CREATE TABLE results
                 (board VARCHAR(200),
                 time DECIMAL(10),
                 user VARCHAR(100),
                 initial VARCHAR(200))"]) => throws)




(facts "Testing the serialize function"
       (fact "Serializing a vector"
             (serialize [1 2 3]) => "[1,2,3]")

       (fact "Serializing an empty vector"
             (serialize []) => "[]")

       (fact "Serializing a map"
             (serialize {:a 1 :b 2 :c 3}) => "{\"a\":1,\"b\":2,\"c\":3}")

       (fact "Serializing an empty map"
             (serialize {}) => "{}")

       (fact "Serializing a string"
             (serialize "Hello, world!") => "\"Hello, world!\"")

       (fact "Serializing a keyword"
             (serialize :keyword) => "\"keyword\"")

       (fact "Serializing a number"
             (serialize 42) => "42")

       (fact "Serializing nil"
             (serialize nil) => "null")
       )


(facts "Testing the deserialize-board function"
       (let [serialized-board "[[5,3,0,0,7,0,0,0,0],[6,0,0,1,9,5,0,0,0],[0,9,8,0,0,0,0,6,0],[8,0,0,0,6,0,0,0,3],[4,0,0,8,0,3,0,0,1],[7,0,0,0,2,0,0,0,6],[0,6,0,0,0,0,2,8,0],[0,0,0,4,1,9,0,0,5],[0,0,0,0,8,0,0,7,9]]"
             board [[5 3 0 0 7 0 0 0 0]
                             [6 0 0 1 9 5 0 0 0]
                             [0 9 8 0 0 0 0 6 0]
                             [8 0 0 0 6 0 0 0 3]
                             [4 0 0 8 0 3 0 0 1]
                             [7 0 0 0 2 0 0 0 6]
                             [0 6 0 0 0 0 2 8 0]
                             [0 0 0 4 1 9 0 0 5]
                             [0 0 0 0 8 0 0 7 9]]]

         (fact "Deserializes a JSON string to a board"
               (deserialize-board serialized-board) => board)
         )
       )


(facts "Testing the fetch-best-result function"
       (let [expected-result {:user [[5 3 0 0 7 0 0 0 0]
                                     [6 0 0 1 9 5 0 0 0]
                                     [0 9 8 0 0 0 0 6 0]
                                     [8 0 0 0 6 0 0 0 3]
                                     [4 0 0 8 0 3 0 0 1]
                                     [7 0 0 0 2 0 0 0 6]
                                     [0 6 0 0 0 0 2 8 0]
                                     [0 0 0 4 1 9 0 0 5]
                                     [0 0 0 0 8 0 0 7 9]],
                              :time 10}
             expected-result-nil {:user nil,
                              :time nil}]

         ;(fact "Fetches the best result from the database -  no results in DB"
         ;      (fetch-best-result) => expected-result-nil
         ;)
         (fact "Fetches the best result from the database"
               (let [expected-result {:time 10M :user "Jelena Repac"}]
                 (insert-time [[5 3 0 0 7 0 0 0 0]
                               [6 0 0 1 9 5 0 0 0]
                               [0 9 8 0 0 0 0 6 0]
                               [8 0 0 0 6 0 0 0 3]
                               [4 0 0 8 0 3 0 0 1]
                               [7 0 0 0 2 0 0 0 6]
                               [0 6 0 0 0 0 2 8 0]
                               [0 0 0 4 1 9 0 0 5]
                               [0 0 0 0 8 0 0 7 9]]
                              [[5 3 4 6 7 8 9 1 2]
                               [6 7 2 1 9 5 3 4 8]
                               [1 9 8 3 4 2 5 6 7]
                               [8 5 9 7 6 1 4 2 3]
                               [4 2 6 8 5 3 7 9 1]
                               [7 1 3 9 2 4 8 5 6]
                               [9 6 1 5 3 7 2 8 4]
                               [2 8 7 4 1 9 6 3 5]
                               [3 4 5 2 8 6 1 7 9]]
                              10 "Jelena Repac" )
                 (fetch-best-result) => expected-result)
               )

         (jdbc/execute! db-spec ["DELETE FROM results"])
         )

       )

(facts "Testing fetch-leaderboard function"
       (fact "Fetches the leaderboard when there are no results in the database"
             (fetch-leaderboard) => {:first nil
                                     :second nil
                                     :third nil})

       (fact "Fetches the leaderboard when there are fewer than three results in the database"
             (let [mock-results {:first {:user "user1" :time 10M}
                                   :second {:user "user2" :time 20M}
                                   :third nil}]
               (insert-time [[5 3 0 0 7 0 0 0 0]
                             [6 0 0 1 9 5 0 0 0]
                             [0 9 8 0 0 0 0 6 0]
                             [8 0 0 0 6 0 0 0 3]
                             [4 0 0 8 0 3 0 0 1]
                             [7 0 0 0 2 0 0 0 6]
                             [0 6 0 0 0 0 2 8 0]
                             [0 0 0 4 1 9 0 0 5]
                             [0 0 0 0 8 0 0 7 9]]
                            [[5 3 4 6 7 8 9 1 2]
                             [6 7 2 1 9 5 3 4 8]
                             [1 9 8 3 4 2 5 6 7]
                             [8 5 9 7 6 1 4 2 3]
                             [4 2 6 8 5 3 7 9 1]
                             [7 1 3 9 2 4 8 5 6]
                             [9 6 1 5 3 7 2 8 4]
                             [2 8 7 4 1 9 6 3 5]
                             [3 4 5 2 8 6 1 7 9]]
                            10 "user1" )
               (insert-time [[5 3 0 0 7 0 0 0 0]
                             [6 0 0 1 9 5 0 0 0]
                             [0 9 8 0 0 0 0 6 0]
                             [8 0 0 0 6 0 0 0 3]
                             [4 0 0 8 0 3 0 0 1]
                             [7 0 0 0 2 0 0 0 6]
                             [0 6 0 0 0 0 2 8 0]
                             [0 0 0 4 1 9 0 0 5]
                             [0 0 0 0 8 0 0 7 9]]
                            [[5 3 4 6 7 8 9 1 2]
                             [6 7 2 1 9 5 3 4 8]
                             [1 9 8 3 4 2 5 6 7]
                             [8 5 9 7 6 1 4 2 3]
                             [4 2 6 8 5 3 7 9 1]
                             [7 1 3 9 2 4 8 5 6]
                             [9 6 1 5 3 7 2 8 4]
                             [2 8 7 4 1 9 6 3 5]
                             [3 4 5 2 8 6 1 7 9]]
                            20 "user2" )
               (fetch-leaderboard) => mock-results))
       (fact "Fetches the leaderboard when there are results in the database"
             (let [mock-results {:first {:user "user1" :time 10M}
                                  :second {:user "user2" :time 20M}
                                  :third {:user "user3" :time 30M}}]
               (insert-time [[5 3 0 0 7 0 0 0 0]
                             [6 0 0 1 9 5 0 0 0]
                             [0 9 8 0 0 0 0 6 0]
                             [8 0 0 0 6 0 0 0 3]
                             [4 0 0 8 0 3 0 0 1]
                             [7 0 0 0 2 0 0 0 6]
                             [0 6 0 0 0 0 2 8 0]
                             [0 0 0 4 1 9 0 0 5]
                             [0 0 0 0 8 0 0 7 9]]
                            [[5 3 4 6 7 8 9 1 2]
                             [6 7 2 1 9 5 3 4 8]
                             [1 9 8 3 4 2 5 6 7]
                             [8 5 9 7 6 1 4 2 3]
                             [4 2 6 8 5 3 7 9 1]
                             [7 1 3 9 2 4 8 5 6]
                             [9 6 1 5 3 7 2 8 4]
                             [2 8 7 4 1 9 6 3 5]
                             [3 4 5 2 8 6 1 7 9]]
                            30 "user3" )
               (fetch-leaderboard) => mock-results))
       (jdbc/execute! db-spec ["DELETE FROM results"])
  )

  (facts "Testing fetch-sudoku-board-hard function"
         (jdbc/execute! db-spec ["DELETE FROM sudoku_boards"])
         (fact "Returns nil when there are no results in the database"
               (fetch-sudoku-board-hard) => throws)
         (fact "Fetches a Sudoku board with 'Hard' difficulty when there are results in the database"
               (let [board [[5 3 0 0 7 0 0 0 0]
                             [6 0 0 1 9 5 0 0 0]
                             [0 9 8 0 0 0 0 6 0]
                             [8 0 0 0 6 0 0 0 3]
                             [4 0 0 8 0 3 0 0 1]
                             [7 0 0 0 2 0 0 0 6]
                             [0 6 0 0 0 0 2 8 0]
                             [0 0 0 4 1 9 0 0 5]
                             [0 0 0 0 8 0 0 7 9]],
                     solved_board [[5 3 4 6 7 8 9 1 2]
                                    [6 7 2 1 9 5 3 4 8]
                                    [1 9 8 3 4 2 5 6 7]
                                    [8 5 9 7 6 1 4 2 3]
                                    [4 2 6 8 5 3 7 9 1]
                                    [7 1 3 9 2 4 8 5 6]
                                    [9 6 1 5 3 7 2 8 4]
                                    [2 8 7 4 1 9 6 3 5]
                                    [3 4 5 2 8 6 1 7 9]],
                     difficulty "Hard"]
                 (insert-sudoku-board board solved_board difficulty)
                 (fetch-sudoku-board-hard) => {:board [[5 3 0 0 7 0 0 0 0]
                                                       [6 0 0 1 9 5 0 0 0]
                                                       [0 9 8 0 0 0 0 6 0]
                                                       [8 0 0 0 6 0 0 0 3]
                                                       [4 0 0 8 0 3 0 0 1]
                                                       [7 0 0 0 2 0 0 0 6]
                                                       [0 6 0 0 0 0 2 8 0]
                                                       [0 0 0 4 1 9 0 0 5]
                                                       [0 0 0 0 8 0 0 7 9]],
                                               :solved-board [[5 3 4 6 7 8 9 1 2]
                                                              [6 7 2 1 9 5 3 4 8]
                                                              [1 9 8 3 4 2 5 6 7]
                                                              [8 5 9 7 6 1 4 2 3]
                                                              [4 2 6 8 5 3 7 9 1]
                                                              [7 1 3 9 2 4 8 5 6]
                                                              [9 6 1 5 3 7 2 8 4]
                                                              [2 8 7 4 1 9 6 3 5]
                                                              [3 4 5 2 8 6 1 7 9]],
                                               :difficulty "Hard"}))
         (jdbc/execute! db-spec ["DELETE FROM sudoku_boards"])

         )
  (facts "Testing fetch-sudoku-board-easy function"
         (jdbc/execute! db-spec ["DELETE FROM sudoku_boards"])
         (fact "Returns nil when there are no results in the database"
               (fetch-sudoku-board-hard) => throws)
         (fact "Fetches a Sudoku board with 'Easy' difficulty when there are results in the database"
               (let [board [[5 3 4 0 7 8 0 0 2]
                            [6 0 0 1 9 5 0 4 8]
                            [1 9 8 0 0 2 0 6 0]
                            [8 0 9 7 6 1 0 2 3]
                            [4 0 6 8 0 3 7 9 1]
                            [7 1 0 0 2 0 0 0 6]
                            [0 6 0 5 0 0 2 8 0]
                            [0 8 7 4 1 9 6 3 5]
                            [0 4 5 0 8 6 0 7 9]],
                     solved_board [[5 3 4 6 7 8 9 1 2]
                                   [6 7 2 1 9 5 3 4 8]
                                   [1 9 8 3 4 2 5 6 7]
                                   [8 5 9 7 6 1 4 2 3]
                                   [4 2 6 8 5 3 7 9 1]
                                   [7 1 3 9 2 4 8 5 6]
                                   [9 6 1 5 3 7 2 8 4]
                                   [2 8 7 4 1 9 6 3 5]
                                   [3 4 5 2 8 6 1 7 9]],
                     difficulty "Easy"]
                 (insert-sudoku-board board solved_board difficulty)
                 (fetch-sudoku-board-easy) => {:board [[5 3 4 0 7 8 0 0 2]
                                                       [6 0 0 1 9 5 0 4 8]
                                                       [1 9 8 0 0 2 0 6 0]
                                                       [8 0 9 7 6 1 0 2 3]
                                                       [4 0 6 8 0 3 7 9 1]
                                                       [7 1 0 0 2 0 0 0 6]
                                                       [0 6 0 5 0 0 2 8 0]
                                                       [0 8 7 4 1 9 6 3 5]
                                                       [0 4 5 0 8 6 0 7 9]],
                                               :solved-board [[5 3 4 6 7 8 9 1 2]
                                                              [6 7 2 1 9 5 3 4 8]
                                                              [1 9 8 3 4 2 5 6 7]
                                                              [8 5 9 7 6 1 4 2 3]
                                                              [4 2 6 8 5 3 7 9 1]
                                                              [7 1 3 9 2 4 8 5 6]
                                                              [9 6 1 5 3 7 2 8 4]
                                                              [2 8 7 4 1 9 6 3 5]
                                                              [3 4 5 2 8 6 1 7 9]],
                                               :difficulty "Easy"}))
         (jdbc/execute! db-spec ["DELETE FROM sudoku_boards"])

         )

  (facts "Testing fetch-sudoku-board-medium function"
         (jdbc/execute! db-spec ["DELETE FROM sudoku_boards"])
         (fact "Returns nil when there are no results in the database"
               (fetch-sudoku-board-hard) => throws)
         (fact "Fetches a Sudoku board with 'Medium' difficulty when there are results in the database"
               (let [board [[0 0 0 6 9 1 7 4 3]
                            [0 0 0 0 0 0 0 2 1]
                            [0 7 4 0 0 8 0 0 0]
                            [4 0 0 0 0 0 0 0 0]
                            [5 9 0 0 0 0 0 3 4]
                            [6 0 0 4 0 3 0 5 7]
                            [7 0 0 0 0 0 1 6 2]
                            [0 0 1 0 0 6 0 0 0]
                            [0 8 0 1 0 0 4 0 9]],
                     solved_board [[8 2 5 6 9 1 7 4 3]
                                   [9 6 3 7 4 5 8 2 1]
                                   [1 7 4 2 3 8 5 9 6]
                                   [4 3 7 5 6 9 2 1 8]
                                   [5 9 2 8 1 7 6 3 4]
                                   [6 1 8 4 2 3 9 5 7]
                                   [7 5 9 3 8 4 1 6 2]
                                   [2 4 1 9 7 6 3 8 5]
                                   [3 8 6 1 5 2 4 7 9]],
                     difficulty "Medium"]
                 (insert-sudoku-board board solved_board difficulty)
                 (fetch-sudoku-board-medium) => {:board [[0 0 0 6 9 1 7 4 3]
                                                       [0 0 0 0 0 0 0 2 1]
                                                       [0 7 4 0 0 8 0 0 0]
                                                       [4 0 0 0 0 0 0 0 0]
                                                       [5 9 0 0 0 0 0 3 4]
                                                       [6 0 0 4 0 3 0 5 7]
                                                       [7 0 0 0 0 0 1 6 2]
                                                       [0 0 1 0 0 6 0 0 0]
                                                       [0 8 0 1 0 0 4 0 9]],
                                               :solved-board [[8 2 5 6 9 1 7 4 3]
                                                              [9 6 3 7 4 5 8 2 1]
                                                              [1 7 4 2 3 8 5 9 6]
                                                              [4 3 7 5 6 9 2 1 8]
                                                              [5 9 2 8 1 7 6 3 4]
                                                              [6 1 8 4 2 3 9 5 7]
                                                              [7 5 9 3 8 4 1 6 2]
                                                              [2 4 1 9 7 6 3 8 5]
                                                              [3 8 6 1 5 2 4 7 9]],
                                               :difficulty "Medium"}))
         (jdbc/execute! db-spec ["DELETE FROM sudoku_boards"])

         )




  (facts "Testing check-cell-value function"
         (fact "Returns the result of checking a cell value"
               (let [request {:params {"board" [[5 3 0 0 7 0 0 0 0]
                                                [6 0 0 1 9 5 0 0 0]
                                                [0 9 8 0 0 0 0 6 0]
                                                [8 0 0 0 6 0 0 0 3]
                                                [4 0 0 8 0 3 0 0 1]
                                                [7 0 0 0 2 0 0 0 6]
                                                [0 6 0 0 0 0 2 8 0]
                                                [0 0 0 4 1 9 0 0 5]
                                                [0 0 0 0 8 0 0 7 9]
                                                ]
                                       "rowIndex" 0
                                       "columnIndex" 2
                                       "value" 4}
                              }
                     result (check-cell-value request)])
               )
       (fact "Handles missing parameters "
         (let [request []]
           (check-cell-value request) => {:status 400
                                       :headers {"Content-Type" "application/json"}
                                       :body "error:Missing or invalid request"})

       )
  )


  (facts "Testing app-routes function"
         (fact "Returns a 200 response with generated Sudoku board when GET /board-hard route is called"
               (let [board [[5 3 0 0 7 0 0 0 0]
                                   [6 0 0 1 9 5 0 0 0]
                                   [0 9 8 0 0 0 0 6 0]
                                   [8 0 0 0 6 0 0 0 3]
                                   [4 0 0 8 0 3 0 0 1]
                                   [7 0 0 0 2 0 0 0 6]
                                   [0 6 0 0 0 0 2 8 0]
                                   [0 0 0 4 1 9 0 0 5]
                                   [0 0 0 0 8 0 0 7 9]],
                            solved_board [[5 3 4 6 7 8 9 1 2]
                                          [6 7 2 1 9 5 3 4 8]
                                          [1 9 8 3 4 2 5 6 7]
                                          [8 5 9 7 6 1 4 2 3]
                                          [4 2 6 8 5 3 7 9 1]
                                          [7 1 3 9 2 4 8 5 6]
                                          [9 6 1 5 3 7 2 8 4]
                                          [2 8 7 4 1 9 6 3 5]
                                          [3 4 5 2 8 6 1 7 9]],
                            difficulty "Hard"]
                     (insert-sudoku-board board solved_board difficulty))
                  (let [ response (app-routes (mock/request :get "/board-hard"))]
                    (-> response :status) => 200
                    (-> response :body) =>  "{\"board\":[[5,3,0,0,7,0,0,0,0],[6,0,0,1,9,5,0,0,0],[0,9,8,0,0,0,0,6,0],[8,0,0,0,6,0,0,0,3],[4,0,0,8,0,3,0,0,1],[7,0,0,0,2,0,0,0,6],[0,6,0,0,0,0,2,8,0],[0,0,0,4,1,9,0,0,5],[0,0,0,0,8,0,0,7,9]],\"difficulty\":\"Hard\",\"solved-board\":[[5,3,4,6,7,8,9,1,2],[6,7,2,1,9,5,3,4,8],[1,9,8,3,4,2,5,6,7],[8,5,9,7,6,1,4,2,3],[4,2,6,8,5,3,7,9,1],[7,1,3,9,2,4,8,5,6],[9,6,1,5,3,7,2,8,4],[2,8,7,4,1,9,6,3,5],[3,4,5,2,8,6,1,7,9]]}"
                    (-> response :headers ) =>  {"Content-Type" "application/json",
                                                 "Access-Control-Allow-Origin" "http://localhost:3000",
                                                 "Access-Control-Allow-Headers" "Content-Type",
                                                 "Access-Control-Allow-Methods" "GET, POST, OPTIONS",
                                                 "Access-Control-Allow-Credentials" "true"},

                    )
               (jdbc/execute! db-spec ["DELETE FROM sudoku_boards"])


         )
         (fact "Returns a 200 response with generated Sudoku board when GET /board-medium route is called"
               (let [board [[0 0 0 6 9 1 7 4 3]
                                  [0 0 0 0 0 0 0 2 1]
                                  [0 7 4 0 0 8 0 0 0]
                                  [4 0 0 0 0 0 0 0 0]
                                  [5 9 0 0 0 0 0 3 4]
                                  [6 0 0 4 0 3 0 5 7]
                                  [7 0 0 0 0 0 1 6 2]
                                  [0 0 1 0 0 6 0 0 0]
                                  [0 8 0 1 0 0 4 0 9]],
                     solved_board [[8 2 5 6 9 1 7 4 3]
                                   [9 6 3 7 4 5 8 2 1]
                                   [1 7 4 2 3 8 5 9 6]
                                   [4 3 7 5 6 9 2 1 8]
                                   [5 9 2 8 1 7 6 3 4]
                                   [6 1 8 4 2 3 9 5 7]
                                   [7 5 9 3 8 4 1 6 2]
                                   [2 4 1 9 7 6 3 8 5]
                                   [3 8 6 1 5 2 4 7 9]],
                     difficulty "Medium"]
                 (insert-sudoku-board board solved_board difficulty))
               (let [ response (app-routes (mock/request :get "/board-medium"))]
                 (-> response :status) => 200
                 (-> response :body) =>  "{\"board\":[[0,0,0,6,9,1,7,4,3],[0,0,0,0,0,0,0,2,1],[0,7,4,0,0,8,0,0,0],[4,0,0,0,0,0,0,0,0],[5,9,0,0,0,0,0,3,4],[6,0,0,4,0,3,0,5,7],[7,0,0,0,0,0,1,6,2],[0,0,1,0,0,6,0,0,0],[0,8,0,1,0,0,4,0,9]],\"difficulty\":\"Medium\",\"solved-board\":[[8,2,5,6,9,1,7,4,3],[9,6,3,7,4,5,8,2,1],[1,7,4,2,3,8,5,9,6],[4,3,7,5,6,9,2,1,8],[5,9,2,8,1,7,6,3,4],[6,1,8,4,2,3,9,5,7],[7,5,9,3,8,4,1,6,2],[2,4,1,9,7,6,3,8,5],[3,8,6,1,5,2,4,7,9]]}"
                 (-> response :headers ) =>  {"Content-Type" "application/json",
                                              "Access-Control-Allow-Origin" "http://localhost:3000",
                                              "Access-Control-Allow-Headers" "Content-Type",
                                              "Access-Control-Allow-Methods" "GET, POST, OPTIONS",
                                              "Access-Control-Allow-Credentials" "true"},

                 )
               (jdbc/execute! db-spec ["DELETE FROM sudoku_boards"])


               )

         (fact "Returns a 200 response with generated Sudoku board when GET /board-easy route is called"
               (let [board [[5 3 4 0 7 8 0 0 2]
                            [6 0 0 1 9 5 0 4 8]
                            [1 9 8 0 0 2 0 6 0]
                            [8 0 9 7 6 1 0 2 3]
                            [4 0 6 8 0 3 7 9 1]
                            [7 1 0 0 2 0 0 0 6]
                            [0 6 0 5 0 0 2 8 0]
                            [0 8 7 4 1 9 6 3 5]
                            [0 4 5 0 8 6 0 7 9]],
                     solved_board [[5 3 4 6 7 8 9 1 2]
                                   [6 7 2 1 9 5 3 4 8]
                                   [1 9 8 3 4 2 5 6 7]
                                   [8 5 9 7 6 1 4 2 3]
                                   [4 2 6 8 5 3 7 9 1]
                                   [7 1 3 9 2 4 8 5 6]
                                   [9 6 1 5 3 7 2 8 4]
                                   [2 8 7 4 1 9 6 3 5]
                                   [3 4 5 2 8 6 1 7 9]]
                     difficulty "Easy"]
                 (insert-sudoku-board board solved_board difficulty))
               (let [ response (app-routes (mock/request :get "/board-easy"))]
                 (-> response :status) => 200
                 (-> response :body) => "{\"board\":[[5,3,4,0,7,8,0,0,2],[6,0,0,1,9,5,0,4,8],[1,9,8,0,0,2,0,6,0],[8,0,9,7,6,1,0,2,3],[4,0,6,8,0,3,7,9,1],[7,1,0,0,2,0,0,0,6],[0,6,0,5,0,0,2,8,0],[0,8,7,4,1,9,6,3,5],[0,4,5,0,8,6,0,7,9]],\"difficulty\":\"Easy\",\"solved-board\":[[5,3,4,6,7,8,9,1,2],[6,7,2,1,9,5,3,4,8],[1,9,8,3,4,2,5,6,7],[8,5,9,7,6,1,4,2,3],[4,2,6,8,5,3,7,9,1],[7,1,3,9,2,4,8,5,6],[9,6,1,5,3,7,2,8,4],[2,8,7,4,1,9,6,3,5],[3,4,5,2,8,6,1,7,9]]}"
                 (-> response :headers ) =>  {"Content-Type" "application/json",
                                              "Access-Control-Allow-Origin" "http://localhost:3000",
                                              "Access-Control-Allow-Headers" "Content-Type",
                                              "Access-Control-Allow-Methods" "GET, POST, OPTIONS",
                                              "Access-Control-Allow-Credentials" "true"},

                 )
               (jdbc/execute! db-spec ["DELETE FROM sudoku_boards"])


               )
         ;(fact "Returns a 200 response with result check-cell-value when GET /check-cell-value is called"
         ;      (let [board [[5 3 4 0 7 8 0 0 2]
         ;                   [6 0 0 1 9 5 0 4 8]
         ;                   [1 9 8 0 0 2 0 6 0]
         ;                   [8 0 9 7 6 1 0 2 3]
         ;                   [4 0 6 8 0 3 7 9 1]
         ;                   [7 1 0 0 2 0 0 0 6]
         ;                   [0 6 0 5 0 0 2 8 0]
         ;                   [0 8 7 4 1 9 6 3 5]
         ;                   [0 4 5 0 8 6 0 7 9]],
         ;            row 0,
         ;            col 0,
         ;            value 10]
         ;         (sudoku-solver.if-valid/cell-valid? board row col value))
         ;      (let [ response (app-routes (mock/request :get "/check-cell-value"))]
         ;        (-> response :status) => 200
         ;        (-> response :body) => "{\"board\":[[5,3,4,0,7,8,0,0,2],[6,0,0,1,9,5,0,4,8],[1,9,8,0,0,2,0,6,0],[8,0,9,7,6,1,0,2,3],[4,0,6,8,0,3,7,9,1],[7,1,0,0,2,0,0,0,6],[0,6,0,5,0,0,2,8,0],[0,8,7,4,1,9,6,3,5],[0,4,5,0,8,6,0,7,9]],\"difficulty\":\"Easy\",\"solved-board\":[[5,3,4,6,7,8,9,1,2],[6,7,2,1,9,5,3,4,8],[1,9,8,3,4,2,5,6,7],[8,5,9,7,6,1,4,2,3],[4,2,6,8,5,3,7,9,1],[7,1,3,9,2,4,8,5,6],[9,6,1,5,3,7,2,8,4],[2,8,7,4,1,9,6,3,5],[3,4,5,2,8,6,1,7,9]]}"
         ;        (-> response :headers ) =>  {"Content-Type" "application/json",
         ;                                     "Access-Control-Allow-Origin" "http://localhost:3000",
         ;                                     "Access-Control-Allow-Headers" "Content-Type",
         ;                                     "Access-Control-Allow-Methods" "GET, POST, OPTIONS",
         ;                                     "Access-Control-Allow-Credentials" "true"},
         ;
         ;        )
         ;      (jdbc/execute! db-spec ["DELETE FROM sudoku_boards"])
         ;
         ;
         ;      )

         )

  )




