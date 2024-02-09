(ns sudoku-solver.algorithms.logic-library
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [clojure.string :as s]
            [clojure.core.logic :refer :all]
            [clojure.core.logic.fd :as fd]))
(defn get-square [rows x y]
  (for [x (range x (+ x 3))
        y (range y (+ y 3))]
    (get-in rows [x y])))
(defn bind [var hint]
  (if-not (zero? hint)
    (== var hint)
    succeed))
(defn bind-all [vars hints]
  (and* (map bind vars hints)))
(defn sudokufd [hints]
  (let [vars (repeatedly 81 lvar)
        rows (->> vars (partition 9) (map vec) (into []))
        cols (apply map vector rows)
        sqs  (for [x (range 0 9 3)
                   y (range 0 9 3)]
               (get-square rows x y))]
    (println vars)
    (run 1 [q]
         (== q vars)
         (everyg #(fd/in % (fd/domain 1 2 3 4 5 6 7 8 9)) vars)
         (bind-all vars hints)
         (everyg fd/distinct rows)
         (everyg fd/distinct cols)
         (everyg fd/distinct sqs))))






