(ns sudoku-solver.controller.sudokuApi
  (:require [compojure.core :refer :all]
            [ring.adapter.jetty :as jetty]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [clj-http.client :as client]
            [clojure.java.jdbc :as jdbc]
            [sudoku-solver.if-valid]
            [sudoku-solver.algorithms.logic-library]
            [sudoku-solver.core])
  )
;;H2 db
(def db-spec
  {:classname "org.h2.Driver"
   :subprotocol "h2:mem"
   :subname "test-db;DB_CLOSE_DELAY=-1"
   :user "sa"
   :password ""
   })


(jdbc/get-connection db-spec)
(jdbc/query db-spec ["SHOW TABLES"])
(try
  (jdbc/execute! db-spec
                 ["CREATE TABLE sudoku_boards
                 (id SERIAL PRIMARY KEY,
                 board VARCHAR(200),
                 solved_board VARCHAR(200),
                 difficulty VARCHAR(50))"])
  (catch Exception e
    (println "Error creating sudoku_boards table:" (.getMessage e))))
(try
  (jdbc/execute! db-spec
                 ["CREATE TABLE results
                 (board VARCHAR(200),
                 time DECIMAL(10),
                 user VARCHAR(100),
                 initial VARCHAR(200))"])
  (catch Exception e
    (println "Error creating results table:" (.getMessage e))))


;(def db-spec
;  {:classname   "com.mysql.cj.jdbc.Driver"
;   :subprotocol "mysql"
;   :subname     "//localhost:3306/sudoku"
;   :user        "root"
;   :password    "root"})

(defn serialize [x]
  (json/generate-string x))
(defn insert-sudoku-board [board solved-board difficulty]
  (jdbc/with-db-connection [conn db-spec]
                           (let [board-data (serialize board)
                                 solved-board-data (serialize solved-board)
                                 difficulty-data (serialize difficulty)]
                             (jdbc/insert!
                               conn
                               :sudoku_boards
                               {:board board-data
                                :solved_board solved-board-data
                                :difficulty difficulty-data})))
  )

(defn insert-time [initialBoard board time user]
    (jdbc/with-db-connection [conn db-spec]
                             (let [initial-board-data (serialize initialBoard)
                                   board-data (serialize board)
                                   time-data (serialize time)
                                   user-data (serialize user)]
                               (jdbc/insert!
                                 conn
                                 :results
                                 { :initial initial-board-data
                                  :board board-data
                                  :time time-data
                                  :user user-data})))
  (log/info "User " user " solved sudoku in " time " milliseconds.")
    )

(def app-state (atom {:board nil :solved-board nil :difficulty nil}))

(defn deserialize-board [board-data]
  (-> board-data
      (json/parse-string true)
      ))

(defn fetch-best-result []
  (jdbc/with-db-connection [conn db-spec]
                           (let [result (jdbc/query conn
                                                  ["SELECT user,time FROM results WHERE time = (SELECT MIN(time) FROM results) LIMIT 1"]
                                                  )
                                 first (first result)]
                             { :user (deserialize-board (:user first))
                              :time  (:time first)
                              }
                           ))
  )


(defn fetch-leaderboard []
  (jdbc/with-db-connection [conn db-spec]
                           (let [result (jdbc/query conn ["SELECT user, time FROM results ORDER BY time ASC LIMIT 3"])
                                 result-count (count result)
                                 first-row (if (>= result-count 1) (nth result 0) nil)
                                 second-row (if (>= result-count 2) (nth result 1) nil)
                                 third-row (if (>= result-count 3) (nth result 2) nil)]
                             (println result)
                             {:first (if first-row
                                       {:user (deserialize-board (:user first-row))
                                        :time (-> first-row :time)})
                              :second (if second-row
                                        {:user (deserialize-board (:user second-row))
                                         :time (-> second-row :time)})
                              :third (if third-row
                                       {:user (deserialize-board (:user third-row))
                                        :time (-> third-row :time)})})))

(defn fetch-sudoku-board-hard []
  (jdbc/with-db-connection [conn db-spec]
                           (let [result (jdbc/query conn
                                                    ["SELECT * FROM sudoku_boards WHERE difficulty = '\"Hard\"'"])
                                 first-row (rand-nth result)]
                             (reset! app-state {:board (deserialize-board (:board first-row)) :solved-board (deserialize-board (:solved_board first-row)) :difficulty (deserialize-board (:difficulty first-row))})
                             {:board (deserialize-board (:board first-row))
                              :solved-board (deserialize-board (:solved_board first-row))
                              :difficulty (deserialize-board (:difficulty first-row))})))
(defn fetch-sudoku-board-easy  []
  (jdbc/with-db-connection [conn db-spec]
                           (let [result (jdbc/query conn
                                                    ["SELECT * FROM sudoku_boards WHERE difficulty = '\"Easy\"'"])
                                 first-row (rand-nth result)]
                             (println first-row)
                             (reset! app-state {:board (deserialize-board (:board first-row)) :solved-board (deserialize-board (:solved_board first-row)) :difficulty (deserialize-board (:difficulty first-row))})
                             {:board (deserialize-board (:board first-row))
                              :solved-board (deserialize-board (:solved_board first-row))
                              :difficulty (deserialize-board (:difficulty first-row))})))

(defn fetch-sudoku-board-medium  []
  (jdbc/with-db-connection [conn db-spec]
                           (let [result (jdbc/query conn
                                                    ["SELECT * FROM sudoku_boards WHERE difficulty = '\"Medium\"'"])
                                 first-row (rand-nth result)]
                             (println first-row)
                             (reset! app-state {:board (deserialize-board (:board first-row)) :solved-board (deserialize-board (:solved_board first-row)) :difficulty (deserialize-board (:difficulty first-row))})
                             {:board (deserialize-board (:board first-row))
                              :solved-board (deserialize-board (:solved_board first-row))
                              :difficulty (deserialize-board (:difficulty first-row))})))
(defn fetch-sudoku-board []
  (let [url "https://sudoku-api.vercel.app/api/dosuku"
        response (client/get url {:headers {"Content-Type" "application/json"}})]
    (if (= 200 (:status response))
      (let [board (-> (:body response)
                      (json/parse-string true)
                      (get-in [:newboard :grids 0 :value]))
            solved-board (-> (:body response)
                             (json/parse-string true)
                             (get-in [:newboard :grids 0 :solution]))
            difficulty (-> (:body response)
                           (json/parse-string true)
                           (get-in [:newboard :grids 0 :difficulty]))
            ]
        (reset! app-state {:board board :solved-board solved-board :difficulty difficulty})
        (:board @app-state))
      (throw (Exception. (str "Failed to fetch Sudoku board. Status: " (:status response)))))))

(defn fetch-and-save-sudoku-boards [n]
  (loop [count 0]
    (if (< count n)
      (let [url "https://sudoku-api.vercel.app/api/dosuku"
            response (client/get url {:headers {"Content-Type" "application/json"}})]
        (if (= 200 (:status response))
          (let [board (-> (:body response)
                          (json/parse-string true)
                          (get-in [:newboard :grids 0 :value]))
                solved-board (-> (:body response)
                                 (json/parse-string true)
                                 (get-in [:newboard :grids 0 :solution]))
                difficulty (-> (:body response)
                               (json/parse-string true)
                               (get-in [:newboard :grids 0 :difficulty]))
                ]
            (insert-sudoku-board board solved-board difficulty)
            (recur (inc count)))
          (throw (Exception. (str "Failed to fetch Sudoku board. Status: " (:status response))))))
      (println "Fetched and saved" n "Sudoku boards."))))


(def cors-headers
  {"Access-Control-Allow-Origin"  "http://localhost:3000"
   "Access-Control-Allow-Headers" "Content-Type"
   "Access-Control-Allow-Methods" "GET, POST, OPTIONS"
   "Access-Control-Allow-Credentials" "true"})

(defn check-cell-value [request]
  (if (empty? request)
    {:status 400
     :headers {"Content-Type" "application/json"}
     :body "error:Missing or invalid request"}
    (let [params (:params request)
          board (get params "board")
          row-index (get params "rowIndex" 0)
          col-index (get params "columnIndex" 0)
          value (get params "value" 0)
          result (sudoku-solver.if-valid/cell-valid? board row-index col-index value)]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/generate-string {:board board
                                    :rowIndex row-index
                                    :columnIndex col-index
                                    :value value
                                    :result result})})))


(defn enable-cors [handler]
  (fn [request]
    (let [response (handler request)]
      (merge response
             {:headers (merge (:headers response) cors-headers)}))))

(def app-routes
  (-> (routes
        (GET "/solved" []
          {:status 200
           :headers {"Content-Type" "application/json"}
           :body (json/generate-string (:solved-board @app-state))}
          )
        (GET "/board" []
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {
                                          :board (sudoku-solver.core/generate-sudoku-board)
                                          })
             })
        (GET "/board-hard" []
          (let [sudoku (fetch-sudoku-board-hard)]
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {
                                          :board  (:board sudoku)
                                          :difficulty (:difficulty sudoku)
                                          :solved-board (:solved-board sudoku)
                                          })
             }
            ))
        (GET "/board-medium" []
          (let [sudoku (fetch-sudoku-board-medium)]
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {
                                          :board  (:board sudoku)
                                          :difficulty (:difficulty sudoku)
                                          :solved-board (:solved-board sudoku)
                                          })
             }
            ))
        (GET "/board-easy" []
          (let [sudoku (fetch-sudoku-board-easy)]
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {
                                          :board  (:board sudoku)
                                          :difficulty (:difficulty sudoku)
                                          :solved-board (:solved-board sudoku)
                                          })
             }
            ))
        (POST "/check-cell-value" request
          (let [body (slurp (:body request))
                params (json/parse-string body true)
                board (:board params)
                row-index (:rowIndex params)
                col-index (:columnIndex params)
                value (:value params)
                result (sudoku-solver.if-valid/cell-valid? board row-index col-index value)]
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {:board board
                                          :rowIndex row-index
                                          :columnIndex col-index
                                          :value value
                                          :result result})}))
        (POST "/if-valid" request
          (let [body (slurp (:body request))
                params (json/parse-string body true)
                board (:board params)
                result (sudoku-solver.if-valid/sudoku-solved? board )]
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {:result result})}))

        (POST "/cell-hints" request
          (let [body (slurp (:body request))
                params (json/parse-string body true)
                board (:board params)
                row-index (:rowIndex params)
                col-index (:columnIndex params)
                result (sudoku-solver.core/valid-numbers board row-index col-index )]
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {:result result})}))

        ;; checking the number count
        (POST "/number-filled" request
          (let [body (slurp (:body request))
                params (json/parse-string body true)
                board (:board params)
                n (:n params)
                result (sudoku-solver.core/number-filled? board n )]
            (println n result board)
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {:result (sudoku-solver.core/number-filled? board n )})}))

        ; my algorithm
        (POST "/solve-sudoku" request
          (let [body (slurp (:body request))
                params (json/parse-string body true)
                board (:board params)
                filled-cells (sudoku-solver.core/count-filled-cells board)
                ]
            ;; if it is hard get the solved sudoku
            (cond
              (< filled-cells 25)
              (do
                (println "hard")
                {:status 200
                 :headers {"Content-Type" "application/json"}
                 :body (json/generate-string (:solved-board @app-state))})
              ;; if it is medium, get the algorithm result
              (< filled-cells 35)
              (do
                (println "medium")
                {:status 200
                 :headers {"Content-Type" "application/json"}
                 :body (json/generate-string (sudoku-solver.algorithms.logic-library/sudokufd (flatten board)))})
              ;; get my algorithm result
              :else
              (do
                (println "easy")
                {:status 200
                 :headers {"Content-Type" "application/json"}
                 :body (json/generate-string  (sudoku-solver.core/solve board))}))))

        (POST "/difficulty" request
          (let [body (slurp (:body request))
                params (json/parse-string body true)
                board (:board params)
                result (sudoku-solver.core/sudoku-difficulty board)
                ]
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string result)}
            ))
        (POST "/time" request
          (let [body (slurp (:body request))
                params (json/parse-string body true)
                initialBoard (:initialBoard params)
                board (:board params)
                time (:time params)
                user (:user params)
                ]
            (insert-time initialBoard board time user)
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string time)}
            ))
        (GET "/best-result" []
          (let [result (fetch-best-result)]
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {
                                          :user  (:user result)
                                          :time (:time result)
                                          })
             }
            ))
        (GET "/leaderboard" []
          (let [result (fetch-leaderboard)]
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {
                                          :first (:first result)
                                          :second (:second result)
                                          :third (:third result)
                                          })
             }
            ))
        (POST "/performance" request
          (let [body (slurp (:body request))
                params (json/parse-string body true)
                board (:board params)
                filled-cells (sudoku-solver.core/count-filled-cells board)
                resolved-board (if (>= filled-cells 40) (sudoku-solver.core/solve board)
                                                        nil)
                result (sudoku-solver.core/performance board)
                ]
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {:performance result
                                          :resolvedBoard resolved-board})}
            ))
        )
      (enable-cors)))

(defn -main []
  ;(fetch-and-save-sudoku-boards 40)
  (log/info "Starting the server on port 8080")
  (jetty/run-jetty app-routes {:port 8080}))

(-main)
(jdbc/query db-spec ["SELECT * FROM sudoku_boards"])
;(jdbc/execute! db-spec ["DELETE FROM results"])
