(ns if-valid_test
  (:require [midje.sweet :refer :all]
            [sudoku-solver.if-valid :refer :all]))

(facts "Testing the valid? function"
       (fact "Testing a valid list"
             (valid? [1 2 3 4 5 6 7 8 9]) => true)

       (fact "Testing an invalid list with duplicate values"
             (valid? [1 2 3 4 5 6 7 8 8]) => false)

       (fact "Testing an invalid list with fewer than 9 elements"
             (valid? [1 2 3 4 5 6 7 8]) => false)

       (fact "Testing an invalid list with more than 9 elements"
             (valid? [1 2 3 4 5 6 7 8 9 10]) => false))
(facts "Testing the subgrid-checker function"
       (let [valid-board [[5 3 4 6 7 8 9 1 2]
                          [6 7 2 1 9 5 3 4 8]
                          [1 9 8 3 4 2 5 6 7]
                          [8 5 9 7 6 1 4 2 3]
                          [4 2 6 8 5 3 7 9 1]
                          [7 1 3 9 2 4 8 5 6]
                          [9 6 1 5 3 7 2 8 4]
                          [2 8 7 4 1 9 6 3 5]
                          [3 4 5 2 8 6 1 7 9]]]
         (fact "Testing valid subgrid"
               (subgrid-checker valid-board) => true)
         (fact "Testing invalid subgrid"
               (subgrid-checker [[5 3 0 0 7 0 0 0 0]
                                 [6 7 2 1 9 5 3 4 8]
                                 [1 9 8 3 4 2 5 6 7]
                                 [8 5 9 7 6 1 4 2 3]
                                 [4 2 6 8 5 3 7 9 1]
                                 [7 1 3 9 2 4 8 5 6]
                                 [9 6 1 5 3 7 2 8 4]
                                 [2 8 7 4 1 9 6 3 5]
                                 [3 4 5 2 8 6 1 7 9]]) => false)))
(facts "Testing the get-square function"
       (let [board [[5 3 0 0 7 0 0 0 0]
                    [6 0 0 1 9 5 0 0 0]
                    [0 9 8 0 0 0 0 6 0]
                    [8 0 0 0 6 0 0 0 3]
                    [4 0 0 8 0 3 0 0 1]
                    [7 0 0 0 2 0 0 0 6]
                    [0 6 0 0 0 0 2 8 0]
                    [0 0 0 4 1 9 0 0 5]
                    [0 0 0 0 8 0 0 7 9]]]
         (fact "Testing get-square for a valid cell"
               (get-square board 0 0) => [5 3 0 6 0 0 0 9 8])
         (fact "Testing get-square for an invalid cell"
               (get-square board 1 2) => [6 0 0 1 9 5 0 0 0])))
(facts "Testing the cell-valid? function"
       (let [board [[5 3 0 0 7 0 0 0 0]
                    [6 0 0 1 9 5 0 0 0]
                    [0 9 8 0 0 0 0 6 0]
                    [8 0 0 0 6 0 0 0 3]
                    [4 0 0 8 0 3 0 0 1]
                    [7 0 0 0 2 0 0 0 6]
                    [0 6 0 0 0 0 2 8 0]
                    [0 0 0 4 1 9 0 0 5]
                    [0 0 0 0 8 0 0 7 9]]]

         (fact "Testing valid placement"
               (cell-valid? board 0 2 4) => true
               (cell-valid? board 1 1 2) => true
               (cell-valid? board 8 6 1) => true)

         (fact "Testing invalid placement"
               (cell-valid? board 0 2 5) => false
               (cell-valid? board 1 1 6) => false
               (cell-valid? board 2 2 8) => false)))
(facts "Testing the sudoku-solved? function"
       (let [valid-board [[5 3 4 6 7 8 9 1 2]
                          [6 7 2 1 9 5 3 4 8]
                          [1 9 8 3 4 2 5 6 7]
                          [8 5 9 7 6 1 4 2 3]
                          [4 2 6 8 5 3 7 9 1]
                          [7 1 3 9 2 4 8 5 6]
                          [9 6 1 5 3 7 2 8 4]
                          [2 8 7 4 1 9 6 3 5]
                          [3 4 5 2 8 6 1 7 9]]]

         (fact "Testing a valid Sudoku board"
               (sudoku-solved? valid-board) => true)

         (fact "Testing an invalid Sudoku board"
               (sudoku-solved? [[5 3 0 0 7 0 0 0 0]
                                [6 7 2 1 9 5 3 4 8]
                                [1 9 8 3 4 2 5 6 7]
                                [8 5 9 7 6 1 4 2 3]
                                [4 2 6 8 5 3 7 9 1]
                                [7 1 3 9 2 4 8 5 6]
                                [9 6 1 5 3 7 2 8 4]
                                [2 8 7 4 1 9 6 3 5]
                                [3 4 5 2 8 6 1 7 9]]) => false)))