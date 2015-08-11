(ns clevolution.cliskeval)


(defn clisk-eval-form
  [form size]
  (binding [*ns* (the-ns 'clevolution.cliskenv)]
    (try
      (let [node (eval form)]
        (println "node is a" (class node))
        (clisk.core/image node :size size))
      (catch Exception e
        (.printStackTrace e)
        (clisk.core/image 0.0 :size size)))))


(defmulti clisk-eval (fn [x _] (class x)))

(defmethod clisk-eval String
  ([generator size]
    (println generator)
    (clisk-eval (read-string generator) size)))

(defmethod clisk-eval clojure.lang.PersistentList
  ([form size]
    (clisk-eval-form form size)))

(defmethod clisk-eval clojure.lang.Symbol
  ([form size]
    (clisk-eval-form form size)))

(defmethod clisk-eval clojure.lang.Cons
  ([form size]
    (clisk-eval-form (apply list form) size)))

