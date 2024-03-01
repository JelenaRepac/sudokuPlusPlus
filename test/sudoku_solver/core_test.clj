(ns sudoku-solver.core-test
  (:require [sudoku-solver.core :refer :all]
            [midje.sweet :refer :all]))
;remove-spaces
(facts
  (fact "Testing remove-spaces with a string containing spaces"
        (remove-spaces "hello world")
        => "helloworld")

  (fact "Testing remove-spaces with an empty string"
        (remove-spaces "")
        => "")

  (fact "Testing remove-spaces with a string containing no spaces"
        (remove-spaces "sudoku")
        => "sudoku")

  (fact "Testing remove-spaces with a string containing only spaces"
        (remove-spaces "    ")
        => "")

  (fact "Testing remove-spaces with nil"
        (remove-spaces nil)
        => throws))

;find-zero-indexes
(facts
  (fact "Testing find-zero-indexes with a board containing zeros"
      (find-zero-indexes [[1 2 3 4 5 6 7 8 9]
                                         [0 0 0 0 0 0 0 0 0]
                                         [1 2 3 4 5 6 7 8 9]
                                         [0 0 0 0 0 0 0 0 0]
                                         [1 2 3 4 5 6 7 8 9]
                                         [0 0 0 0 0 0 0 0 0]
                                         [1 2 3 4 5 6 7 8 9]
                                         [0 0 0 0 0 0 0 0 0]
                                         [1 2 3 4 5 6 7 8 9]])
      => (just #{[1 0] [1 1] [1 2] [1 3] [1 4] [1 5] [1 6] [1 7] [1 8]
           [3 0] [3 1] [3 2] [3 3] [3 4] [3 5] [3 6] [3 7] [3 8]
           [5 0] [5 1] [5 2] [5 3] [5 4] [5 5] [5 6] [5 7] [5 8]
           [7 0] [7 1] [7 2] [7 3] [7 4] [7 5] [7 6] [7 7] [7 8]}))

