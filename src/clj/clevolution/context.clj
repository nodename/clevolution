(ns clevolution.context)

(defn int-range
  [lo hi]
  (+ lo (rand-int (- hi lo))))

(defn float-range
  [lo hi]
  (+ lo (rand (- hi lo))))

;; :ns is the name of the namespace in which expressions are to be evaluated
;; :arity is the number of input images
;; the keys in the :params vectors are just documentation
(def version0_1_1 {:ns "clevolution.version.version0-1-1"
                   :ops {"X" {:arity 0}
                         "Y" {:arity 0}
                         "bw-noise" {:arity 0
                                     :params [[:seed '(int-range 50 1000)]
                                              [:octaves '(int-range 1 10)]
                                              [:falloff '(float-range 0.1 1.0)]]}
                         "*" {:arity 1
                              :params [[:factor '(float-range 0.5 2.0)]]}
                         "blur" {:arity 1
                                 :params [[:radius '(float-range 0.0 1.0)]
                                          [:sigma '(float-range 0.5 2.0)]]}
                         "abs" {:arity 1}
                         "sin" {:arity 1}
                         "cos" {:arity 1}
                         "atan" {:arity 1}
                         "log" {:arity 1}
                         "inverse" {:arity 1}
                         "+" {:arity 2}
                         "-" {:arity 2}
                         "and" {:arity 2}
                         "or" {:arity 2}
                         "xor" {:arity 2}
                         "min" {:arity 2}
                         "max" {:arity 2}
                         "mod" {:arity 2}}})


(def contexts {:version0-1-1 version0_1_1})