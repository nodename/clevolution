(ns clevolution.image_ops.unary
  (:import (java.awt.image BufferedImage))
  (:import (clojure.contrib.math))
  (:require [rinzelight.effects.blur
             :only [blur]])
  (:refer-clojure :exclude [* and or])
  (:use [rinzelight.constants
         :only [quantum-range]]
        [rinzelight.pixel
         :only [create-pixel round-to-quantum]]
        [rinzelight.image
         :only [create-image]]
        [rinzelight.effects.basic-effects
         :only [map-image]]))


(defn- abs-channel-op
  [color]
  (let [colorF (/ color (quantum-range))
        new-colorF (clojure.core/* 2 (Math/abs (- colorF 0.5)))]
    (int (clojure.core/* new-colorF (quantum-range)))))

(defn- sin-channel-op
  [color]
  (let [colorF (/ color (quantum-range))
        new-colorF (clojure.core/* 0.5 (+ 1.0 (Math/sin (clojure.core/* 2 Math/PI colorF))))]
    (int (clojure.core/* new-colorF (quantum-range)))))

(defn- cos-channel-op
  [color]
  (let [colorF (/ color (quantum-range))
        new-colorF (clojure.core/* 0.5 (+ 1.0 (Math/cos (clojure.core/* 2 Math/PI colorF))))]
    (int (clojure.core/* new-colorF (quantum-range)))))

(defn- atan-channel-op
  [color]
  (let [colorF (/ color (quantum-range))
        new-colorF (clojure.core/* (/ 2 Math/PI) (Math/atan (clojure.core/* 10 colorF)))]
    (int (clojure.core/* new-colorF (quantum-range)))))
  
(defn- log-channel-op
  [color]
  (let [colorF (/ color (quantum-range))
        new-colorF (Math/log (+ 1.0 colorF))]
    (int (clojure.core/* new-colorF (quantum-range)))))
  
(defn- inverse-channel-op
  [color]
  (- (quantum-range) color))

(defn- multiply-channel-op-generator [factor]
  (fn
   [color]
   (round-to-quantum(clojure.core/* factor color))))

(defn- pixel-op-generator
  ([channel-op]
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
  ([rgb-op alpha-op]
    (fn ([p]
          (create-pixel (rgb-op (:red   p))
                        (rgb-op (:green p))
                        (rgb-op (:blue  p))
                        (alpha-op (:alpha p))))
      ([r g b a]
        [(rgb-op r)
         (rgb-op g)
         (rgb-op b)
         (alpha-op a)]))))

(defn- image-op-generator [channel-op]
  (fn
    [bi]
    (:image (map-image (pixel-op-generator channel-op) (create-image bi)))))

(def abs
  (image-op-generator abs-channel-op))

(def sin
  (image-op-generator sin-channel-op))

(def cos
  (image-op-generator cos-channel-op))

(def atan
  (image-op-generator atan-channel-op))

(def log
  (image-op-generator log-channel-op))

(def inverse
  (image-op-generator inverse-channel-op))

(defn multiply [factor]
  (image-op-generator (multiply-channel-op-generator factor)))


(defn blur [radius sigma bi]
    (:image (rinzelight.effects.blur/blur (create-image bi) radius sigma)))


(defprotocol Multiply (* [this bi]))

(extend Double Multiply {:* (fn [this bi] ((multiply this) bi))})