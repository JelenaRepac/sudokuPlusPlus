(ns sudoku-solver.algorithms.logic-library
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [clojure.string :as s]
            [clojure.core.logic :refer :all]
            [clojure.core.logic.fd :as fd]))


(def row-indexes (partition 9 (range 81)))
(def column-indexes (apply map vector row-indexes))
(def square-indexes [[0  1  2  9  10 11 18 19 20]
                     [3  4  5  12 13 14 21 22 23]
                     [6  7  8  15 16 17 24 25 26]
                     [27 28 29 36 37 38 45 46 47]
                     [30 31 32 39 40 41 48 49 50]
                     [33 34 35 42 43 44 51 52 53]
                     [54 55 56 63 64 65 72 73 74]
                     [57 58 59 66 67 68 75 76 77]
                     [60 61 62 69 70 71 78 79 80]])


(defn indexed-sub-board
  [b index-set]
  (letfn [(sub-board [n] (vals (select-keys b (nth index-set n))))]
    (doall (map sub-board (range 9)))))

(defn solve
  [sudoku]
  (println "Solving sudoku with algorithm LOGIC....")
  (let [board (vec (map #(if (zero? %) (lvar) %) sudoku))
        rows (indexed-sub-board board row-indexes)
        cols (indexed-sub-board board column-indexes)
        squares (indexed-sub-board board square-indexes)]

    (first
      (run 1 [q]
           (== q board)
           (everyg #(fd/in % (fd/interval 1 9)) board)
           (everyg fd/distinct rows)
           (everyg fd/distinct cols)
           (everyg fd/distinct squares)))))



(defn print-sudoku
  [sudoku]
  (doall (map #(println (s/join " " %)) (partition 9 sudoku))))

