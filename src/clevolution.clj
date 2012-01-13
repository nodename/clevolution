(ns clevolution
	(:import (javax.imageio ImageIO))
	(:import (java.io File))
	(:use [com.nodename.evolution.file-io :only [save-image
                                              get-imagereader
                                              get-generator-string]] :reload-all)
	(:use [com.nodename.evolution.image_ops.gradient :only [X
                                                         Y]] :reload-all)
	(:use [com.nodename.evolution.image_ops.noise :only [create-noise-image]] :reload-all)
	(:use [com.nodename.evolution.image_ops.unary :only [abs
                                                      sin
                                                      cos
                                                      log
                                                      inverse]] :reload-all))

(def image-file-name "test.png")

(def image-width 200)
(def image-height 200)

(defn x-gradient
	[]
	(list 'X image-width image-height))

(defn y-gradient
	[]
	(list 'Y image-width image-height))

(defn bw-noise
  []
  (let [seed (+ 50 (rand-int 950))
        octaves (+ 1 (rand-int 9))
        falloff (+ 0.1 (rand))]
    (list 'create-noise-image seed octaves falloff image-width image-height)))



(def image-creation-ops [(x-gradient) (y-gradient) (bw-noise)])

(defn select-image-creation-op
  []
    (image-creation-ops (rand-int (count image-creation-ops))))
      

(def selected-op (select-image-creation-op))
(println selected-op)

(def unary-op (list 'sin selected-op))
(println unary-op)

 
;;(save-image selected-op image-file-name)
(save-image unary-op image-file-name)

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
