(ns clevolution.test.core
  (:use [clevolution.core])
  (:use [clisk.live])
  (:use [clojure.test]))

(deftest test-one
  (is (thrown? java.lang.IllegalArgumentException
               (do
                 (in-ns 'clevolution.cliskenv)
                 (eval (read-string "(vmod (vcos blue) (offset vround vround))"))
                 (in-ns 'clevolution)))))


;; "(vround (warp vround max-component))" No matching method found: round

;; (make-multi-fractal (v+ (vmod vround vabs) (vabs max-component))) mod

;; (checker (rotate (rotate (rgb-from-hsl brown) (v+ wood perlin-noise)) (rotate (scale length blue) (rotate simplex-noise darkGray))) (dot (vdivide (dot vsqrt purple) (psychedelic blue)) (psychedelic (psychedelic agate))))
;; java.lang.RuntimeException: Method code too large!

;; (offset (v+ (v- (rotate flecks purple) (rotate vsin blue)) (v- (psychedelic vabs) (vdivide lightGray simplex-noise))) (warp (vdivide (psychedelic vsqrt) (rotate vcos agate)) (vpow (checker tile blue) (rgb-from-hsl min-component))))
;; java.util.concurrent.ExecutionException: java.lang.NullPointerException


;; (swirl (vmod (v- grain sigmoid) (psychedelic vsqrt :noise-scale 0.6832167603175606 :noise-bands 7.047759497460712)))
;; java.util.concurrent.ExecutionException: java.lang.NullPointerException




(shatter
  (tile (make-multi-fractal
          (rotate 0.9081414094051161 z)
          :octaves 1
          :lacunarity 1.0922954699658527
          :gain 0.6374550428738956
          :scale 0.7577837963811469))
  :points 20)
;; clojure.lang.ArityException: Wrong number of args (0) passed to: core/max





#_
(mikera.util.Maths/mod 0.32139875719878824
                       (mikera.util.Maths/mod
                         (clojure.core/min
                           (java.lang.Math/sqrt
                             (clojure.core/+
                               (clojure.core/let [v__252445__auto__ (clojure.core/double x)]
                                 (clojure.core/* v__252445__auto__ v__252445__auto__)) (clojure.core/let [v__252445__auto__ (clojure.core/double y)] (clojure.core/* v__252445__auto__ v__252445__auto__)) (clojure.core/let [v__252445__auto__ (clojure.core/double z)] (clojure.core/* v__252445__auto__ v__252445__auto__)) (clojure.core/let [v__252445__auto__ (clojure.core/double t)] (clojure.core/* v__252445__auto__ v__252445__auto__))))) (Math/sin (clojure.core/let [x-temp (clojure.core// x 0.3) y-temp (clojure.core// y 0.3) z-temp (clojure.core// z 0.3) t-temp (clojure.core// t 0.3) x x-temp y y-temp z z-temp t t-temp] (clojure.core/- 1.0 (Math/pow (clojure.core/+ (clojure.core/let [x-temp (clojure.core/* x 1.0) y-temp (clojure.core/* y 1.0) z-temp (clojure.core/* z 1.0) t-temp (clojure.core/* t 1.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.5 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 2.0) y-temp (clojure.core/* y 2.0) z-temp (clojure.core/* z 2.0) t-temp (clojure.core/* t 2.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.25 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 4.0) y-temp (clojure.core/* y 4.0) z-temp (clojure.core/* z 4.0) t-temp (clojure.core/* t 4.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.125 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 8.0) y-temp (clojure.core/* y 8.0) z-temp (clojure.core/* z 8.0) t-temp (clojure.core/* t 8.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.0625 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 16.0) y-temp (clojure.core/* y 16.0) z-temp (clojure.core/* z 16.0) t-temp (clojure.core/* t 16.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.03125 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 32.0) y-temp (clojure.core/* y 32.0) z-temp (clojure.core/* z 32.0) t-temp (clojure.core/* t 32.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.015625 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 64.0) y-temp (clojure.core/* y 64.0) z-temp (clojure.core/* z 64.0) t-temp (clojure.core/* t 64.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.0078125 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 128.0) y-temp (clojure.core/* y 128.0) z-temp (clojure.core/* z 128.0) t-temp (clojure.core/* t 128.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.00390625 (clisk.noise.Simplex/noise x y z t)))) 3.0))))))