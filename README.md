Sudoku++ is an application that provides the user with an interactive and enjoyable Sudoku solving experience. In addition to offering various levels of Sudoku puzzles, the application generates a completely random puzzle for each game.

## Implementation
The application has implemented an H2 embedded database, which is distributed as a library. Data is stored in .mv.db files, while the SQL queries themselves are executed in Clojure. 
This type of database allows for the development and testing of applications with an integrated database without the need for an external server.
For the database implementation itself, it was necessary to include the following dependency:
**[com.h2database/h2 "1.4.200"]**

While the database specification itself is defined as follows.
```
(def db-spec
  {:classname "org.h2.Driver"
   :subprotocol "h2:mem"
   :subname "test-db;DB_CLOSE_DELAY=-1"
   :user "sa"
   :password ""
   })
```
DB_CLOSE_DELAY is set to -1 to prevent the connection to the database from automatically closing, keeping the database active in memory during the application's runtime.
Initially, two tables were created: **SUDOKU_BOARDS** and **RESULTS**.
```
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
```

SUDOKU_BOARDS stores Sudoku tables inserted by calling the public API https://sudoku-api.vercel.app/api/dosuku. Meanwhile, the results table stores the result (the time it took to solve the Sudoku) as well as specific users who have played Sudoku.

**[ring/ring-jetty-adapter "1.6.3"]** is a library that enables running a web server using the Jetty server. Ring provides a standard interface for communication between Clojure applications and HTTP servers. The server is started with the help of jetty/run-jetty.
```
(defn -main []
  (fetch-and-save-sudoku-boards 40)
  (log/info "Starting the server on port 8080")
  (jetty/run-jetty app-routes {:port 8080}))
```
By passing through the app-routes defined using the library **[compojure "1.7.1"]**, we enable routing within our application.
```
(def cors-headers
  {"Access-Control-Allow-Origin"  "http://localhost:3000"
   "Access-Control-Allow-Headers" "Content-Type"
   "Access-Control-Allow-Methods" "GET, POST, OPTIONS"
   "Access-Control-Allow-Credentials" "true"})
```

By running **sudokiApi.clj,** we start the entire application and expose endpoints that allow interaction with the application.

## Algorithms

The solution of Sudoku puzzles is implemented as follows:

- EASY BOARDS are solved using the algorithm provided below.
```
  (defn solve-helper [indexes i new-board]
  (if (= i (count indexes))
    new-board
    (let [sorted-indexes (sort-by #(count-valid-numbers new-board %) indexes)
          current-index (nth sorted-indexes i)
          [row col] current-index
          possible-numbers (valid-numbers new-board row col)]
      (if (seq possible-numbers)
        (if (= 1 (count possible-numbers))
          (recur indexes (inc i) (assoc-in new-board [row col] (first possible-numbers)))
          (let [random-solution (rand-nth possible-numbers)]
            (solve-helper indexes (inc i) (assoc-in new-board [row col] random-solution))))
        nil))))
```
```
(defn solve [board]
  (let [indexes (find-zero-indexes board)]
    (loop [i 0
           new-board board]
      (if-let [solved-board (solve-helper indexes i new-board)]
        solved-board
        (recur i (assoc new-board :error "Could not solve"))))))
```

The algorithm is based on finding positions with only one solution. If there are multiple possible numbers for a cell, we randomly choose one number and recursively continue searching for a solution.

* MEIDUM BOARD are solved by algorithm that is taken from https://github.com/sideshowcoder/core-logic-sudoku-solver/tree/master.
+ HARD BOARD are solved sudoku boards from public API https://sudoku-api.vercel.app/api/dosuku.

The described process is shown below.
```
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
```

In the case of solving, we divided the difficulty into three levels based on the number of filled cells (easy, medium, hard).

For generating new tables, we used a function that determines the difficulty of the table based on the number and distribution of filled cells. This way, we offer the user a more precise determination of the difficulty of randomly generated tables.
```
(defn filled-cells-distribution [board]
  (let [block-counts (for [row-start (range 0 9 3)
                           col-start (range 0 9 3)]
                       (count-filled-cells-in-block board row-start col-start))]
    (if (apply = block-counts)
      "Even distribution"
      "Clustered distribution")))
```
```
(defn sudoku-difficulty [board]
  (let [filled-cells (count-filled-cells board)
        distribution (filled-cells-distribution board)]
    (println distribution)
    (cond
      (<= filled-cells 20) "Very Hard"
      (and (<= filled-cells 35) (= distribution "Even distribution")) "Hard"
      (and (<= filled-cells 30) (= distribution "Clustered distribution")) "Hard"
      (and (<= filled-cells 40) (= distribution "Clustered distribution")) "Medium"
      (<= filled-cells 45) "Easy"
      :else "Very Easy")))
```

We can obtain a new table in the following ways:
- Downloading tables from a public API.
- Invoking the function provided below to generate a Sudoku table.

```
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
```

Initially, an empty 9x9 table is created with all elements set to 0. The algorithm tracks the number of cells that are not filled, and based on them, generates a random arrangement of empty cells. To ensure that the generated table is solvable, the algorithm calls the retrieved algorithm to check if the Sudoku can be solved. This means that if the table is not valid, has no solution, we return to step one, and the algorithm begins generating a new table. To fill the table, the first cell is taken from the list, and if there are more than 3 possible numbers for the cell, one is randomly chosen, otherwise, the algorithm returns to the beginning. Another mandatory step is to call the cell-valid? function, which checks if the selected number is valid for that specific cell, taking into account the entire table. The entire process repeats until there are 28 filled cells.


## Routes

Some of the available routes in the application are:

- **POST /time**
  
Inserts the time taken by the user to solve the Sudoku.
```
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
```
- **GET /leaderboard**
  
Retrieves the top three best results.
```
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
```

- **POST /performance**
  
Obtains the time required to solve the table using the algorithm.
```
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
```
