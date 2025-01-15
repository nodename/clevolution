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
  (let [a (int (* ^double a 255))
        b (int (* ^double b 255))]
    (/ ^double (bit-fn a b) 255)))

(def band
  (partial bit-op bit-and))

(def bxor
  (partial bit-op bit-xor))

(def vand
  (vectorize-op 'band))

(def vxor
  (vectorize-op 'bxor))

;; Make the corresponding Clisk functions repeatable
;; by explicitly setting the appropriate seed:

(defn perlin-seed
  [seed f & args]
  (seed-perlin-noise! seed)
     (apply f args))


(defn simplex-seed
  [seed f & args]
  (seed-simplex-noise! seed)
  (apply f args))

;;;;;;;

(defn ev-perlin-noise
  [seed]
  (perlin-seed seed (fn [] '(clisk.noise.Perlin/noise x y z t))))

(defn ev-perlin-snoise
  [seed]
  (perlin-seed seed (fn [] '(clisk.noise.Perlin/snoise x y z t))))

(defn ev-simplex-noise
  [seed]
  (simplex-seed seed (fn [] '(clisk.noise.Simplex/noise x y z t))))

(defn ev-simplex-snoise
  [seed]
  (simplex-seed seed (fn [] '(clisk.noise.Simplex/snoise x y z t))))

(def ev-noise ev-simplex-noise)
(def ev-snoise ev-simplex-snoise)

(defn ev-vnoise
  [seed]
  (simplex-seed seed vnoise))

(defn ev-vsnoise
  [seed]
  (simplex-seed seed vsnoise))

(defn ev-plasma
  [seed]
  (simplex-seed seed plasma))

(defn ev-splasma
  [seed]
  (simplex-seed seed splasma))

(defn ev-turbulence
  [seed]
  (simplex-seed seed turbulence))

(defn ev-vturbulence
  [seed]
  (simplex-seed seed vturbulence))

(defn ev-vplasma
  [seed]
  (simplex-seed seed vplasma))

(defn ev-vsplasma
  [seed]
  (simplex-seed seed vsplasma))

(defn ev-turbulate
  [seed factor func]
  (simplex-seed seed turbulate factor func))

(defn ev-psychedelic
  [seed src noise-scale noise-bands]
  (simplex-seed seed psychedelic src :noise-scale noise-scale :noise-bands noise-bands))

(defn read-file
  [uri]
  (texture-map (read-image-from-file uri)))
