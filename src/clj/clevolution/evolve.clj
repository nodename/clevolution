(ns clevolution.evolve
  (:require [clojure.pprint :refer [pprint]]))

;; based upon https://raw.githubusercontent.com/lspector/gp/master/src/gp/evolvefn.clj

;; To help write mutation and crossover functions we'll write a utility
;; function that returns a random subtree from an expression and another that
;; replaces a random subtree of an expression.

(defn codesize
  [c]
  (if (list? c) ;; we regard a vector as a primitive
    (count (flatten c))
    1))

(defn random-subtree
  [tree]
  (if (zero? ^long (rand-int (codesize tree)))
    tree
    (let [foo (apply concat
                     (map #(repeat (codesize %) %)
                          (rest tree)))]
      (random-subtree
        (rand-nth
          foo)))))

;(random-subtree '(+ (* x (+ y z)) w))

(defn replace-random-subtree
  [tree replacement]
  (if (zero? ^long (rand-int (codesize tree)))
    replacement
    (let [positions (apply concat
                           (map #(repeat (codesize %1) %2)
                                (rest tree)
                                (iterate inc 1)))
          node-at (fn [position] (get (vec tree) position))
          ;; Don't replace a keyword:
          may-replace? (fn [position] (not (keyword? (node-at position))))
          may-replace-tree? (fn [] (some may-replace? positions))]
      (if-not (may-replace-tree?)
        tree
        (let [replace (fn [position-to-change]
                        (map (fn [replace? subtree]
                               (if replace?
                                 (replace-random-subtree subtree replacement)
                                 subtree))
                             (for [n (iterate inc 0)] (= n position-to-change))
                             tree))]

          (loop [position-to-change (rand-nth positions)]
            (if-not (may-replace? position-to-change)
              (recur (rand-nth positions))
              (replace position-to-change))))))))


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