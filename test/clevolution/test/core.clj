(ns clevolution.test.core
  (:use [clevolution.core])
  (:use [clevolution.cliskstring])
  (:use [clevolution.parser])
  (:use [clojure.test]))

#_
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


(defn parses-ok?
  "If the expression parses ok a PersistentVector is returned;
  if not, an instaparse.gll.Failure."
  [expression]
  (let [parsed (exp-parser expression)]
    (vector? parsed)))


(deftest parser
  (is (and
        (parses-ok? "(make-multi-fractal  (ev-noise 795706659898165)  :octaves\n  2  :lacunarity    2.736204502367921 :gain  0.5832703802782291  :scale  0.2915261766378404)")
        (parses-ok? "(cross3 [1.0 1.0 1.0] [1.0 1.0 1.0])"))))


#_
(deftest parsing
  (binding [*test-out* (clojure.java.io/writer "test-out/parsing.txt" :append true)]
    (dotimes [_ 1000]
      (is (parses-ok? (random-clisk-string))))))




#_
    (mikera.util.Maths/mod 0.32139875719878824
                           (mikera.util.Maths/mod
                             (clojure.core/min
                               (java.lang.Math/sqrt
                                 (clojure.core/+
                                   (clojure.core/let [v__252445__auto__ (clojure.core/double x)]
                                     (clojure.core/* v__252445__auto__ v__252445__auto__)) (clojure.core/let [v__252445__auto__ (clojure.core/double y)] (clojure.core/* v__252445__auto__ v__252445__auto__)) (clojure.core/let [v__252445__auto__ (clojure.core/double z)] (clojure.core/* v__252445__auto__ v__252445__auto__)) (clojure.core/let [v__252445__auto__ (clojure.core/double t)] (clojure.core/* v__252445__auto__ v__252445__auto__))))) (Math/sin (clojure.core/let [x-temp (clojure.core// x 0.3) y-temp (clojure.core// y 0.3) z-temp (clojure.core// z 0.3) t-temp (clojure.core// t 0.3) x x-temp y y-temp z z-temp t t-temp] (clojure.core/- 1.0 (Math/pow (clojure.core/+ (clojure.core/let [x-temp (clojure.core/* x 1.0) y-temp (clojure.core/* y 1.0) z-temp (clojure.core/* z 1.0) t-temp (clojure.core/* t 1.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.5 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 2.0) y-temp (clojure.core/* y 2.0) z-temp (clojure.core/* z 2.0) t-temp (clojure.core/* t 2.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.25 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 4.0) y-temp (clojure.core/* y 4.0) z-temp (clojure.core/* z 4.0) t-temp (clojure.core/* t 4.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.125 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 8.0) y-temp (clojure.core/* y 8.0) z-temp (clojure.core/* z 8.0) t-temp (clojure.core/* t 8.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.0625 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 16.0) y-temp (clojure.core/* y 16.0) z-temp (clojure.core/* z 16.0) t-temp (clojure.core/* t 16.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.03125 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 32.0) y-temp (clojure.core/* y 32.0) z-temp (clojure.core/* z 32.0) t-temp (clojure.core/* t 32.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.015625 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 64.0) y-temp (clojure.core/* y 64.0) z-temp (clojure.core/* z 64.0) t-temp (clojure.core/* t 64.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.0078125 (clisk.noise.Simplex/noise x y z t))) (clojure.core/let [x-temp (clojure.core/* x 128.0) y-temp (clojure.core/* y 128.0) z-temp (clojure.core/* z 128.0) t-temp (clojure.core/* t 128.0) x x-temp y y-temp z z-temp t t-temp] (clojure.core/* 0.00390625 (clisk.noise.Simplex/noise x y z t)))) 3.0))))))