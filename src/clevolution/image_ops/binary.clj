(ns clevolution.image_ops.binary
  (:import (java.awt.image BufferedImage))
  (:refer-clojure :exclude [+ - and or min max mod])
  (:use [rinzelight.pixel
           :only [create-pixel round-to-quantum]]
          [rinzelight.image
           :only [create-image]]
          [rinzelight.effects.basic-effects
           :only [map-image]] :reload-all))


(defn- plus-channel-op
  [color0 color1]
  (round-to-quantum (clojure.core/+ color0 color1)))

(defn- minus-channel-op
  [color0 color1]
  (round-to-quantum (clojure.core/- color0 color1)))

(defn- and-channel-op
  [color0 color1]
  (bit-and color0 color1))

(defn- or-channel-op
  [color0 color1]
  (bit-or color0 color1))

(defn- xor-channel-op
  [color0 color1]
  (bit-xor color0 color1))

(defn- min-channel-op
  [color0 color1]
  (clojure.core/min color0 color1))

(defn- max-channel-op
  [color0 color1]
  (clojure.core/max color0 color1))

(defn- mod-channel-op
  [color0 color1]
  (if (= color1 0)
    color0
    (clojure.core/mod color0 color1)))

(defn- pixel-op-generator [channel-op]
  (fn 
    ([p0 p1]
      (create-pixel (channel-op (:red   p0) (:red   p1))
                    (channel-op (:green p0) (:green p1))
                    (channel-op (:blue  p0) (:blue  p1))
                    (:alpha p0)))
    ([r0 g0 b0 a0 r1 g1 b1 a1]
      [(channel-op r0 r1)
      (channel-op g0 g1)
      (channel-op b0 b1)
      a0])))

(defn- image-op-generator [channel-op]
  (fn
    [bi0 bi1]
    (:image (map-image (pixel-op-generator channel-op) (create-image bi0) (create-image bi1)))))

(def plus
  (image-op-generator plus-channel-op))

(def minus
  (image-op-generator minus-channel-op))

(def and
  (image-op-generator and-channel-op))

(def or
  (image-op-generator or-channel-op))

(def xor
  (image-op-generator xor-channel-op))

(def min
  (image-op-generator min-channel-op))

(def max
  (image-op-generator max-channel-op))

(def mod
  (image-op-generator mod-channel-op))


(defprotocol Add (+ [this color]))
(defprotocol Subtract (- [this color]))

(extend BufferedImage Add {:+ (fn [this bi] (plus this bi))})
(extend Double Add {:+ (fn [this color] (clojure.core/+ this color))})
(extend Long Add {:+ (fn [this color] (clojure.core/+ this color))})
(extend Integer Add {:+ (fn [this color] (clojure.core/+ this color))})

(extend BufferedImage Subtract {:- (fn [this bi] (minus this bi))})
(extend Double Subtract {:- (fn [this color] (clojure.core/- this color))})
(extend Long Subtract {:- (fn [this color] (clojure.core/- this color))})
(extend Integer Subtract {:- (fn [this color] (clojure.core/- this color))})