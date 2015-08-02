(ns clevolution.legacy
  (:require [clevolution.util :refer :all]
            [clevolution.context :refer :all]
            [clevolution.file-io :refer :all] :reload-all))

;; LEGACY VERSION BEGIN

(defn make-operations
  [ops-map]
  (for [[op-name op-properties] ops-map]
    (let [params (op-properties :params)]
      (fn []
        (with-meta
          (concat [op-name]
                  (for [[param-name param-expr] params]
                    (eval param-expr)))
          {:arity (op-properties :arity)})))))

;; read-image-from-file exists outside of any ops-map.
;; We create a read-image-from-file op for each input image filename passed to generate-expression.
(defn make-read [uri]
  (fn []
    (with-meta (list "read-image-from-file" (.concat (.concat "\"" uri) "\"")) {:arity 0})))

(defn make-nullary-op-makers
  [ops-map]
  (let [filtered-map (apply dissoc ops-map (for [[key val] ops-map :when (not= (val :arity) 0)] key))]
    (make-operations filtered-map)))

(defn make-non-nullary-op-makers
  [ops-map]
  (let [filtered-map (apply dissoc ops-map (for [[key val] ops-map :when (= (val :arity) 0)] key))]
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
  ([max-depth ops-map input-image-files]
    (let [input-image-op-makers (map make-read input-image-files)
          nullary-op-makers (concat (make-nullary-op-makers ops-map) input-image-op-makers)]
      (generate-tree max-depth nullary-op-makers (make-non-nullary-op-makers ops-map))))
  ([max-depth ops-map]
    (generate-tree max-depth (make-nullary-op-makers ops-map) (make-non-nullary-op-makers ops-map))))

(defn tree-to-string
  [tree]
  (with-out-str (print tree)))

(defn generate-random-image-file
  ([uri max-depth context-name input-files]
  (let [context (contexts (keyword context-name))
        expression (tree-to-string (generate-expression max-depth (context :ops) input-files))]
    (println expression)
    (save-image expression context-name uri)))
  ([uri max-depth context-name]
  (let [context (contexts (keyword context-name))
        expression (tree-to-string (generate-expression max-depth (context :ops)))]
    (println expression)
    (save-image expression context-name uri))))

;; LEGACY VERSION END