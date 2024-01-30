(ns sudoku-solver.controller.sudokuApi
  (:require [compojure.core :refer :all]
            [ring.adapter.jetty :as jetty]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [clj-http.client :as client]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
         ))

(def app-state (atom {:board nil :solved-board nil}))

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
            ]
        (reset! app-state {:board board :solved-board solved-board})
        (:board @app-state))
      (throw (Exception. (str "Failed to fetch Sudoku board. Status: " (:status response)))))))

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
           :body (json/generate-string (:solved-board @app-state))}
          )

        (GET "/board" []
          {:status 200
           :headers {"Content-Type" "application/json"}
           :body (json/generate-string (fetch-sudoku-board))}))
      (enable-cors)))


(defn -main []
  (log/info "Starting the server on port 8080")
  (jetty/run-jetty app-routes {:port 8080}))

(-main)