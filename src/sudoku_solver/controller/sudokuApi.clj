(ns sudoku-solver.controller.sudokuApi
  (:require [compojure.core :refer :all]
            [ring.adapter.jetty :as jetty]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
         ))

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

(defn enable-cors [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc response
        :headers (assoc (:headers response) "Access-Control-Allow-Origin" "*")))))

(def app-routes
  (-> (GET "/board" []
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string example-board)})
      (enable-cors)))

(defn -main []
  (log/info "Starting the server on port 8080")
  (jetty/run-jetty app-routes {:port 8080}))

(-main)