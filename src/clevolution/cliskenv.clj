(ns clevolution.cliskenv
  (:import java.lang.Math)
  (:require [clevolution.util :refer :all]
            [clevolution.image-ops.nullary.file-input :refer [read-image-from-file]]
            [clisk.core :refer :all]
            [clisk.node :refer :all]
            [clisk.functions :refer :all]
            [clisk.patterns :refer :all]
            [clisk.colours :refer :all]
            [clisk.textures :refer :all]
            [clisk.effects :refer :all] :reload-all))

;; a and b are between 0 and 1
(defn bit-op
  [bit-fn a b]
  (let [a (int (* a 255))
        b (int (* b 255))]
    (/ (bit-fn a b) 255)))

(def band
  (partial bit-op bit-and))

(def bxor
  (partial bit-op bit-xor))

(def vand
  (vectorize-op 'band))

(def vxor
  (vectorize-op 'bxor))

(defn read-file
  [uri]
  (texture-map (read-image-from-file uri)))

(defn make-clisk-image
  ([form]
    (make-clisk-image form 256 256))
  ([form w h]
    (img (node (eval form)) w h)))
