(ns clevolution.image_ops.nullary.noise
  (:import (java.awt.image BufferedImage) (clevolution.perlin ImprovedNoise))
  (:use clojure.contrib.math))

(defmacro dbg [& body]
  `(let [x# ~@body]
     (println (str "dbg: " (quote ~@body) "=" x#))
     x#))


(defn- next-random-number
  [random-number]
  (rem (* random-number 16807) 2147483647))

(defn- powers
  "Return a vector of length _length_ containing p-to-the-zeroth, p, p squared, etc"
    ([length]
      (powers 2 length))
    ([p length]
  (loop [result []]
    (let [i (count result)]
      (cond
        (== i length) result
        :else (recur (conj result (expt p i))))))))

(defn- noise-pixel-octave-contrib-generator [x y z persistences freqs]
  (fn [octave]
    (let [frequency (freqs octave)
          persistence (persistences octave)]
      (* persistence (ImprovedNoise/noise (* frequency x) (* frequency y) (* frequency z))))))
       
(defn- noise-pixel-sum-nontiled
  [x y {z :z
        persistences :persistences
        freqs :freqs
        octaves-count :octaves-count
        :as noise-params}]
    (let [noise-pixel-octave-contrib (noise-pixel-octave-contrib-generator x y z persistences freqs)]
      (loop [octave 0
             sum 0]
        (cond
          (== octave octaves-count) sum
          :else (recur (inc octave) (+ sum (noise-pixel-octave-contrib octave)))))))
  
;; cheap tiling
(defn- noise-pixel-sum-tiled
  [x y {z :z
        persistences :persistences
        freqs :freqs
        x-period :x-period
        y-period :y-period
        base-x :base-x
        base-y :base-y
        octaves-count :octaves-count
        :as noise-params}]
    (let [noise-pixel-octave-contrib0 (noise-pixel-octave-contrib-generator x y z persistences freqs)
          noise-pixel-octave-contrib1 (noise-pixel-octave-contrib-generator (+ x x-period) y z persistences freqs)
          noise-pixel-octave-contrib2 (noise-pixel-octave-contrib-generator x (+ y y-period) z persistences freqs)
          noise-pixel-octave-contrib3 (noise-pixel-octave-contrib-generator (+ x x-period) (+ y y-period) z persistences freqs)
          xmix (- 1.0 (/ (- x base-x) x-period))
          ymix (- 1.0 (/ (- y base-y) y-period))]
      (loop [octave 0
             sum 0]
        (cond
          (== octave octaves-count) sum
          :else (let [contrib01 ((.lerp ImprovedNoise) xmix (noise-pixel-octave-contrib0 octave) (noise-pixel-octave-contrib1 octave))
                      contrib23 ((.lerp ImprovedNoise) xmix (noise-pixel-octave-contrib2 octave) (noise-pixel-octave-contrib3 octave))]
                  (recur (inc octave) (+ sum ((.lerp ImprovedNoise) ymix contrib01 contrib23))))))))
    
(defn- noise-pixel-setter [y py {total-persistence :total-persistence
                                 tileable :tileable
                                 :or {tileable false}
                                 :as noise-params}]
  (fn [x px bi]
    (let [noise-pixel-sum (if tileable
                            noise-pixel-sum-tiled
                            noise-pixel-sum-nontiled)
          sum (noise-pixel-sum x y noise-params)
          grey-level (int (* 128 (+ 1 (/ sum total-persistence))))
          argb-color (bit-or 0xff000000 (bit-or (bit-shift-left grey-level 16) (bit-or (bit-shift-left grey-level 8) grey-level)))
          _ (.setRGB bi px py argb-color)]
      bi)))
 
(defn- noise-row-setter [{base-x :base-x
                          width :width
                          base-factor :base-factor
                         :as noise-params}]
     (fn [y py bi]
       (let [set-noise-pixel (noise-pixel-setter y py noise-params)]
         (loop [x base-x
                px 0
                image bi]
           (cond
             (== px width) image
             :else (recur (+ x base-factor) (inc px) (set-noise-pixel x px image)))))))

(defn bw-noise
	"Create a constant-z slice of 3D Perlin-noise texture. Parameters: seed; octaves-count; falloff; width, height of image; scale; x, y, z: location of upper right corner of image in noise space"
	([seed octaves-count falloff width height]
   (bw-noise seed octaves-count falloff width height 1 0 0 0))
	([seed octaves-count falloff width height scale]
   (bw-noise seed octaves-count falloff width height scale 0 0 0))
	([seed octaves-count falloff width height origin-x origin-y origin-z]
   (bw-noise seed octaves-count falloff width height 1 origin-x origin-y origin-z))
	([seed octaves-count falloff width height scale origin-x origin-y origin-z]
   (let [bi (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
       base-factor (/ scale 64)
       persistences (powers falloff octaves-count)
       x-offset (next-random-number seed)
       y-offset (next-random-number x-offset)
       z-offset (next-random-number y-offset)
       base-x (+ (* origin-x base-factor) x-offset)
       base-y (+ (* origin-y base-factor) y-offset)
       base-z (+ (* origin-z base-factor) z-offset)
       noise-params {:width width
                     :base-x base-x
                     :base-y base-y
                     :z base-z
                     :octaves-count octaves-count
                     :persistences persistences
                     :total-persistence (reduce + persistences)
                     :freqs (powers octaves-count)
                     :base-factor base-factor
                     :x-period (* width base-factor)
                     :y-period (* height base-factor)
                     :tileable false}
       set-noise-row (noise-row-setter noise-params)]
   (loop [y base-y
          py 0
          image bi]
     (cond
       (== py height) image
       :else (recur (+ y base-factor) (inc py) (set-noise-row y py image)))))))

