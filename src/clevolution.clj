(ns clevolution
  (:import [javax.imageio ImageIO]
           [java.io File])
  (:refer-clojure :exclude [* + - and or min max mod])
  (:use [com.nodename.evolution.file-io :only [save-image
                                              get-generator-string
                                              read-image-from-file]]
        [clevlib :only [generate-random-image-file]]
        [com.nodename.evolution.image_ops.zeroary.gradient :only [X Y]]
        [com.nodename.evolution.image_ops.zeroary.noise :only [bw-noise]]
        [com.nodename.evolution.image_ops.unary :only [abs sin cos log inverse blur *]]
        [com.nodename.evolution.image_ops.binary :only [+ - and or xor min max mod]] :reload-all))

(def image-file-name "F:\\generated-images\\test.png")
(def input-files (list "F:\\generated-images\\z-movie\\0000.png" "F:\\generated-images\\z-movie\\0453.png"))

(def max-depth 3)

;;(generate-random-image-file image-file-name max-depth input-files)
(generate-random-image-file image-file-name max-depth)

;; face: (or (sin (cos (bw-noise 735 9 0.37795912888484595 200 200))) (cos (sin (bw-noise 83 7 0.18731404903090182 200 200))))
