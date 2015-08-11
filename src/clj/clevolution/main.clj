(ns clevolution.main
  (:gen-class)
  [:require [clevolution.core :refer :all]])

(defn -main
  [uri]
  (let [main-thread (Thread/currentThread)]
    (println main-thread)
    (show-clisk-file uri)))