(fact "Testing find-zero-indexes with a board containing no zeros"
      (find-zero-indexes [[1 2 3 4 5 6 7 8 9]
                                         [4 5 6 7 8 9 1 2 3]
                                         [7 8 9 1 2 3 4 5 6]
                                         [2 3 4 5 6 7 8 9 1]
                                         [5 6 7 8 9 1 2 3 4]
                                         [8 9 1 2 3 4 5 6 7]
                                         [3 4 5 6 7 8 9 1 2]
                                         [6 7 8 9 1 2 3 4 5]
                                         [9 1 2 3 4 5 6 7 8]])
      => (just #{})))

;divisible-by-three?
(facts
  (fact "Testing divisible-by-three? with a number divisible by three"
        (divisible-by-three? 9) => true)

  (fact "Testing divisible-by-three? with a number not divisible by three"
        (divisible-by-three? 7) => false)

  (fact "Testing divisible-by-three? with zero"
        (divisible-by-three? 0) => true)
  )

;valid-numbers
(facts
  (fact "Testing valid-numbers with"
      (valid-numbers [[5 3 0 0 7 0 0 0 0]
                                     [6 0 0 1 9 5 0 0 0]
                                     [0 9 8 0 0 0 0 6 0]
                                     [8 0 0 0 6 0 0 0 3]
                                     [4 0 0 8 0 3 0 0 1]
                                     [7 0 0 0 2 0 0 0 6]
                                     [0 6 0 0 0 0 2 8 0]
                                     [0 0 0 4 1 9 0 0 5]
                                     [0 0 0 0 8 0 0 7 9]]
                                    0 2)
      => (just #{1 2 4}))

(fact "Testing valid-numbers with an empty board"
      (valid-numbers (vec (repeat 9 (vec (repeat 9 nil)))) 0 0)
      => (just #{1 2 3 4 5 6 7 8 9})
      )

(fact "Testing valid-numbers with a partially filled board"
      (valid-numbers board 1 1)
      => (just #{2 4 7 })))

;count-valid-numbers
(facts
  (fact "Testing count-valid-numbers"
      (count-valid-numbers board [0 2])
      => 3)
(fact "Testing count-valid-numbers with empty board"
      (count-valid-numbers (vec (repeat 9 (vec (repeat 9 nil)))) [0 0])
      => 9)
  (fact "Testing count-valid-numbers with full board"
        (count-valid-numbers (solve board) [0 0]) => 0)
  )

;count-filled cells
(facts
  (fact "Testing count-filled cells"
        (count-filled-cells board)
        => 30)
  (fact "Testing count-filled cells"
        (count-filled-cells (vec (repeat 9 (vec (repeat 9 0)))))
        => 0)
  )

;count-filled-cells-in-block
(facts
  (fact "Testing count-filled-cells-in-block with a block containing filled cells"
        (count-filled-cells-in-block [[1 2 3 4 5 6 7 8 9]
                                                     [4 5 6 7 8 9 1 2 3]
                                                     [7 8 9 1 2 3 4 5 6]
                                                     [2 3 4 5 6 7 8 9 1]
                                                     [5 6 7 8 9 1 2 3 4]
                                                     [8 9 1 2 3 4 5 6 7]
                                                     [3 4 5 6 7 8 9 1 2]
                                                     [6 7 8 9 1 2 3 4 5]
                                                     [9 1 2 3 4 5 6 7 8]]
                                                    0 0)
        => 9)

  (fact "Testing count-filled-cells-in-block with a block containing empty cells"
        (count-filled-cells-in-block [[1 2 3 4 5 6 7 8 9]
                                                     [4 5 6 7 8 9 1 2 3]
                                                     [7 8 9 1 2 7 4 5 6]
                                                     [2 3 4 0 0 0 8 9 1]
                                                     [5 6 7 0 0 0 2 3 4]
                                                     [8 9 1 0 0 0 5 6 7]
                                                     [3 4 5 6 7 8 9 1 2]
                                                     [6 7 8 9 1 2 3 4 5]
                                                     [9 1 2 3 4 5 6 7 8]]
                                                    3 3)
        => 0)
  (fact "Testing count-filled-cells-in-block with a block containing partially filled cells"
        (count-filled-cells-in-block [[5 3 0 0 0 0 0 0 0]
                                                     [6 7 0 0 0 0 0 0 0]
                                                     [1 9 0 0 0 0 0 0 0]
                                                     [0 0 0 0 0 0 0 0 0]
                                                     [0 0 0 0 0 0 0 0 0]
                                                     [0 0 0 0 0 0 0 0 0]
                                                     [0 0 0 0 0 0 0 0 0]
                                                     [0 0 0 0 0 0 0 0 0]
                                                     [0 0 0 0 0 0 0 0 0]]
                                                    0 0)
        => 6))

;count-number
(facts
  (fact "Testing count-number with a board containing multiple occurrences of the number"
        (count-number [[5 3 4 6 7 8 9 1 2]
                                      [6 7 2 1 9 5 3 4 8]
                                      [1 9 8 3 4 2 5 6 7]
                                      [8 5 9 7 6 1 4 2 3]
                                      [4 2 6 8 5 3 7 9 1]
                                      [7 1 3 9 2 4 8 5 6]
                                      [9 6 1 5 3 7 2 8 4]
                                      [2 8 7 4 1 9 6 3 5]
                                      [3 4 5 2 8 6 1 7 9]]
                                     1)
        => 9)
  (fact "Testing count-number with a board containing no occurrences of the number"
        (count-number [[5 3 0 0 7 0 0 0 0]
                       [6 0 0 1 9 5 0 0 0]
                       [0 9 8 0 0 0 0 6 0]
                       [8 0 0 0 6 0 0 0 3]
                       [4 0 0 8 0 3 0 0 1]
                       [7 0 0 0 0 0 0 0 6]
                       [0 6 0 0 0 0 7 8 0]
                       [0 0 0 4 1 9 0 0 5]
                       [0 0 0 0 8 0 0 7 9]]
                                    2 )
        => 0)
  (fact "Testing count-number with a board containing multiple occurrences of the number"
        (count-number [[5 3 0 0 7 0 0 0 0]
                       [6 0 0 1 9 5 0 0 0]
                       [0 9 8 0 0 0 0 6 0]
                       [8 0 0 0 6 0 0 0 3]
                       [4 0 0 8 0 3 0 0 1]
                       [7 0 0 0 0 0 0 0 6]
                       [0 6 0 0 0 0 7 8 0]
                       [0 0 0 4 1 9 0 0 5]
                       [0 0 0 0 8 0 0 7 9]]
                      5 )
        => 3))

;number-filled
(facts
  (fact "Testing number-filled? with a board where all cells are filled with the number"
      (number-filled? [[5 3 4 6 7 8 9 1 2]
                                      [6 7 2 1 9 5 3 4 8]
                                      [1 9 8 3 4 2 5 6 7]
                                      [8 5 9 7 6 1 4 2 3]
                                      [4 2 6 8 5 3 7 9 1]
                                      [7 1 3 9 2 4 8 5 6]
                                      [9 6 1 5 3 7 2 8 4]
                                      [2 8 7 4 1 9 6 3 5]
                                      [3 4 5 2 8 6 1 7 9]]
                                     1)
      => true)

(fact "Testing number-filled? with a board where not all cells are filled with the number"
      (number-filled? [[5 3 4 6 7 8 9 1 2]
                                      [6 7 2 1 9 5 3 4 8]
                                      [1 9 8 3 4 2 5 6 7]
                                      [8 5 9 7 6 1 4 2 3]
                                      [4 2 6 8 5 3 7 9 1]
                                      [7 1 3 9 2 4 8 5 6]
                                      [9 6 1 5 3 7 2 8 4]
                                      [2 8 7 4 1 9 6 3 5]
                                      [3 4 5 2 8 6 1 7 9]]
                                     0)
      => false))


(facts
  (fact "Testing filled-cells-distribution with evenly distributed filled cells"
        (let [board [[5 3 4 6 7 8 9 1 2]
                     [6 7 2 1 9 5 3 4 8]
                     [1 9 8 3 4 2 5 6 7]
                     [8 5 9 7 6 1 4 2 3]
                     [4 2 6 8 5 3 7 9 1]
                     [7 1 3 9 2 4 8 5 6]
                     [9 6 1 5 3 7 2 8 4]
                     [2 8 7 4 1 9 6 3 5]
                     [3 4 5 2 8 6 1 7 9]]]
          (filled-cells-distribution board))
        => "Even distribution")

  (fact "Testing filled-cells-distribution with clustered filled cells"
        (let [board [[5 3 0 0 0 0 0 0 0]
                     [6 7 2 1 9 5 3 4 8]
                     [1 9 8 3 4 2 5 6 7]
                     [8 5 9 7 6 1 4 2 3]
                     [4 2 6 8 5 3 7 9 1]
                     [7 1 3 9 2 4 8 5 6]
                     [9 6 1 5 3 7 2 8 4]
                     [2 8 7 4 1 9 6 3 5]
                     [3 4 5 2 8 6 1 7 9]]]
          (filled-cells-distribution board))
        => "Clustered distribution"))