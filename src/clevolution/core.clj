(ns clevolution.core
  (:refer-clojure :exclude [* + - and or min max mod])
  (:use [clevolution.image_ops.nullary.gradient :only [X Y]]
        [clevolution.image_ops.nullary.noise :only [bw-noise]]
        [clevolution.file-io :only [read-image-from-file]]
        [clevolution.image_ops.unary :only [abs sin cos atan log inverse blur *]]
        [clevolution.image_ops.binary :only [+ - and or xor min max mod]]
        [clevolution.file-io :only [save-image]] :reload-all))

;; http://blog.jayway.com/2011/03/13/dbg-a-cool-little-clojure-macro/
(defmacro dbg [& body]
  `(let [x# ~@body]
     (println (str "dbg: " (quote ~@body) "=" x#))
     x#))


(def image-width 400)
(def image-height 400)

(defn- int-range
  [lo hi]
  (+ lo (rand-int (- hi lo))))

(defn- float-range
  [lo hi]
  (+ lo (rand (- hi lo))))


(defn- make-with-arity [arity operator & params]
  (fn []
    (with-meta (conj params operator) {:arity arity})))


(def make-X (make-with-arity 0 'X image-width image-height))
(def make-Y (make-with-arity 0 'Y image-width image-height))

(def make-abs (make-with-arity 1 'abs))
(def make-sin (make-with-arity 1 'sin))
(def make-cos (make-with-arity 1 'cos))
(def make-atan (make-with-arity 1 'atan))
(def make-log (make-with-arity 1 'log))
(def make-inverse (make-with-arity 1 'inverse))

(def make-+ (make-with-arity 2 '+))
(def make-- (make-with-arity 2 '-))
(def make-and (make-with-arity 2 'and))
(def make-or (make-with-arity 2 'or))
(def make-xor (make-with-arity 2 'xor))
(def make-min (make-with-arity 2 'min))
(def make-max (make-with-arity 2 'max))
(def make-mod (make-with-arity 2 'mod))
 
(defn make-bw-noise
  []
  (let [seed (int-range 50 1000)
        octaves (int-range 1 10)
        falloff (float-range 0.1 1.0)]
    ((make-with-arity 0 'bw-noise seed octaves falloff image-width image-height))))

;; TODO cache images
(defn- make-make-read [uri]
  (fn []
    ((make-with-arity 0 'read-image-from-file uri))))


(defn- make-*
  []
  (let [factor (float-range 0.5 2.0)]
    ((make-with-arity 1 '* factor))))

(defn- make-blur
  []
  (let [radius (float-range 0.0 1.0)
        sigma (float-range 0.5 2.0)]
    ((make-with-arity 1 'blur radius sigma))))


(def zeroary-op-makers
  [make-X make-Y make-bw-noise])

(def unary-op-makers
  [make-abs make-sin make-cos make-atan make-log make-inverse make-* make-blur])

(def binary-op-makers
  [make-+ make-- make-and make-or make-xor make-min make-max make-mod])

                
(defn- make-random-op
  "Select and evaluate a random function from one or more vectors"
  [op-makers & more]
    (let [op-makers 
          (if more
            (vec (concat op-makers (reduce concat more)))
            op-makers)]
      ; note the extra parens used to evaluate the selected op-maker:
      ((op-makers (rand-int (count op-makers))))))


(defn- append-without-flattening
  "Add list-to-append as a single last element of orig-list"
  [orig-list list-to-append]
      (seq (conj (vec orig-list) list-to-append)))

(defn- compose-ops
  ([binary-op op0 op1]
    (compose-ops (compose-ops binary-op op0) op1))
  ([unary-op op]
    (if (seq? unary-op)
      (append-without-flattening unary-op op)
      (conj unary-op op))))


(defn- foo
  [depth zeroary-ops]
    (let [op (if (zero? depth)
               (make-random-op zeroary-ops)
               (make-random-op zeroary-ops unary-op-makers binary-op-makers))
          arity ((meta op) :arity)]
      (loop [i 0
            expression op]
        (cond
          (== i arity) expression
          :else (recur (inc i) (compose-ops expression (foo (dec depth) zeroary-ops)))))))


;; TODO manage width and height of input images
(defn generate-expression
  ([max-depth input-image-files]
    (let [input-image-op-makers (vec (map make-make-read input-image-files))
          my-zeroary-ops (vec (concat zeroary-op-makers input-image-op-makers))
  ;       my-zeroary-ops input-image-op-makers
          ]
      (foo max-depth my-zeroary-ops)))
  ([max-depth]
    (foo max-depth zeroary-op-makers)))


(defn generate-random-image-file
  ([uri max-depth input-files]
  (let [expression (generate-expression max-depth input-files)]
    (println expression)
    (save-image expression uri)))
  ([uri max-depth]
  (let [expression (generate-expression max-depth)]
    (println expression)
    (save-image expression uri))))

