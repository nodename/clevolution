(ns clevolution.cliskeval
  (:require [clevolution.cliskenv]))


(defn clisk-eval-form
  [form]
  (try
    (binding [*ns* (the-ns 'clevolution.cliskenv)]
      (eval form))
    (catch Exception e
      (println "clisk-eval-form failed:")
      (println "form=" form)
      (throw e))))



(defmulti clisk-eval class)

(defmethod clisk-eval String
  ([generator]
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

