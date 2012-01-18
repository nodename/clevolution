(ns clevolution
  (:import [javax.imageio ImageIO]
           [java.io File])
  (:refer-clojure :exclude [* + - and or min max mod])
  (:use [com.nodename.evolution.file-io :only [save-image
                                              get-imagereader
                                              get-generator-string]]
        [clevlib :only [generate-expression]]
        [com.nodename.evolution.image_ops.zeroary.gradient :only [X Y]]
        [com.nodename.evolution.image_ops.zeroary.noise :only [bw-noise]]
        [com.nodename.evolution.image_ops.unary :only [abs sin cos log inverse blur *]]
        [com.nodename.evolution.image_ops.binary :only [+ - and or xor min max mod]] :reload-all))

(def image-file-name "test.png")

;;(def expression (generate-expression))
(def expression '(abs (abs (sin (bw-noise 342 1 0.49348922739109613 400 400)))))

(println expression)

(save-image expression image-file-name)


;; now reopen the file and read the header

(def input-generator (get-generator-string image-file-name))
(println input-generator)


;; face: (or (sin (cos (bw-noise 735 9 0.37795912888484595 200 200))) (cos (sin (bw-noise 83 7 0.18731404903090182 200 200))))
;; cool strings: (abs (abs (sin (bw-noise 342 1 0.49348922739109613 200 200))))