(ns clevolution.cliskeval
  (:require [clevolution.cliskenv]))


(defn clisk-eval-form
  [form]
  (binding [*ns* (the-ns 'clevolution.cliskenv)]
    (try
      (let [node (eval form)]
        (println "node:" node)
        (println "node is a" (class node))
        node)
      (catch Exception e
        (println "clisk-eval-form: ERROR, returning 0.0")
        #_(.printStackTrace e)
        0.0))))


(defmulti clisk-eval class)

(defmethod clisk-eval String
  ([generator]
    (println generator)
    (clisk-eval (read-string generator))))

(defmethod clisk-eval clojure.lang.PersistentList
  ([form]
    (clisk-eval-form form)))

(defmethod clisk-eval clojure.lang.Symbol
  ([form]
    (clisk-eval-form form)))

(defmethod clisk-eval clojure.lang.Cons
  ([form]
    (clisk-eval-form (apply list form))))

