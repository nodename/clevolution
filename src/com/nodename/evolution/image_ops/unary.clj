(ns com.nodename.evolution.image_ops.unary
  (:import (clojure.contrib.math))
  (:require [rinzelight.effects.blur
             :only [blur]])
  (:use [rinzelight.constants
         :only [quantum-range]]
        [rinzelight.pixel
         :only [create-pixel]]
        [rinzelight.image
         :only [create-image]]
        [rinzelight.effects.basic-effects
         :only [map-image]]))

(defn abs-channel-op
  [color]
  (let [colorF (/ color (quantum-range))
        new-colorF (* 2 (Math/abs (- colorF 0.5)))]
    (int (* new-colorF (quantum-range)))))

(defn sin-channel-op
  [color]
  (let [colorF (/ color (quantum-range))
        new-colorF (* 0.5 (+ 1.0 (Math/sin (* 2 Math/PI colorF))))]
    (int (* new-colorF (quantum-range)))))

(defn cos-channel-op
  [color]
  (let [colorF (/ color (quantum-range))
        new-colorF (* 0.5 (+ 1.0 (Math/cos (* 2 Math/PI colorF))))]
    (int (* new-colorF (quantum-range)))))
  
(defn log-channel-op
  [color]
  (let [colorF (/ color (quantum-range))
        new-colorF (Math/log (+ 1 colorF))]
    (int (* new-colorF (quantum-range)))))
  
(defn inverse-channel-op
  [color]
  (- (quantum-range) color))

(defn pixel-op-generator [channel-op]
  (fn ([p]
      (create-pixel (channel-op (:red   p))
                    (channel-op (:green p))
                    (channel-op (:blue  p))
                    (:alpha p)))
  ([r g b a]
     [(channel-op r)
      (channel-op g)
      (channel-op b)
      a])))

(defn image-op-generator [channel-op]
  (fn
    [bi]
    (:image (map-image (pixel-op-generator channel-op) (create-image bi)))))

(def abs
  (image-op-generator abs-channel-op))

(def sin
  (image-op-generator sin-channel-op))

(def cos
  (image-op-generator cos-channel-op))

(def log
  (image-op-generator log-channel-op))

(def inverse
  (image-op-generator inverse-channel-op))


(defn blur [radius sigma bi]
    (:image (rinzelight.effects.blur/blur (create-image bi) radius sigma)))

