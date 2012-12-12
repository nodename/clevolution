(ns clevolution.util)

;; http://blog.jayway.com/2011/03/13/dbg-a-cool-little-clojure-macro/
(defmacro dbg [& body]
  `(let [x# ~@body]
     (println (str "dbg: " (quote ~@body) "=" x#))
     x#))