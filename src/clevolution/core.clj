(ns clevolution.core
  (:refer-clojure :exclude [* + - and or min max mod])
  (:use [clevolution.image_ops.nullary.gradient :only [X Y]]
        [clevolution.image_ops.nullary.noise :only [bw-noise]]
        [clevolution.file-io :only [read-image-from-file]]
        [clevolution.image_ops.unary :only [abs sin cos atan log inverse blur *]]
        [clevolution.image_ops.binary :only [+ - and or xor min max mod]]
        [clevolution.file-io] :reload-all))

;; http://blog.jayway.com/2011/03/13/dbg-a-cool-little-clojure-macro/
(defmacro dbg [& body]
  `(let [x# ~@body]
     (println (str "dbg: " (quote ~@body) "=" x#))
     x#))

(defn int-range
  [lo hi]
  (+ lo (rand-int (- hi lo))))

(defn float-range
  [lo hi]
  (+ lo (rand (- hi lo))))


(defn make-with-arity [arity operator & params]
  (fn []
    (with-meta (conj params operator) {:arity arity})))

 
(defn make-bw-noise
  [w h]
  (let [seed (int-range 50 1000)
        octaves (int-range 1 10)
        falloff (float-range 0.1 1.0)
        the-function (make-with-arity 0 'bw-noise seed octaves falloff w h)]
    (the-function)))

(defn make-make-bw-noise
  [width height]
  (fn []
    (make-bw-noise width height)))
    

;; TODO cache images?
(defn make-make-read [uri]
  (fn []
    (let [the-function (make-with-arity 0 'read-image-from-file uri)]
      (the-function))))


(defn make-*
  []
  (let [factor (float-range 0.5 2.0)
        the-function (make-with-arity 1 '* factor)]
    (the-function)))

(defn make-blur
  []
  (let [radius (float-range 0.0 1.0)
        sigma (float-range 0.5 2.0)
        the-function (make-with-arity 1 'blur radius sigma)]
    (the-function)))


(defn make-creationary-op-makers
  [w h]
  [(make-with-arity 0 'X w h)
   (make-with-arity 0 'Y w h)
   (make-make-bw-noise w h)])

(def unary-op-makers
  [(make-with-arity 1 'abs)
   (make-with-arity 1 'sin)
   (make-with-arity 1 'cos)
   (make-with-arity 1 'atan)
   (make-with-arity 1 'log)
   (make-with-arity 1 'inverse)
    make-*
    make-blur])

(def binary-op-makers
  [(make-with-arity 2 '+)
   (make-with-arity 2 '-)
   (make-with-arity 2 'and)
   (make-with-arity 2 'or)
   (make-with-arity 2 'xor)
   (make-with-arity 2 'min)
   (make-with-arity 2 'max)
   (make-with-arity 2 'mod)])

                
(defn make-random-op
  "Select and execute a random function from a seq"
  [op-makers]
  (let [op-makers (vec op-makers)
        the-function (op-makers (rand-int (count op-makers)))]
      (the-function)))


(defn append-without-flattening
  "Add list-to-append as a single last element of orig-list"
  [orig-list list-to-append]
      (concat orig-list (list list-to-append)))

;; The full method always fills out the tree so all leaves are at the same depth;
;; the grow method may choose a nullary op, creating a leaf node, before reaching the full depth
(defn generate-tree
  [depth nullary-op-makers non-nullary-op-makers & {:keys [method]
                                                    :or {method :grow}}]
    (let [non-leaf-choices {:grow (concat nullary-op-makers non-nullary-op-makers)
                            :full non-nullary-op-makers}
          op (if (zero? depth)
               (make-random-op nullary-op-makers)
               (make-random-op (non-leaf-choices method)))
          _ (dbg depth)
          _ (dbg op)
          arity ((meta op) :arity)]
      (loop [i 0
            expression op]
       (if (== i arity)
         expression
         (let [subtree (generate-tree (dec depth) nullary-op-makers non-nullary-op-makers)]
           (recur (inc i) (append-without-flattening expression subtree)))))))

;; TODO handle input image files whose size doesn't match w and h
(defn generate-expression
  ([max-depth w h input-image-files]
    (let [input-image-op-makers (map make-make-read input-image-files)
          nullary-op-makers (concat (make-creationary-op-makers w h) input-image-op-makers)]
      (generate-tree max-depth nullary-op-makers (concat unary-op-makers binary-op-makers))))
  ([max-depth w h]
    (generate-tree max-depth (make-creationary-op-makers w h) (concat unary-op-makers binary-op-makers))))

(defn generate-random-image-file
  ([uri max-depth w h input-files]
  (let [expression (generate-expression max-depth w h input-files)]
    (println expression)
    (save-image expression uri)))
  ([uri max-depth w h]
  (let [expression (generate-expression max-depth w h)]
    (println expression)
    (save-image expression uri))))

(comment
  
  (load-file "src/clevolution/core.clj")
  (in-ns 'clevolution.core)
  
  (def output-image-file "images/test.png")
  
  (def input-files (list "images/Dawn_on_Callipygea.png"))
  
  (def max-depth 2)
  
  (def image-width 400)
  (def image-height 400)
  
  ;; generate a random expression:
  (generate-expression max-depth image-width image-height)
  ;; OR:
  (generate-expression max-depth image-width image-height input-files)

  ;; generate a random expression and evaluate it, saving the resulting image to a file:
  (generate-random-image-file output-image-file max-depth image-width image-height)
  ;; OR:
  (generate-random-image-file output-image-file max-depth image-width image-height input-files)
  
  ;; evaluate an explicit expression, saving the resulting image to a file
  ;; (This one is a Galois field (http://nklein.com/2012/05/visualizing-galois-fields/):
  (save-image '(xor (X 400 400) (Y 400 400)) output-image-file)
  
  ;; read back the expression that generated the image in a file:
  (get-generator-string output-image-file)
  )