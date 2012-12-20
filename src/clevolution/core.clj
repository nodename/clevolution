(ns clevolution.core
  (:require [clevolution.util :refer :all]
            [clevolution.file-io :refer :all] :reload-all))

(defn int-range
  [lo hi]
  (+ lo (rand-int (- hi lo))))

(defn float-range
  [lo hi]
  (+ lo (rand (- hi lo))))

(defn tree-to-string
  [tree]
  (with-out-str (print tree)))

(def version0_1_1 {"X" {:arity 0}
                   "Y" {:arity 0}
                   "bw-noise" {:arity 0}
                   "read-image-from-file" {:arity 0}
                   "*" {:arity 1}
                   "blur" {:arity 1}
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
                   "mod" {:arity 2}})

(defn make-bw-noise
  [w h]
  (let [seed (int-range 50 1000)
        octaves (int-range 1 10)
        falloff (float-range 0.1 1.0)]
    (list "bw-noise" seed octaves falloff w h)))

(defn make-make-bw-noise
  [width height]
  (fn []
    (make-bw-noise width height)))

;; TODO cache images?
(defn make-make-read [uri]
  (fn []
    (list "read-image-from-file" (.concat (.concat "\"" uri) "\""))))


(defn make-*
  []
  (let [factor (float-range 0.5 2.0)]
    (list "*" factor)))

(defn make-blur
  []
  (let [radius (float-range 0.0 1.0)
        sigma (float-range 0.5 2.0)]
    (list "blur" radius sigma)))


(defn make-creationary-op-makers
  [w h]
  (list
    (list "X" w h)
    (list "Y" w h)
    (make-make-bw-noise w h)))

(def unary-op-makers
  (conj
    (for [f ["abs" "sin" "cos" "atan" "log" "inverse"]]
      (list f))
    make-*
    make-blur))

(def binary-op-makers
  (for [f ["+" "-" "and" "or" "xor" "min" "max" "mod"]]
        (list f)))
                
(defn make-random-op
  [op-makers]
  (let [op-makers (vec op-makers)
        op-maker (op-makers (rand-int (count op-makers)))]
      (if (ifn? op-maker)
        (op-maker)
        op-maker)))


(defn append-without-flattening
  "Add list-to-append as a single last element of orig-list"
  [orig-list list-to-append]
      (concat orig-list [list-to-append]))


(defn gen-tree
  [depth leaf-choices non-leaf-choices]
  (let [expression (if (zero? depth)
             (make-random-op leaf-choices)
             (make-random-op non-leaf-choices))
        op (first expression)
        _ (dbg depth)
        _ (dbg op)
        arity ((version0_1_1 op) :arity)]
    (loop [i 0
           expression expression]
      (if (== i arity)
        expression
        (let [subtree (gen-tree (dec depth) leaf-choices non-leaf-choices)]
          (recur (inc i) (append-without-flattening expression subtree)))))))
      
;      (reduce (fn [expression _]
;                (let [ subtree (gen-tree (dec depth) leaf-choices non-leaf-choices)]
;                  (append-without-flattening expression subtree)))
;                  op (range arity))))
         

;; The full method always fills out the tree so all leaves are at the same depth;
;; the grow method may choose a nullary op, creating a leaf node, before reaching the full depth
(defn generate-tree
  [depth nullary-op-makers non-nullary-op-makers & {:keys [method]
                                                    :or {method :grow}}]
    (let [leaf-choices nullary-op-makers
          non-leaf-choices-map {:grow (concat nullary-op-makers non-nullary-op-makers)
                                :full non-nullary-op-makers}
          non-leaf-choices (non-leaf-choices-map method)]
      (gen-tree depth leaf-choices non-leaf-choices)))

;; TODO handle input image files whose size doesn't match w and h
(defn generate-expression
  ([max-depth w h input-image-files]
    (let [input-image-op-makers (map make-make-read input-image-files)
          nullary-op-makers (concat (make-creationary-op-makers w h) input-image-op-makers)]
      (generate-tree max-depth nullary-op-makers (concat unary-op-makers binary-op-makers))))
  ([max-depth w h]
    (generate-tree max-depth (make-creationary-op-makers w h) (concat unary-op-makers binary-op-makers))))

(defn generate-random-image-file
  ([uri version max-depth w h input-files]
  (let [expression (tree-to-string (generate-expression max-depth w h input-files))]
    (println expression)
    (save-image expression version uri)))
  ([uri version max-depth w h]
  (let [expression (tree-to-string (generate-expression max-depth w h))]
    (println expression)
    (save-image expression version uri))))

(comment
  
  (load-file "src/clevolution/core.clj")
  (require ['clevolution.core :refer :all])
  
  (def output-image-file "images/test.png")
  
  (def input-files ["images/Dawn_on_Callipygea.png" "images/galois.png"])
  
  (def max-depth 2)
  
  (def image-width 400)
  (def image-height 400)
  
  ;; generate a random expression:
  (generate-expression max-depth image-width image-height)
  ;; OR:
  (generate-expression max-depth image-width image-height input-files)

  ;; generate a random expression and evaluate it, saving the resulting image to a file:
  (generate-random-image-file output-image-file clevolution-version max-depth image-width image-height)
  ;; OR:
  (generate-random-image-file output-image-file clevolution-version max-depth image-width image-height input-files)
  
  ;; evaluate an explicit expression, saving the resulting image to a file
  ;; (This one is a Galois field (http://nklein.com/2012/05/visualizing-galois-fields/):
  (save-image "(xor (X 400 400) (Y 400 400))" first-named-version output-image-file)
  
  ;; read back the expression that generated the image in a file:
  (get-generator-string output-image-file)
  ;; and the version in which the image was generated
  (get-clevolution-version output-image-file)
  )