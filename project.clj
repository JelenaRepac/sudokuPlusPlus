(defproject sudoku-solver "0.1.0-SNAPSHOT"
  :description "A Clojure Sudoku Solver"
  :url "https://github.com/your-username/sudoku-solver"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies
  [[org.clojure/clojure "1.12.0-alpha1"]
   [midje "1.10.9"]
   [org.clojure/core.logic "1.0.1"]
   [criterium "0.4.6"]
   [compojure "1.7.0"]
   [cheshire "5.12.0"]
   [ring/ring-core "1.11.0"]
   [ring-cors "0.1.13"]
   [ring/ring-jetty-adapter "1.6.3"]
   [ring/ring-defaults "0.4.0"]
   [clj-http "3.12.0"]
   [org.clojure/java.jdbc "0.7.12"]
   [mysql/mysql-connector-java "8.0.23"]
   [com.h2database/h2 "1.4.197"]])
