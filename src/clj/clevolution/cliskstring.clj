(ns clevolution.cliskstring)


(defn make-with-arity
  [arity f]
  {:function f
   :arity arity})


(def named-colors
  (map (partial make-with-arity 0)
       ["sunset-map" "black" "blue" "cyan" "darkGray" "gray" "green" "lightGray"
        "magenta" "orange" "pink" "red" "white" "yellow" "purple" "brown"
        ]))


(def random-scalar-color
  [{:function #(let [color (rand 1.0)]
                (str "[" color " " color " " color "]"))
    :arity 0}])


(def random-vector-color
  [{:function #(str "[" (rand 1.0) " " (rand 1.0) " " (rand 1.0) "]")
    :arity 0}])

(def textures
  (map (partial make-with-arity 0)
       ["agate" "clouds" "velvet" "flecks" "wood"]))

(defn input-images [uris]
  (let [input-image (fn [uri] {:function (constantly (list 'read-file (.concat (.concat "\"" uri) "\"")))
                               :arity 0})]
    (map input-image uris)))

; can't find mikera.util.Maths.java version that defines t(), needed for triangle-wave
;(defn random-vector-nullary-operation
;  []
;  'triangle-wave)


(def make-multi-fractal
  {:function (fn []
               (let [octaves (rand-int 9)
                     lacunarity (rand 10.0)
                     gain (rand 1.0)
                     scale (rand 1.0)]
                 (str "make-multi-fractal % :octaves " octaves " :lacunarity " lacunarity
                      " :gain " gain " :scale " scale)))
   :arity 1})


(def unary-v-operators
  "Operators that take one argument, either scalar or vector"
  (map (partial make-with-arity 1)
       ["vsin" "vcos" "vabs" "vround" "vfloor" "vfrac" "square"
        "vsqrt" "sigmoid" "tile" "max-component" "min-component" "length" "gradient"]))



(def ev-perlin-noise
  {:function (fn []
               (let [seed (.nextLong (mikera.util.Random.))]
                 (str "(ev-perlin-noise " seed ")")))
   :arity 0})


(def ev-perlin-snoise
  {:function (fn []
               (let [seed (.nextLong (mikera.util.Random.))]
                 (str "(ev-perlin-snoise " seed ")")))
   :arity 0})


(def ev-simplex-noise
  {:function (fn []
               (let [seed (.nextLong (mikera.util.Random.))]
                 (str "(ev-simplex-noise " seed ")")))
   :arity 0})

(def ev-simplex-snoise
  {:function (fn []
               (let [seed (.nextLong (mikera.util.Random.))]
                 (str "(ev-simplex-snoise " seed ")")))
   :arity 0})


(def turbulate
  {:function (fn []
               (let [factor (rand 10.0)]
                 (str "turbulate " factor)))
   :arity 1})



(def nullary-operators-scalar
  "() -> Scalar"
  (map (partial make-with-arity 0)
       ["x" "y" "z"
        "max-component" "min-component"]))

(def nullary-operators-vector
  "() -> Vector"
  (map (partial make-with-arity 0)
       ["vsin" "vcos" "vabs" "vround" "vfloor" "vfrac"
        "square" "vsqrt" "sigmoid" "tile" "grain"
        "hash-cubes" "colour-cubes" "plasma" "splasma" "turbulence" "vturbulence"
        "vplasma" "vsplasma" "globe"]))

(def psychedelic
  {:function (fn []
               (let [noise-scale (rand 1.0)
                     noise-bands (rand 10.0)]
                 (str "psychedelic % :noise-scale " noise-scale " :noise-bands " noise-bands)))
   :arity 1})

(def posterize
  {:function (fn []
               (let [bands (inc (rand-int 9))]
                 (str "posterize % :bands " bands)))
   :arity 1})

(def pixelize
  {:function (fn []
               (let [size (rand 10.0)]
                 (str "pixelize " size)))
   :arity 1})

(def shatter
  {:function #(str "shatter % :points " (+ 3 (rand-int 27)))
   :arity 1})

(def radial
  {:function (fn []
               (let [repeat (inc (rand-int 11))]
                 (str "radial % :repeat " repeat)))
   :arity 1})

(def swirl
  {:function (fn []
               (let [rate (rand 10.0)]
                 (str "swirl " rate)))
   :arity 1})

#_(def add-glow {})

(def unary-operators-vector
  "_ -> Vector"
  (map (partial make-with-arity 1)
       ["rgb-from-hsl" "monochrome" "height-normal" "normalize"
        "theta" "radius" "polar"]))

(def unary-scale
  ;; scale by a constant factor
  {:function #(let [factor (rand 5.0)]
               (str "scale " factor))
   :arity 1})

(def unary-offset
  ;; offset by a constant
  {:function #(let [offset [(rand 1.0) (rand 1.0) (rand 1.0)]]
               (str "offset " offset))
   :arity 1})

(def unary-rotate
  ;; rotate by a constant
  {:function #(str "rotate " (rand Math/PI))
   :arity 1})

(def matrix-transform
  {:function #(let [a (rand 100.0)
                    b (rand 100.0)
                    c (rand 100.0)
                    d (rand 100.0)
                    e (rand 100.0)
                    f (rand 100.0)
                    g (rand 100.0)
                    h (rand 100.0)
                    i (rand 100.0)]
               (str "matrix-transform " [[a b c] [d e f] [g h i]]))
   :arity 1})

