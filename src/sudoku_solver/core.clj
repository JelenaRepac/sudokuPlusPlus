(ns sudoku-solver.core
  (:require [clojure.string :as str]
            [sudoku-solver.algorithms.logic-library :as ll]
            [sudoku-solver.if-valid :as if-valid]
            [clojure.core.logic :refer :all]
            [sudoku-solver.algorithms.locked-candidate :as lc]
            [criterium.core :as criterium]
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

(defn count-valid-numbers-for-zeroes [board]
  (for [row (range 9)
        col (range 9)
        :when (= (get-in board [row col]) 0)]
    [row col (count (valid-numbers board row col))]))


;;pocinjemo od onih koji imaju samo jednu opciju
(defn count-valid-numbers [board [row col]]
  (let [possible-numbers (valid-numbers board row col)]
    (count possible-numbers)))

;;my algorithm
(defn solve-helper [indexes i new-board]
  (if (= i (count indexes))
    new-board
    (let [sorted-indexes (sort-by #(count-valid-numbers new-board %) indexes)
          current-index (nth sorted-indexes i)
          [row col] current-index
          possible-numbers (valid-numbers new-board row col)]
      ;;(println "row :" row "col :" col "Possible numbers:" possible-numbers)
      (if (seq possible-numbers)
        (if (= 1 (count possible-numbers))
          (recur indexes (inc i) (assoc-in new-board [row col] (first possible-numbers)))
          (recur indexes (inc i) new-board))
        (recur indexes (inc i) new-board)))))
(defn solve [board]
  (let [indexes (find-zero-indexes board)]
    (loop [i 0
           new-board board]
      (solve-helper indexes i new-board))
    ))

(def board
  [[5 3 0 0 7 0 0 0 0]
   [6 0 0 1 9 5 0 0 0]
   [0 9 8 0 0 0 0 6 0]
   [0 0 0 0 6 0 0 0 3]
   [4 0 0 8 0 3 0 0 1]
   [7 0 0 0 2 0 0 0 6]
   [0 6 0 0 0 0 2 8 0]
   [0 0 0 4 1 9 0 0 5]
   [0 0 0 0 8 0 0 7 9]])
(def example-board
  [[0 0 0 2 6 0 7 0 1]
   [6 8 0 0 7 0 0 9 0]
   [1 9 0 0 0 4 5 0 0]
   [8 2 0 1 0 0 0 4 0]
   [0 0 4 6 0 2 9 0 0]
   [0 5 0 0 0 3 0 2 8]
   [0 0 9 3 0 0 0 7 4]
   [0 4 0 0 5 0 0 3 6]
   [7 0 3 0 1 8 0 0 0]])

(lc/solve-sudoku example-board)
(defn count-filled-cells [board]
  (count (filter #(not= 0 %) (flatten board))))

(defn unique-solutions? [board]
  ;; You would implement a function to check if the puzzle has a unique solution
  (rand-nth [true false]))

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
    distribution))

(defn sudoku-difficulty [board]
  (let [filled-cells (count-filled-cells board)
        ;;unique-solution (unique-solutions? board)
        distribution (filled-cells-distribution board)]
    (println distribution)
    (cond
       (<= filled-cells 20) "Very Hard"
      (<= filled-cells 35)  "Hard")
     (<= filled-cells 40)  "Medium")
      :else "Easy")
(sudoku-difficulty example-board-1)
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


;;Ukoliko moj algoritam ne resi, pozivamo logic
(defn check-sudoku [board]
  (if  (if-valid/sudoku-solved? board)
    (println "It is solved")
      (ll/print-sudoku (ll/solve (flatten board)))
    )
  )

(defn -main []
  (println "\n===================================")
  (println "SUDOKU ++")
  (loop []
    (println "\n===================================")
    (println "1. Solve your sudoku\n2. Solve our sudoku \n3. Find out difficulty level \n4. Exit")
    (println "===================================")
    (print "Select an option: ")
    (flush)
    (let [choice (read-line)]
      (cond
        (= choice "1")
        (do (println "Insert 1. row: ")
            (flush)
            (let [user-board (read-sudoku)]
              (print user-board)
              (check-sudoku (solve user-board))
              )
            (recur))
        (= choice "2")
        (do (println "\n===================================")
            (println "1. SUDOKU ++ ALGORITHM\n2. ALGORITHM LOGIC LIBRARY \n")
            (println "===================================")
            (print "Select an algorithm: ")
            (flush)
            (let [alg (read-line)]
              (cond
                (= alg "1")
                (do (println "Solve our sudoku: ")
                    (flush)
                    (print-sudoku board)
                    (println "Solving our Sudoku board...")
                    (println "SUDOKU ++ ALGORITHM")
                    (print-sudoku (solve board))
                    (time (solve board))
                    (recur))
                (= alg "2")
                (do (println "Solve our sudoku: ")
                    (flush)
                    (print-sudoku board)
                    (println "Solving our Sudoku board...")
                    (println "ALGORITHM LOGIC LIBRARY")
                    (time (ll/print-sudoku (ll/solve (into [] (flatten board)))))
                    (recur))
                :else
                (recur))))
        (= choice "3")
        (do
          (println "Find out difficulty level:")
          (flush)
          (let [user-board board
                difficulty (sudoku-difficulty user-board)]
            (println " SUDOKU")
            (print-sudoku user-board)
            (println "#####################################")
            (println (str "Difficulty Level: " difficulty)))
          (println "#####################################")
          (recur))
        (= choice "4")
        (do (println "Goodbye!"))
        :else
        (do (println "Invalid choice. Try again.")
            (recur))))))



