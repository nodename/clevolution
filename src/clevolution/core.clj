(ns clevolution.core
  (:require [clevolution.util :refer :all]
            [clevolution.context :refer :all]
            [clevolution.file-io :refer :all]
            [clevolution.cliskenv :refer :all] :reload-all))

(defn rand-member
  "Choose a random element from one or more sequences"
  [& seqs]
  (let [v (vec (mapcat identity seqs))]
    (v (rand-int (count v)))))

(defn random-named-color
  []
  (let [names ["black" "blue" "cyan" "darkGray" "gray" "green" "lightGray" "magenta" "orange" "pink" "red" "white" "yellow" "purple" "brown"]
        symbols (map symbol names)]
    (rand-member symbols)))

;; TODO perhaps different ranges for different functions
(defn random-scalar-constant
  ([]
    (random-scalar-constant 6.0))
  ([limit]
    (rand limit)))

;; TODO make-multi-fractal has optional numeric key args
(def unary-v-operators
  "Operators that take one argument, either scalar or vector"
  (map symbol ["vsin" "vcos" "vabs" "vround" "vfloor" "vfrac" "square" "vsqrt" "sigmoid" "tile" "max-component" "min-component" "length" "make-multi-fractal"]))

(defn random-nullary-operator-scalar
  "() -> Scalar"
  []
  (let [names ["x" "y" "perlin-noise" "perlin-snoise" "simplex-noise" "simplex-snoise" "max-component" "min-component" "length"]
        symbols (map symbol names)]
    (rand-member symbols)))

(defn random-nullary-operator-vector
  "() -> Vector"
  []
  (let [names ["vsin" "vcos" "vabs" "vround" "vfloor" "vfrac" "square" "vsqrt" "sigmoid" "tile" "grain"]
        symbols (map symbol names)]
    (rand-member symbols)))

(defn random-unary-v-operator
  []
    (rand-member unary-v-operators))

(def unary-operators-vector
  (map symbol ["rgb-from-hsl"]))

(defn random-unary-operator-vector
  "_ -> Vector"
  []
  (rand-member unary-operators-vector))

(defn random-unary-operator
  []
    (rand-member unary-v-operators unary-operators-vector))

;; TODO some of these can do multiple arities; check all
;; TODO cross3 requires two vectors; normalize requires one vector
(defn random-binary-operator
  []
  (let [names ["v+" "v*" "v-" "vdivide" "vpow" "vmod" "checker" "scale" "rotate" "offset" "dot" "warp"]
        symbols (map symbol names)]
    (rand-member symbols)))

(declare random-scalar-color)
(defn random-scalar-unary-operation
  []
  (let [a (random-scalar-color)]
    (list (random-unary-v-operator) a)))

(defn random-scalar-binary-operation
  []
  (let [a (random-scalar-color)
        b (random-scalar-color)]
    (list (random-binary-operator) a b)))

(defn random-scalar-color
  []
  (let [fns [random-scalar-constant random-nullary-operator-scalar random-scalar-unary-operation random-scalar-binary-operation]
        operation (rand-member fns)]
    (operation)))

(defn random-rgb-color
  []
  [(random-scalar-color) (random-scalar-color) (random-scalar-color)])

(declare random-vector-color)
(declare random-color)

(comment can't find mikera.util.Maths.java version that defines t(), needed for triangle-wave
(defn random-vector-nullary-operation
  []
  (symbol "triangle-wave"))
)

(defn random-vector-unary-operation
  []
  (let [a (random-color)]
    (list (random-unary-operator) a)))

(defn random-vector-binary-operation
  []
  (let [a (random-color)
        b (random-vector-color)]
    (list (random-binary-operator) a b)))

(defn random-vector-color
  []
  (let [fns [random-rgb-color random-named-color random-nullary-operator-vector random-vector-unary-operation random-vector-binary-operation]
        operation (rand-member fns)]
    (operation)))

(defn random-color
  []
  (let [fns [random-scalar-color random-vector-color]
        operation (rand-member fns)]
    (operation)))

(defn random-clisk-string
  []
  (str (random-color)))


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
  [output-file-path index]
  (let [output-uri (uri-for-index output-file-path index)]
    (save-clisk-image (random-clisk-string) output-uri)))




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
  ;; LEGACY VERSION code examples are commented out but still work for now:
  
  (load-file "src/clevolution/core.clj")
  (require ['clevolution.core :refer :all])
  
  (def output-file "images/test.png")
  
  ;;(def input-files ["images/Dawn_on_Callipygea.png" "images/galois.png"])
  
  ;;(def max-depth 2)
  
  ;; generate a random expression:
  ;; (generate-expression max-depth ((contexts :version0-1-1) :ops))
  ;; OR:
  ;; (generate-expression max-depth ((contexts :version0-1-1) :ops) input-files)

  ;; generate a random expression and evaluate it, saving the resulting image to a file:
  ;; (generate-random-image-file output-file max-depth "version0-1-1")
  ;; OR:
  ;; (generate-random-image-file output-file max-depth "version0-1-1" input-files)
  (save-clisk-image (random-clisk-string) output-file)
                    
  ;; evaluate an explicit expression, saving the resulting image to a file
  ;; (This one is a Galois field (http://nklein.com/2012/05/visualizing-galois-fields/):
  ;; (save-image "(xor (X) (Y))" "version0-1-1" output-file)
  (save-clisk-image "(vxor x y)" output-file)
  
  ;; generate 1000 random expressions, saving each with its image to a file:
  (def output-file-path "F:\\clisk-images\\")
  (dotimes [n 1000]
    (make-random-clisk-file output-file-path n))
  
  
  ;; read back the expression that generated the image in a file:
  (get-generator-string output-file)
  
  ;; re-evaluate an image's generator expression at a given width and height, and save it to another file:
  (resize-file output-file 800 800 big-output-file)
  )
