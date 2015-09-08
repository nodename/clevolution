(ns clevolution.cliskeval
  (:require [clevolution.cliskenv]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defn clisk-eval-form
  [form]
  (binding [*ns* (the-ns 'clevolution.cliskenv)]
    (eval form)))



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

