(ns clevolution.version.version0-1-1
  (:refer-clojure :exclude [* + - and or min max mod])
  (:require [clevolution.image_ops.nullary.noise :as noise]
            [clevolution.image-ops.nullary.file-input :refer [read-image-from-file]]
            [clevolution.image_ops.unary :refer [abs sin cos atan log inverse blur *]]
            [clevolution.image_ops.binary :refer [+ - and or xor min max mod]]
            [clisk.core :refer :all]
            [clisk.node :refer :all]
            [clisk.functions :refer :all]
            [clisk.patterns :refer :all]
            [clisk.colours :refer :all]
            [clevolution.util] :reload-all))

(def image-width 400)
(def image-height 400)


(defn x-gradient
  ([a b]
    (lerp (vfrac x) a b)))

(defn X
  ([]
    (X 0 1))
  ([a b]
    (img (x-gradient a b) image-width image-height)))

(defn y-gradient
  ([a b]
    (lerp (vfrac y) a b)))

(defn Y
  ([]
    (Y 0 1))
  ([a b]
    (img (y-gradient a b) image-width image-height)))


;; the rest arguments to bw-noise are for compatibility with older versions that specified w and h; we ignore those parameters now

(defn bw-noise
  [seed octaves falloff & rest]
  (noise/bw-noise seed octaves falloff image-width image-height))