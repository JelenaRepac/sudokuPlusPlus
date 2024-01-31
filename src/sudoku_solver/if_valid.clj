(ns sudoku-solver.if-valid
  (:require [sudoku-solver.core :refer :all]))


(defn valid? [x]
  (let [distinct-numbers(set x)]
    (= 9 (count x))
    (= 9 (count distinct-numbers))))


(defn subgrid-checker [board]
  (loop [grid-index 0
         result true]
    (if (< grid-index 9)
      (let [row (* 3 (quot grid-index 3))
            col (* 3 (mod grid-index 3))
            subgrid (for [i (range 3) j (range 3)]
                      (get-in board [(+ row i) (+ col j)]))]
        (recur (inc grid-index) (and result (valid? (flatten subgrid)))))
      result)))

(defn cell-valid? [board row col value]
  (let [current-row (get board row)
        current-column (map #(nth % col) board)
        current-subgrid (sudoku-solver.core/get-square board col row)]
    (println "Current Row:" current-row)
    (println "Current Column:" current-column)
    (println "Current Subgrid:" current-subgrid)

    (and (not= value 0)
         (not-any? #{value} current-row)
         (not-any? #{value} current-column)
         (not-any? #{value} (flatten current-subgrid))
        )))

(def example-board-1
  [[1 2 0 4 5 6 7 8 0]
   [0 5 0 7 0 9 1 2 3]
   [7 8 0 0 0 3 4 5 6]
   [2 3 0 0 6 4 8 9 0]
   [5 6 0 0 9 7 2 3 0]
   [8 9 0 0 3 1 5 0 0]
   [3 1 0 0 0 5 9 7 0]
   [6 4 0 0 0 8 3 1 0]
   [0 7 8 3 0 2 6 4 5]])
(cell-valid? example-board-1 0 2 1)
(defn sudoku-solved? [board]
  (loop [row 0
         column 0
         result true]
    (if (< row 9)
      (let [current-row (get board row)
            current-column (map #(nth % column) board)]
        (recur (inc row) (inc column) (and result (valid? current-row) (valid? current-column)))
        )
      (and result (subgrid-checker board)))))






