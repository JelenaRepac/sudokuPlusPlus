(ns sudoku-solver.algorithms.locked-candidate)

;When a candidate is possible in a certain block and row/column,
;and it is not possible anywhere else in the same row/column,
;then it is also not possible anywhere else in the same block.

;When a candidate is possible in a certain block and row/column,
;and it is not possible anywhere else in the same block,
;then it is also not possible anywhere else in the same row/column.

;Once all the singles have been found, I usually start marking.
;Then the job is to eliminate marks until only one remains in a cell,
;so then we know that cell's value. In this next technique, we use
;the fact that once a number can be assigned to a given row or column
;of a specific block (even if its exact location is still unknown),
;no other block may have that same number in the same row or column.
(defn get-row [board row]
  (nth board row))
(defn get-col [board col]
  (nth (apply map vector board) col))

(defn get-block [board row col]
  (let [block-row (quot row 3)
        block-col (quot col 3)]
    (for [i (range (* 3 block-row) (+ (* 3 block-row) 3))
          j (range (* 3 block-col) (+ (* 3 block-col) 3))]
      (nth (nth board i) j))))
(get-block sudoku-board 0 0)
(defn valid-num? [board row col num]
  (and (not (contains? (set (get-row board row)) num))
       (not (contains? (set (get-col board col)) num))
       (not (contains? (set (get-block board row col)) num))))
(contains? (set (get-row sudoku-board 0))1)
(contains? (get-row sudoku-board 0) 1)

(defn find-singles [board]
  (for [row (range 9)
        col (range 9)
        :when (= 0 (get-in board [row col]))
        num (range 1 10)
        :when (valid-num? board row col num)]
    [row col num]))

(find-singles sudoku-board)

(defn eliminate-possibilities [board singles]
  (loop [board board
         singles singles]
    (if (empty? singles)
      board
      (let [single (first singles)
            [row col num] single
            updated-board (assoc-in board [row col] num)
            updated-singles (filter #(not= % single) singles)]
        (println single)
        (recur updated-board
               (concat updated-singles
                       (for [r (range 9)
                             c (range 9)
                             :when (= 0 (get-in updated-board [r c]))
                             :when (or (= r row) (= c col) (= (quot r 3) (quot row 3) (quot c 3)))
                             num (range 1 10)
                             :when (valid-num? updated-board r c num)]
                         [r c num])))))))

  (defn solve-sudoku [board]
    (let [singles (find-singles board)]
      (if (empty? singles)
        board
        (recur (eliminate-possibilities board singles)))))

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

(solve-sudoku sudoku-board)


