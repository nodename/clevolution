(ns clevolution.cliskstring)

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
