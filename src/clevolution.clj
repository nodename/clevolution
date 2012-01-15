(ns clevolution
  (:import [javax.imageio ImageIO]
          [java.io File])
  (:use
        [com.nodename.evolution.file-io :only [save-image
                                              get-imagereader
                                              get-generator-string]]
        [com.nodename.evolution.image_ops.gradient :only [X
                                                         Y]]
        [com.nodename.evolution.image_ops.noise :only [bw-noise]]
        [com.nodename.evolution.image_ops.unary :only [abs
                                                      sin
                                                      cos
                                                      log
                                                      inverse
                                                      blur]] :reload-all))


(def image-file-name "test.png")

(def image-width 200)
(def image-height 200)

(defn int-range
  [lo hi]
  (+ lo (rand-int (- hi lo))))

(defn float-range
  [lo hi]
  (+ lo (rand (- hi lo))))

(defn make-x-gradient
	[]
	(list 'X image-width image-height))

(defn make-y-gradient
	[]
	(list 'Y image-width image-height))

(defn make-noise
  []
  (let [seed (int-range 50 1000)
        octaves (int-range 1 10)
        falloff (float-range 0.1 1.0)]
    (list 'bw-noise seed octaves falloff image-width image-height)))

(defn make-blur
  []
  (let [radius (float-range 0.0 1.0)
        sigma (float-range 0.5 2.0)]
    (list 'blur radius sigma)))


(def image-creation-ops [(make-x-gradient) (make-y-gradient) (make-noise)])

(def unary-ops ['abs 'sin 'cos 'log 'inverse (make-blur)])
                
(defn select-random-op
  ([]
    (select-random-op (concat image-creation-ops unary-ops)))
  ([ops]
    (ops (rand-int (count ops)))))

(defn select-image-creation-op
  []
  (select-random-op image-creation-ops))

(defn select-unary-op
  []
  (select-random-op unary-ops))

  
  
(defn compose-ops
  [unary-op image-creation-op]
  (if (list? unary-op)
    (seq(conj (vec unary-op) image-creation-op)) ;; append image-creation-op as a list without deconstructing it
    (list unary-op image-creation-op)))

(def composite-op (compose-ops (select-unary-op) (select-image-creation-op)))
(println composite-op)
 
(save-image composite-op image-file-name)

;; now reopen the file and read the header

(def input-stream (ImageIO/createImageInputStream (File. image-file-name)))


(def imagereader (get-imagereader input-stream))
(.setInput imagereader input-stream true)

(def image-index 0)
(def input-metadata (.getImageMetadata imagereader image-index))
(.close input-stream)
(.dispose imagereader)


(def input-generator (get-generator-string input-metadata))
(println input-generator)
