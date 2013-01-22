(ns clevolution.cliskstring
   (:require [clevolution.util :refer :all]))

(def named-colors
   (map #(constantly (with-meta % {:arity 0}))
        ['black 'blue 'cyan 'darkGray 'gray 'green 'lightGray 'magenta 'orange 'pink 'red 'white 'yellow 'purple 'brown]))

(def random-scalar-constant-colors
  [#(rand 1.0)])

(def random-vector-constant-colors
  [#(with-meta [(rand 1.0) (rand 1.0) (rand 1.0)] {:arity 0})])

(def textures
  (map #(constantly (with-meta % {:arity 0}))
       ['agate 'clouds 'velvet 'flecks 'wood]))

; can't find mikera.util.Maths.java version that defines t(), needed for triangle-wave
;(defn random-vector-nullary-operation
;  []
;  'triangle-wave)

;; TODO make-multi-fractal has optional numeric key args
(def unary-v-operators
  "Operators that take one argument, either scalar or vector"
  (map #(constantly (with-meta % {:arity 1}))
       ['vsin 'vcos 'vabs 'vround 'vfloor 'vfrac 'square 'vsqrt 'sigmoid 'tile 'max-component 'min-component 'length 'make-multi-fractal]))

(def nullary-operators-scalar
  "() -> Scalar"
  (map #(constantly (with-meta % {:arity 0}))
       ['x 'y 'perlin-noise 'perlin-snoise 'simplex-noise 'simplex-snoise 'max-component 'min-component 'length]))

(def nullary-operators-vector
  "() -> Vector"
  (map #(constantly (with-meta % {:arity 0}))
       ['vsin 'vcos 'vabs 'vround 'vfloor 'vfrac 'square 'vsqrt 'sigmoid 'tile 'grain]))

(def unary-operators-vector
   "_ -> Vector"
  (map #(constantly (with-meta % {:arity 1}))
       ['rgb-from-hsl]))

;; TODO some of these can do multiple arities; check all
;; TODO cross3 requires two vectors; normalize requires one vector
(def binary-operators
  (map #(constantly (with-meta % {:arity 2}))
       ['v+ 'v* 'v- 'vdivide 'vpow 'vmod 'checker 'scale 'rotate 'offset 'dot 'warp]))

;; TODO provide scalar operands outside (0, 1.0) range


(def ops
  (concat named-colors random-scalar-constant-colors random-vector-constant-colors textures unary-v-operators nullary-operators-scalar nullary-operators-vector unary-operators-vector binary-operators))


;; default arity is zero
;; (perfect spot for a monad)
(defn get-arity
  [x]
  (let [m (meta x)
        arity (if (nil? m) 0 (m :arity))]
    (if (nil? arity) 0 arity)))

(def terminals
  (filter #(zero? (get-arity (%))) ops))

(def nonterminals
  (filter #(not (zero? (get-arity (%)))) ops))

(def non-leaf-choices
  {:grow ops
   :full nonterminals})

;; The full method always fills out the tree so all leaves are at the same depth;
;; the grow method may choose a nullary op, creating a leaf node, before reaching the full depth

(defn random-color
  [depth & {:keys [method]
            :or {method :full}}]
  (let [fns (if (zero? depth) terminals (non-leaf-choices method))
        f (rand-nth fns)
        operation (f)
        arity (get-arity operation)]
    (if (zero? arity)
      operation
      (reduce (fn [expression _]
                (concat expression [(random-color (dec depth))]))
              (list operation) (range arity)))))

