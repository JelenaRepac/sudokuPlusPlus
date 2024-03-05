(ns sudoku-solver.algorithms.logic-library-test
  (:require [midje.sweet :refer :all]
            [sudoku-solver.algorithms.logic-library :refer :all]))



(facts "Testing get-square function"
       (let [board [[1 2 3 4 5 6 7 8 9]
                   [4 5 6 7 8 9 1 2 3]
                   [7 8 9 1 2 3 4 5 6]
                   [2 3 4 5 6 7 8 9 1]
                   [5 6 7 8 9 1 2 3 4]
                   [8 9 1 2 3 4 5 6 7]
                   [3 4 5 6 7 8 9 1 2]
                   [6 7 8 9 1 2 3 4 5]
                   [9 1 2 3 4 5 6 7 8]]]
         (fact "Testing get-square for top-left corner"
               (get-square board 0 0) => [1 2 3 4 5 6 7 8 9])

         (fact "Testing get-square for top-right corner"
               (get-square board 0 6) => [7 8 9 1 2 3 4 5 6])

         (fact "Testing get-square for bottom-left corner"
               (get-square board 6 0) => [3 4 5 6 7 8 9 1 2])

         (fact "Testing get-square for bottom-right corner"
               (get-square board 6 6) => [9 1 2 3 4 5 6 7 8])

         (fact "Testing get-square for middle of the board"
               (get-square board 3 3) => [5 6 7 8 9 1 2 3 4])
         ))
