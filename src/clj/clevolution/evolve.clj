(ns clevolution.evolve
  (:require [clojure.pprint :refer [pprint]]))

;; based upon https://raw.githubusercontent.com/lspector/gp/master/src/gp/evolvefn.clj


;; To help write mutation and crossover functions we'll write a utility
;; function that returns a random subtree from an expression and another that
;; replaces a random subtree of an expression.

(defn codesize [c]
  (if (list? c) ;; we regard a vector as a primitive
    (count (flatten c))
    1))

(defn random-subtree
  [tree]
  (println)
  (println "random-subtree: tree=")
  (pprint tree)
  (println)
  (if (zero? (rand-int (codesize tree)))
    tree
    (let [foo (apply concat
                     (map #(repeat (codesize %) %)
                          (rest tree)))]
      (println)
      (println "random-subtree: foo=")
      (pprint foo)
      (println)
      (random-subtree
        (rand-nth
          foo)))))

;(random-subtree '(+ (* x (+ y z)) w))

(defn replace-random-subtree
  [tree replacement]
  ;(println)
  ;(pprint ["TREE:" tree])
  ;(println)
  (if (zero? (rand-int (codesize tree)))
    replacement
    (let [foo (apply concat
                     (map #(repeat (codesize %1) %2)
                          (rest tree)
                          (iterate inc 1)))

          position-to-change (rand-nth foo)]

      ;(println)
      ;(pprint ["FOO:" foo])
      ;(println)

      (map #(if %1 (replace-random-subtree %2 replacement) %2)
           (for [n (iterate inc 0)] (= n position-to-change))
           tree))))

;(replace-random-subtree '(0 (1) (2 2) (3 3 3) (4 4 4 4) (5 5 5 5 5) (6 6 6 6 6 6 6)) 'x)

;(replace-random-subtree '(+ (* x (+ y z)) w) 3)

#_
    (defn mutate
      [i]
      (replace-random-subtree i (random-code 2)))

;(mutate '(+ (* x (+ y z)) w))

(defn crossover
  [i j]
  (replace-random-subtree i (random-subtree j)))

;(crossover '(+ (* x (+ y z)) w) '(/ a (/ (/ b c) d)))

; We can see some mutations with:
; (let [i (random-code 2)] (println (mutate i) "from individual" i))

; and crossovers with:
; (let [i (random-code 2) j (random-code 2)]
;   (println (crossover i j) "from" i "and" j))

;(let [e '(* x 2)
;      m (mutate e)]
; (println (error e) e)
; (println (error m) m))
;
;(let [e1 '(* x 2)
;      e2 '(+ (* x 3) 4)
;      c (crossover e1 e2)]
; (println (error e1) e1)
; (println (error e2) e2)
; (println (error c) c))