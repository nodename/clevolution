(ns clevolution.view.controlpanel
  (:require [seesaw.core :refer :all]
            [seesaw.bind :as b])
  (:import (com.jhlabs.image ReduceNoiseFilter)))


(defonce app-state (atom {:app-frame nil
                          :ast nil
                          :image nil}))


(defn make-apply-button
  [control]
  (button :text "Apply"
          :listen [:action (fn [e] (println "GO!"))]))

(defn add-button
  [control]
  (horizontal-panel :items [control (make-apply-button control)]))

;; TILING

(def seamless-scale
  (doto (slider :min 0
                :max 100
                :value 100)
    (.setMajorTickSpacing 10)
    (.setPaintTicks true)
    (.setPaintLabels true)))


(def tiling-panel
  (horizontal-panel :items [seamless-scale]))



;; ANTIALIAS

(def antialias-atom (atom 2))

(def antialias-control (slider :min 0
                               :max 4
                               :value 2
                               :major-tick-spacing 1))

(b/bind antialias-control
        antialias-atom
        antialias-control)

(def antialias-panel
  (add-button antialias-control))



;; RECENTER

(def recenter-panel
  (horizontal-panel
    :items [(label :text "Recenter")
            (button :text "Apply"
                    :listen [:action (fn [e]
                                       (let [ast (:ast @app-state)
                                             new-ast (list 'offset [-0.5 -0.5] ast)]
                                         (println new-ast)))])]))




(def control-panel
  (vertical-panel
    :items [tiling-panel
            antialias-panel
            recenter-panel]))


(defn toy
  []
  (invoke-later
    (-> (frame :title "Hello"
               :content control-panel
               :on-close :nothing)
        pack!
        show!)))