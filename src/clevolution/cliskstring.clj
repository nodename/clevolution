(ns clevolution.cliskstring
   (:require [clevolution.util :refer :all]))

(defn make-with-arity
  [arity sym]
  {:function (constantly sym)
   :arity arity})

(def named-colors
   (map (partial make-with-arity 0)
        ['black 'blue 'cyan 'darkGray 'gray 'green 'lightGray 'magenta 'orange 'pink 'red 'white 'yellow 'purple 'brown]))

(def random-scalar-constant-colors
  [{:function (fn [] (rand 1.0)) :arity 0}])

(def random-vector-constant-colors
  [{:function (fn [] [(rand 1.0) (rand 1.0) (rand 1.0)]) :arity 0}])

(def textures
  (map (partial make-with-arity 0)
       ['agate 'clouds 'velvet 'flecks 'wood]))

(defn input-images [uris]
  (let [input-image (fn [uri] {:function (constantly (list 'read-file (.concat (.concat "\"" uri) "\""))) :arity 0})]
    (map input-image uris)))

; can't find mikera.util.Maths.java version that defines t(), needed for triangle-wave
;(defn random-vector-nullary-operation
;  []
;  'triangle-wave)

;; TODO make-multi-fractal has optional numeric key args
(def unary-v-operators
  "Operators that take one argument, either scalar or vector"
  (map (partial make-with-arity 1)
       ['vsin 'vcos 'vabs 'vround 'vfloor 'vfrac 'square 'vsqrt 'sigmoid 'tile 'max-component 'min-component 'length 'make-multi-fractal]))

(def nullary-operators-scalar
  "() -> Scalar"
  (map (partial make-with-arity 0)
       ['x 'y 'perlin-noise 'perlin-snoise 'simplex-noise 'simplex-snoise 'max-component 'min-component 'length]))

(def nullary-operators-vector
  "() -> Vector"
  (map (partial make-with-arity 0)
       ['vsin 'vcos 'vabs 'vround 'vfloor 'vfrac 'square 'vsqrt 'sigmoid 'tile 'grain]))

(def unary-operators-vector
   "_ -> Vector"
  (map (partial make-with-arity 1)
       ['rgb-from-hsl]))

;; TODO some of these can do multiple arities; check all
;; TODO cross3 requires two vectors; normalize requires one vector
(def binary-operators
  (map (partial make-with-arity 2)
       ['v+ 'v* 'v- 'vdivide 'vpow 'vmod 'checker 'scale 'rotate 'offset 'dot 'warp]))

;; TODO provide scalar operands outside (0, 1.0) range


(def ops
  (concat named-colors random-scalar-constant-colors random-vector-constant-colors textures
          unary-v-operators nullary-operators-scalar nullary-operators-vector unary-operators-vector binary-operators))

(defn terminals [ops]
  (filter #(zero? (:arity %)) ops))

(defn nonterminals [ops]
  (filter #(not (zero? (:arity %))) ops))

(defn non-leaf-choices [ops]
  {:grow ops
   :full (nonterminals ops)})

;; The full method always fills out the tree so all leaves are at the same depth;
;; the grow method may choose a nullary op, creating a leaf node, before reaching the full depth

(defn random-clisk-expression
  [depth method input-files]
  (let [ops (concat ops (input-images input-files))
        fns (if (zero? depth) (terminals ops) ((non-leaf-choices ops) method))
        f (rand-nth fns)
        operation ((:function f))
        arity (:arity f)]
    (if (zero? arity)
      operation
      (reduce (fn [expression _]
                (concat expression [(random-clisk-expression (dec depth) method input-files)]))
              (list operation) (range arity)))))

