(ns sudoku-solver.core
  (:require [clojure.string :as str]
            [sudoku-solver.algorithms.logic-library :as ll]
            [sudoku-solver.if-valid :as if-valid]
            [clojure.core.logic :refer :all]

            )
  (:refer-clojure :exclude [==]))
(def board
  [[5 3 0 0 7 0 0 0 0]
   [6 0 0 1 9 5 0 0 0]
   [0 9 8 0 0 0 0 6 0]
   [8 0 0 0 6 0 0 0 3]
   [4 0 0 8 0 3 0 0 1]
   [7 0 0 0 2 0 0 0 6]
   [0 6 0 0 0 0 2 8 0]
   [0 0 0 4 1 9 0 0 5]
   [0 0 0 0 8 0 0 7 9]])

(defn remove-spaces [s]
  (str/replace s #"\s" ""))

(defn read-sudoku []
  (loop [i 0
         board (vector)]
    (if (< i 9)
      (let [row (read-line)]
        (if (< (count row) 8)
          (do
            (println "Every row must have 9 numbers!")
            (println "Insert " i ". row:")
            (recur i board))
          (do
            (println "Insert " (inc i) ". row:")
            (recur (inc i) (conj board (vec (map #(Integer/parseInt (str %)) (remove-spaces row)))))
            )
          )
        )
      board))
  )

(defn find-zero-indexes [board]
  (for [row (range 9)
        col (range 9)
        :when (= 0 (get-in board [row col]))]
    [row col]))

(defn get-square [p x y]
  (let [square-x (* 3 (quot x 3))
        square-y (* 3 (quot y 3))
        row1 (subvec (nth p square-y) square-x (+ 3 square-x))
        row2 (subvec (nth p (+ 1 square-y)) square-x (+ 3 square-x))
        row3 (subvec (nth p (+ 2 square-y)) square-x (+ 3 square-x))]
    (concat row1 row2 row3)))

(defn divisible-by-three? [number]
  (zero? (mod number 3)))

(defn print-sudoku [board]
  (doseq [[i row] (map vector (range) board)]
    (if (divisible-by-three? i)
      (println " _________________________________"))
    (print "| ")
    (doseq [[j col] (map vector (range) row)]
      (print col " ")
      (when (zero? (mod (inc (.indexOf (str col) "\n")) 3))
        (print ""))
      (when (= (mod (inc j) 3) 0)
        (print "| ")))
    (println))
  (println " _________________________________"))


(defn valid-numbers [board row col]
  (let [row-values (get board row)
        col-values (map #(nth % col) board)
        square-values (get-square board col row)]
    (->> (range 1 10)
         (filter (fn [num]
                   (not-any? #{num} (concat row-values col-values square-values)))))))


;;pocinjemo od onih koji imaju samo jednu opciju
(defn count-valid-numbers [board [row col]]
  (let [possible-numbers (valid-numbers board row col)]
    (count possible-numbers)))

;;my algorithm
(defn solve-helper [indexes i new-board]
  (if (= i (count indexes))
    [new-board]
    (let [sorted-indexes (sort-by #(count-valid-numbers new-board %) indexes)
          current-index (nth sorted-indexes i)
          [row col] current-index
          possible-numbers (valid-numbers new-board row col)]
      (if (seq possible-numbers)
        (if (= 1 (count possible-numbers))
          (recur indexes (inc i) (assoc-in new-board [row col] (first possible-numbers)))
          (let [random-solution (rand-nth possible-numbers)]
            (recur indexes (inc i) (assoc-in new-board [row col] random-solution))))
        (recur indexes (inc i) new-board)))))

(defn solve [board]
  (loop [retry 2
         retry-board board]
    (if (pos? retry)
      (let [indexes (find-zero-indexes retry-board)
            solutions (loop [i 0
                             new-board retry-board]
                        (solve-helper indexes i new-board))]
        (if (seq solutions)
          solutions
          (recur (dec retry) board)))
      nil)))


(defn count-filled-cells [board]
  (count (filter #(not= 0 %) (flatten board))))

(defn count-filled-cells-in-block [board row-start col-start]
  (reduce + (for [i (range 3)
                  j (range 3)]
              (if (pos? (get-in board [(+ row-start i) (+ col-start j)]))
                1
                0))))

(defn filled-cells-distribution [board]
  (let [block-counts (for [row-start (range 0 9 3)
                           col-start (range 0 9 3)]
                       (count-filled-cells-in-block board row-start col-start))]
    (if (apply = block-counts)
      "Even distribution"
      "Clustered distribution")))

(defn sudoku-difficulty [board]
  (let [filled-cells (count-filled-cells board)
        distribution (filled-cells-distribution board)]
    (println distribution)
    (cond
      (and (<= filled-cells 20) (= distribution "Even distribution")) "Very Hard"
      (and (<= filled-cells 35) (= distribution "Even distribution")) "Hard"
      (and (<= filled-cells 30) (= distribution "Clustered distribution")) "Hard"
      (and (<= filled-cells 40) (= distribution "Clustered distribution")) "Medium"
      (<= filled-cells 45) "Easy"
      :else "Very Easy")))

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

(defn generate-sudoku-board []
  (let [empty-board (vec (repeat 9 (vec (repeat 9 0))))]
    (loop [board empty-board
           inserted-numbers 0]
      (let [unfilled-cells (filter #(= 0 (get-in board %)) (for [i (range 81)] [(quot i 9) (mod i 9)]))
            shuffled-cells (shuffle unfilled-cells)]
        (if (>= inserted-numbers 28)
         (if (empty? (sudoku-solver.algorithms.logic-library/sudokufd (flatten board) ))
           (recur empty-board 0)
           board
           )
          (let [n (first shuffled-cells)
                row (first n)
                col (second n)
                possible-numbers (valid-numbers board row col)
                rand-number (if (seq possible-numbers)
                              (rand-nth possible-numbers)
                              nil)]
            (if (> (count possible-numbers) 3)
              (if (nil? rand-number)
                (recur board inserted-numbers)
                (if (sudoku-solver.if-valid/cell-valid? board row col rand-number)
                  (recur (assoc-in board [row col] rand-number) (inc inserted-numbers))
                  (recur board inserted-numbers)))
              (recur empty-board 0))))))))
;(generate-sudoku-board)
;(sudoku-solver.algorithms.logic-library/sudokufd (flatten (generate-sudoku-board)))
(defn performance [board]
  (let [start-time (System/currentTimeMillis)
        result (solve board)
        end-time (System/currentTimeMillis)]
     (- end-time start-time) ))
(defn count-number [board n]
  (loop [row 0
         total-count 0]
    (if (< row (count board))
      (let [row-data (nth board row)]
        (recur (inc row) (+ total-count (count (filter #(= n %) row-data)))))
      total-count)))

(defn number-filled? [board n]
  (= 9 (count-number board n)))




