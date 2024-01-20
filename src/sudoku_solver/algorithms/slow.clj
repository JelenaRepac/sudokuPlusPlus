(ns sudoku-solver.algorithms.slow
  (:require
  [sudoku-solver.if-valid :as if-valid]
  [sudoku-solver.core :as core]))


(defn random-solve-helper [indexes i new-board]
  (if (= i (count indexes))
    (if (if-valid/valid? new-board)
      new-board
      (random-solve-helper indexes 0 (vec (repeat 9 (vec (repeat 9 0))))))
    (let [sorted-indexes (sort-by #(core/count-valid-numbers new-board %) indexes)
          current-index (nth sorted-indexes i)
          [row col] current-index
          possible-numbers (core/valid-numbers new-board row col)]
      (println possible-numbers)
      (if (seq possible-numbers)
        (if (not= 1 (count possible-numbers))
          (recur indexes (inc i) (assoc-in new-board [row col] (first possible-numbers)))
          (let [num (rand-nth possible-numbers)]
            (recur indexes (inc i) (assoc-in new-board [row col] num))))
        (recur indexes (inc i) new-board)))))

(defn random-solve [board]
  (let [indexes (core/find-zero-indexes board)]
    (random-solve-helper indexes 0 board)))

;; Example usage:
(def sudoku-board
  [[5 3 0 0 7 0 0 0 0]
   [6 0 0 1 9 5 0 0 0]
   [0 9 8 0 0 0 0 6 0]
   [8 0 0 0 6 0 0 0 3]
   [4 0 0 8 0 3 0 0 1]
   [7 0 0 0 2 0 0 0 6]
   [0 6 0 0 0 0 2 8 0]
   [0 0 0 4 1 9 0 0 5]
   [0 0 0 0 8 0 0 7 9]])

(core/print-sudoku (random-solve sudoku-board))
