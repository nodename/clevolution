(ns clevolution.app.controlpanel
  (:require [clevolution.app.appstate :refer :all]
            [seesaw.core :refer :all]
            [seesaw.bind :as b]))


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

#_
(defn recenter
  "Move the origin from the default position (top left) to the center of the image"
  [generator]
  (str "(offset [-0.5 -0.5 0.0] " generator ")"))

#_
(def recenter-panel
  (horizontal-panel
    :items [(label :text "Recenter")
            (button :text "Apply"
                    :listen [:action (fn [e]
                                       (let [generator (:generator @app-state)
                                             new-generator (recenter generator)]
                                         (set-generator! new-generator)))])]))



;; VIEWPORT

(def viewport-grid
  (grid-panel
    :columns 2
    :items ["From x" (spinner :id :ax :model (spinner-model 0.0 :by 0.5))
            "y" (spinner :id :ay :model (spinner-model 0.0 :by 0.5))
            "To x" (spinner :id :bx :model (spinner-model 1.0 :by 0.5))
            "y" (spinner :id :by :model (spinner-model 1.0 :by 0.5))]))

;; TODO 2-way binding to app-state
;; TODO "Merge to generator" button

(def viewport-panel
  (vertical-panel
    :items [viewport-grid
            (button :text "Go"
                    :listen [:action (fn [e]
                                       (set-viewport! (value viewport-grid)))])]))



(def control-panel
  (vertical-panel
    :items [tiling-panel
            antialias-panel
            #_recenter-panel
            viewport-panel]))


(defn toy
  []
  (invoke-later
    (-> (frame :title "Hello"
               :content control-panel
               :on-close :nothing)
        pack!
        show!)))