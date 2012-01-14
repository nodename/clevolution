(ns clevolution
	(:import (javax.imageio ImageIO))
	(:import (java.io File))
	(:use [com.nodename.evolution.file-io :only [save-image
                                              get-imagereader
                                              get-generator-string]] :reload-all)
	(:use [com.nodename.evolution.image_ops.gradient :only [X
                                                         Y]] :reload-all)
	(:use [com.nodename.evolution.image_ops.noise :only [bw-noise]] :reload-all)
	(:use [com.nodename.evolution.image_ops.unary :only [abs
                                                      sin
                                                      cos
                                                      log
                                                      inverse]] :reload-all))


(def image-file-name "test.png")

(def image-width 200)
(def image-height 200)


(defn make-x-gradient
	[]
	(list 'X image-width image-height))

(defn make-y-gradient
	[]
	(list 'Y image-width image-height))

(defn make-noise
  []
  (let [seed (+ 50 (rand-int 950))
        octaves (+ 1 (rand-int 9))
        falloff (+ 0.1 (rand))]
    (list 'bw-noise seed octaves falloff image-width image-height)))


(def image-creation-ops [(make-x-gradient) (make-y-gradient) (make-noise)])

(def unary-ops ['abs 'sin 'cos 'log 'inverse])
                
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

  
  
(def image-creation-op (select-image-creation-op))
(println image-creation-op)

(def composite-op (list (select-unary-op) image-creation-op))
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
