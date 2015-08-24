(ns clevolution.cliskenv
  (:require [mikera.cljutils.namespace :as n]
            [clevolution.file-input :refer [read-image-from-file]]))


(n/pull-all clisk.core)
(n/pull-all clisk.node)
(n/pull-all clisk.functions)
(n/pull-all clisk.patterns)
(n/pull-all clisk.colours)
(n/pull-all clisk.textures)
(n/pull-all clisk.effects)


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


(defn ev-perlin-noise
  [seed]
  (seed-perlin-noise! seed)
  '(clisk.noise.Perlin/noise x y z t))

(defn ev-perlin-snoise
  [seed]
  (seed-perlin-noise! seed)
  '(clisk.noise.Perlin/snoise x y z t))

(defn ev-simplex-noise
  [seed]
  (seed-simplex-noise! seed)
  '(clisk.noise.Simplex/noise x y z t))

(defn ev-simplex-snoise
  [seed]
  (seed-simplex-noise! seed)
  '(clisk.noise.Simplex/snoise x y z t))

(def ev-noise ev-simplex-noise)
(def ev-snoise ev-simplex-snoise)

(defn ev-vnoise
  [seed]
  (seed-simplex-noise! seed)
  (vnoise))

(defn ev-vsnoise
  [seed]
  (seed-simplex-noise! seed)
  (vsnoise))

(defn ev-plasma
  [seed]
  (seed-simplex-noise! seed)
  (plasma))

(defn ev-splasma
  [seed]
  (seed-simplex-noise! seed)
  (splasma))

(defn ev-turbulence
  [seed]
  (seed-simplex-noise! seed)
  (turbulence))

(defn ev-vturbulence
  [seed]
  (seed-simplex-noise! seed)
  (vturbulence))

(defn ev-vplasma
  [seed]
  (seed-simplex-noise! seed)
  (vplasma))

(defn ev-vsplasma
  [seed]
  (seed-simplex-noise! seed)
  (vsplasma))

(defn ev-turbulate
  [seed factor func]
  (seed-simplex-noise! seed)
  (turbulate factor func))

(defn ev-psychedelic
  [seed src noise-scale noise-bands]
  (seed-simplex-noise! seed)
  (psychedelic src :noise-scale noise-scale :noise-bands noise-bands))



(defn read-file
  [uri]
  (texture-map (read-image-from-file uri)))
