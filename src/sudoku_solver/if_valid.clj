(ns sudoku-solver.if-valid)

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

(defn get-square [p x y]
  (let [square-x (* 3 (quot x 3))
        square-y (* 3 (quot y 3))
        row1 (subvec (nth p square-y) square-x (+ 3 square-x))
        row2 (subvec (nth p (+ 1 square-y)) square-x (+ 3 square-x))
        row3 (subvec (nth p (+ 2 square-y)) square-x (+ 3 square-x))]
    (concat row1 row2 row3)))

(defn cell-valid? [board row col value]
  (let [current-row (get board row)
        current-column (map #(nth % col) board)
        current-subgrid (get-square board col row)]

    (and (not= value 0)
         (not-any? #{value} current-row)
         (not-any? #{value} current-column)
         (not-any? #{value} (flatten current-subgrid))
        )))


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





