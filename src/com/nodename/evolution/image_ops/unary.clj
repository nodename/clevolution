(ns com.nodename.evolution.image_ops.unary
  (:import (java.awt.image BufferedImage)
           (clojure.contrib.math)))


(defn unary-pixel-setter [original channel-op y]
   (fn [x bi]
   (let [ rgb (.getRGB original x y)
         red (bit-shift-right (bit-and 0x00ff0000 rgb) 16)
         green (bit-shift-right (bit-and 0x0000ff00 rgb) 8)
         blue (bit-and 0x000000ff rgb)
         argb-color (bit-or 0xff000000 (bit-or (bit-shift-left (channel-op red) 16) (bit-or (bit-shift-left (channel-op green) 8) (channel-op blue))))
         dummy (.setRGB bi x y argb-color)]
     bi)))
 
(defn unary-row-setter [width original channel-op]
  (fn [y bi]
  (let [set-unary-pixel (unary-pixel-setter original channel-op y)]
    (loop [x 0
           image bi]
      (cond
        (== x width) image
        :else (recur (inc x) (set-unary-pixel x image)))))))

(defn unary-image-creator [channel-op]
  (fn [original]
  (let [ width (.getWidth original)
        height (.getHeight original)
        bi (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        set-unary-row (unary-row-setter width original channel-op)]
       (loop [y 0
              image bi]
         (cond
           (== y height) image
           :else (recur (inc y) (set-unary-row y image)))))))

(defn abs-channel-op
  [color]
  (let [colorF (/ color 255)
        new-colorF (* 2 (Math/abs (- colorF 0.5)))]
    (int (* new-colorF 255))))

(def abs
  (unary-image-creator abs-channel-op))

(defn sin-channel-op
  [color]
  (let [colorF (/ color 255)
        new-colorF (* 0.5 (+ 1.0 (Math/sin (* 2 Math/PI colorF))))]
    (int (* new-colorF 255))))

(def sin
  (unary-image-creator sin-channel-op))

(defn cos-channel-op
  [color]
  (let [colorF (/ color 255)
        new-colorF (* 0.5 (+ 1.0 (Math/cos (* 2 Math/PI colorF))))]
    (int (* new-colorF 255))))

(def cos
  (unary-image-creator cos-channel-op))
  
(defn log-channel-op
  [color]
  (let [colorF (/ color 255)
        new-colorF (Math/log (+ 1 colorF))]
    (int (* new-colorF 255))))

(def log
  (unary-image-creator log-channel-op))
  
(defn inverse-channel-op
  [color]
  (255 - color))

(def inverse
  (unary-image-creator inverse-channel-op))
  