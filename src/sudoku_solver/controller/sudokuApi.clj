(ns sudoku-solver.controller.sudokuApi
  (:require [compojure.core :refer :all]
            [ring.adapter.jetty :as jetty]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [clj-http.client :as client]
            [clojure.java.jdbc :as jdbc]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
         ))
(def db-spec
  {:classname   "com.mysql.cj.jdbc.Driver"
   :subprotocol "mysql"
   :subname     "//localhost:3306/sudoku"
   :user        "root"
   :password    "root"})
(defn serialize [x]
  (json/generate-string x))
(defn insert-sudoku-board [board solved-board difficulty]
  (jdbc/with-db-connection [conn db-spec]
                           (let [board-data (serialize board)
                                 solved-board-data (serialize solved-board)
                                 difficulty-data (serialize difficulty)]
                           (jdbc/insert!
                             conn
                             :sudoku_boards ; Change this to your actual table name
                             {:board board-data
                              :solved_board solved-board-data
                              :difficulty difficulty-data}))))
(def app-state (atom {:board nil :solved-board nil :difficulty nil}))

(defn deserialize-board [board-data]
  (-> board-data
      (json/parse-string true)))
(defn fetch-sudoku-board-hard []
  (jdbc/with-db-connection [conn db-spec]
                           (let [result (jdbc/query conn
                                                    ["SELECT * FROM sudoku_boards WHERE difficulty = '\"Hard\"'"])
                                 first-row (rand-nth result)]
                             (reset! app-state {:board (deserialize-board (:board first-row)) :solved-board (deserialize-board (:solved_board first-row)) :difficulty (deserialize-board (:difficulty first-row))})
                           (println (:solved-board @app-state))
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

(defn enable-cors [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc response
        :headers (assoc (:headers response) "Access-Control-Allow-Origin" "*")))))



(def app-routes
  (-> (routes
        (GET "/solved" []
          {:status 200
           :headers {"Content-Type" "application/json"}
           :body (json/generate-string (:solved-board @app-state)
                                        )}
          )
        (GET "/board" []
          {:status 200
           :headers {"Content-Type" "application/json"}
           :body (json/generate-string {
                  :board  (fetch-sudoku-board)
                  :difficulty (:difficulty @app-state)
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
        )
      (enable-cors)))


(defn -main []
  (log/info "Starting the server on port 8080")
  (jetty/run-jetty app-routes {:port 8080}))

(-main)