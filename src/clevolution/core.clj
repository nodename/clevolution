(ns clevolution.core
  (:require [clevolution.util :refer :all]
            [clevolution.context :refer :all]
            [clevolution.file-io :refer :all]
            [clevolution.cliskstring :refer [random-color]]
            [clevolution.cliskenv :refer [make-clisk-image]] :reload-all))

(def default-depth 2)

(defn random-clisk-string
  ([] (random-clisk-string default-depth))
  ([depth] (with-out-str (print (random-color depth)))))

(defn clisk-eval
  ([^String generator]
    (clisk-eval generator 256 256))
  ([^String generator w h]
  (let [form (read-string generator)
        orig-ns *ns*]
    (in-ns 'clevolution.cliskenv)
    (try
      (make-clisk-image form w h)
      (catch Exception e
        (println "Error:" (.getMessage e))
        (make-clisk-image 0.0 w h))
      (finally (in-ns (ns-name orig-ns)))))))

(defn save-clisk-image
  "Generate and save an image from generator"
  ([^String generator ^String uri]
    (save-clisk-image generator 256 256 uri))
  ([^String generator w h ^String uri]
 (let [context-name "clisk"
       _ (dbg generator)
       metadata (make-generator-metadata generator context-name)
       image (clisk-eval generator w h)]
   (write-image-to-file image metadata uri))))


(defn uri-for-index
  [file-path index]
    (str file-path (format "%04d" index) ".png"))

(defn make-random-clisk-file
  ([output-file-path index]
    (make-random-clisk-file output-file-path index default-depth))
  ([output-file-path index depth]
    (let [output-uri (uri-for-index output-file-path index)]
      (save-clisk-image (random-clisk-string depth) output-uri))))

(defn get-generator-string
  [source]
  (get-chunk-data source generator-chunk-name))

(defn resize-file
  "Same image, stretched or compressed to fit new dimensions"
  [in-uri w h out-uri]
  (let [gstring (get-generator-string in-uri)]
    (save-clisk-image gstring w h out-uri)))

(defn zoom-file
  "Zoom in: factor > 1; zoom out: factor < 1; works from top left, not center"
  ([in-uri factor out-uri]
    (zoom-file in-uri factor (get-width in-uri) (get-height in-uri) out-uri))
  ([in-uri factor w h out-uri]
  (let [gstring (get-generator-string in-uri)
        nstring (str "(scale " factor " " gstring ")")]
    (save-clisk-image nstring w h out-uri))))



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

(comment
  ;; LEGACY VERSION code examples are commented out but still work for now, but SLOWLY:
  
  (load-file "src/clevolution/core.clj")
  (require ['clevolution.core :refer :all])
  
  (def output-file "images/test.png")
  
  ;;(def input-files ["images/Dawn_on_Callipygea.png" "images/galois.png"])
  
  (def depth 2)
  
  ;; generate a random expression:
  ;; (generate-expression depth ((contexts :version0-1-1) :ops))
  ;; OR:
  ;; (generate-expression depth ((contexts :version0-1-1) :ops) input-files)
  (random-clisk-string depth)

  ;; generate a random expression and evaluate it, saving the resulting image to a file:
  ;; (generate-random-image-file output-file depth "version0-1-1")
  ;; OR:
  ;; (generate-random-image-file output-file depth "version0-1-1" input-files)
  (save-clisk-image (random-clisk-string depth) output-file)
                    
  ;; evaluate an explicit expression, saving the resulting image to a file
  ;; (This one is a Galois field (http://nklein.com/2012/05/visualizing-galois-fields/):
  ;; (save-image "(xor (X) (Y))" "version0-1-1" output-file)
  (save-clisk-image "(vxor x y)" output-file)
  
  ;; generate 1000 random expressions, saving each with its image to a file:
  (def output-file-path "F:\\clisk-images\\")
  (dotimes [n 1000]
    (make-random-clisk-file output-file-path n))
 
  ;; Or to be explicit about depth 
  (dotimes [n 1000]
    (make-random-clisk-file output-file-path n depth))
  
  ;; read back the expression that generated the image in a file:
  (get-generator-string output-file)
  
  ;; re-evaluate an image's generator expression at a given width and height, and save it to another file:
  (resize-file output-file 800 800 big-output-file)
  )
