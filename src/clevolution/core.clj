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

;; :arity is the number of input images
;; the keys in the :params vectors are just documentation
(def version0_1_1 {"X" {:arity 0}
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
                   "mod" {:arity 2}})


(def image-width 400)
(def image-height 400)

(defn make-operations
  [operation-map]
  (for [[op-name op-properties] operation-map]
    (let [params (op-properties :params)
          size (if (zero? (op-properties :arity))
                 [image-width image-height]
                 nil)]
      (fn []
        (with-meta (concat [op-name]
                           (for [[param-name param-expr] params]
                             (eval param-expr))
                           size) {:arity (op-properties :arity)})))))

;; read-image-from-file exists outside of any operation-map.
;; It is used whenever input image files are passed to generate-expression.
(defn make-read [uri]
  (fn []
    (with-meta (list "read-image-from-file" (.concat (.concat "\"" uri) "\"")) {:arity 0})))

(defn make-nullary-op-makers
  [operation-map]
  (let [filtered-map (apply dissoc operation-map (for [[key val] operation-map :when (not= (val :arity) 0)] key))]
    (make-operations filtered-map)))

(defn make-non-nullary-op-makers
  [operation-map]
  (let [filtered-map (apply dissoc operation-map (for [[key val] operation-map :when (= (val :arity) 0)] key))]
    (make-operations filtered-map)))
                
(defn make-random-op
  [op-makers]
  (let [op-makers (vec op-makers)
        op-maker (op-makers (rand-int (count op-makers)))]
    (op-maker)))


(defn append-without-flattening
  "Add list-to-append as a single last element of orig-list"
  [orig-list list-to-append]
      (concat orig-list [list-to-append]))

(defn gen-tree
  [depth leaf-choices non-leaf-choices]
  (let [expression (if (zero? depth)
                     (make-random-op leaf-choices)
                     (make-random-op non-leaf-choices))
        op-name (first expression)
        _ (dbg depth)
        _ (dbg op-name)
        arity ((meta expression) :arity)]
    (loop [i 0
           expression expression]
      (if (== i arity)
        expression
        (let [subtree (gen-tree (dec depth) leaf-choices non-leaf-choices)]
          (recur (inc i) (append-without-flattening expression subtree)))))))
      
;      (reduce (fn [expression _]
;                (let [ subtree (gen-tree (dec depth) leaf-choices non-leaf-choices)]
;                  (append-without-flattening expression subtree)))
;                  op-name (range arity))))
         

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

(defn generate-expression
  ([max-depth operation-map input-image-files]
    (let [input-image-op-makers (map make-read input-image-files)
          nullary-op-makers (concat (make-nullary-op-makers operation-map) input-image-op-makers)]
      (generate-tree max-depth nullary-op-makers (make-non-nullary-op-makers operation-map))))
  ([max-depth operation-map]
    (generate-tree max-depth (make-nullary-op-makers operation-map) (make-non-nullary-op-makers operation-map))))

(defn generate-random-image-file
  ([uri version max-depth operation-map input-files]
  (let [expression (tree-to-string (generate-expression max-depth operation-map input-files))]
    (println expression)
    (save-image expression version uri)))
  ([uri version max-depth operation-map]
  (let [expression (tree-to-string (generate-expression max-depth operation-map))]
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
  (generate-expression max-depth version0_1_1)
  ;; OR:
  (generate-expression max-depth version0_1_1 input-files)

  ;; generate a random expression and evaluate it, saving the resulting image to a file:
  (generate-random-image-file output-image-file clevolution-version max-depth version0_1_1)
  ;; OR:
  (generate-random-image-file output-image-file clevolution-version max-depth version0_1_1 input-files)
  
  ;; evaluate an explicit expression, saving the resulting image to a file
  ;; (This one is a Galois field (http://nklein.com/2012/05/visualizing-galois-fields/):
  (save-image "(xor (X 400 400) (Y 400 400))" first-named-version output-image-file)
  
  ;; read back the expression that generated the image in a file:
  (get-generator-string output-image-file)
  ;; and the version in which the image was generated
  (get-clevolution-version output-image-file)
  )