(def affine-transform
  {:function #(let [a (rand 100.0)
                    b (rand 100.0)
                    c (rand 100.0)
                    d (rand 100.0)
                    e (rand 100.0)
                    f (rand 100.0)
                    g (rand 100.0)
                    h (rand 100.0)
                    i (rand 100.0)]
               (str "affine-transform " [[a b c] [d e f] [g h i]]))
   :arity 1})

(def average
  {:function #(str "average")
   :arity #(+ 2 (rand-int 3))})


;; TODO some of these can do multiple arities; check all
;; TODO cross3 requires two vectors; normalize requires one vector
(def binary-operators
  (map (partial make-with-arity 2)
       ["v+" "v*" "v-" "vdivide" "vpow" "vmod" #_"checker" ;; checker is boring
        "scale" "rotate" "offset" "dot" "warp" "compose" "cross3" "light-value"]))



(def ops
  (concat #_named-colors ;; boring
    random-scalar-color random-vector-color
    textures unary-v-operators nullary-operators-scalar nullary-operators-vector
    unary-operators-vector binary-operators
    [#_psychedelic ;; overdone
     posterize pixelize radial swirl make-multi-fractal
     unary-scale #_unary-offset unary-rotate #_shatter ;; not using shatter pending resolution of eval issue on :objects map
     ev-perlin-noise ev-perlin-snoise ev-simplex-noise ev-simplex-snoise
     turbulate matrix-transform affine-transform average]
    ))



(defn operation
  "(:function op) must be a string or a function that returns a string"
  [op]
  (let [op (:function op)]
    (if (= (class op) String)
      op
      (op))))

(defn arity
  "(:arity op) must be a Long or a Number or a function that returns a Long or a Number"
  [op]
  (let [arity (:arity op)]
    (if (or (= (class arity) java.lang.Long)
            (= (class arity) java.lang.Number))
      arity
      (arity))))


(defn terminals [ops]
  (filter #(zero? (arity %)) ops))

(defn nonterminals [ops]
  (filter #(not (zero? (arity %))) ops))

(defn non-leaf-choices [ops]
  {:grow ops
   :full (nonterminals ops)})


;; The :full method always fills out the tree so all leaves are at the same depth;
;; the :grow method may choose a nullary op, creating a leaf node, before reaching the full depth

(defn random-clisk-expression
  [depth method input-files]
  (let [ops (concat ops (input-images input-files))
        fns (if (zero? depth)
              (terminals ops)
              ((non-leaf-choices ops) method))
        f (rand-nth fns)
        operation (operation f)
        arity (arity f)]
    (if (zero? arity)
      operation
      (let [build-subexpr (fn [expression _]
                            (let [child-expr (random-clisk-expression (dec depth) method input-files)]
                              ;; "%", if present, represents the position in the expression
                              ;; where the image argument should be inserted:
                              (if (= -1 (.indexOf (first expression) "%"))
                                ;; default: image argument goes at the end:
                                (concat expression (list child-expr))
                                ;; if "%" is found, image arg replaces it:
                                (let [new-first (.replaceFirst (first expression) "%"
                                                               (with-out-str (print child-expr)))]
                                  (concat (list new-first) (rest expression))))))]
        (reduce build-subexpr (list operation) (range arity))))))


