(ns clevolution.main
  (:gen-class)
  [:require [clevolution.core :refer :all]])

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defn -main
  []
  (show-clisk-image "black"))