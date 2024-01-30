(ns sudoku-solver.if-valid)

--Checking if the sudoku board is valid

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






