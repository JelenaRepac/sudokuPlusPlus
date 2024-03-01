(ns sudokuApi_test
  (:require [midje.sweet :refer :all]
            [clojure.java.jdbc :as jdbc]
    [sudoku-solver.controller.sudokuApi :refer :all] ))

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
                 difficulty VARCHAR(50))"]) => truthy))





