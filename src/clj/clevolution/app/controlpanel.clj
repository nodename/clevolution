(ns clevolution.app.controlpanel
  (:require [clevolution.app.appstate :refer :all]
            [clevolution.app.timetravel :refer [do-undo do-redo]]
            [seesaw.core :refer :all]
            [seesaw.mig :refer [mig-panel]]
            [seesaw.border :refer [custom-border compound-border line-border]]
            [seesaw.bind :as b])
  (:import [java.awt Color]
           [javax.swing.border TitledBorder]))


(defn rounded-border
  [& [color]]
  (let [color (or color Color/BLACK)]
    (custom-border
      :insets 10
      :paint (fn [c g x y w h]
               (doto g
                 (.setColor color)
                 (.drawRoundRect (+ 5 x) (+ 5 y) (- w 10) (- h 10) 15 15))))))

(defn titled-border
  [title & {:keys [color]
            :or {color Color/BLACK}}]
  (TitledBorder. (rounded-border color) title
                 TitledBorder/LEADING TitledBorder/TOP nil color))


;; TILING

(defn seamless-tile
  [scale generator]
  (str "(seamless " scale " " generator ")"))


(def seamless-scale
  (doto (slider :min 0
                :max 100
                :value 100)
    (.setMajorTickSpacing 10)
    (.setPaintTicks true)
    (.setPaintLabels true)))


(def tiling-panel
  (vertical-panel
    :border (titled-border "Seamless Tiling")
    :items [(horizontal-panel
              :items [seamless-scale
                      (button :text "Apply"
                              :listen [:action (fn [_]
                                                 (set-generator! (seamless-tile
                                                                   (/ (value seamless-scale) 100)
                                                                   (:generator @app-state))))])])]))



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
  (horizontal-panel
    :border (titled-border "Antialias")
    :items [antialias-control
            (button :text "Apply"
                    :listen [:action (fn [e] (println "GO!"))])]))



;; ZOOM


#_
    (defn zoom-center
      "Zoom in or out from center of the image,
      rather than the default top left corner.
      factor < 1: zoom out; factor > 1: zoom in"
      [factor generator]
      (let [zoom-from-origin (fn [generator] (str "(scale " factor " " generator ")"))
            offset (/ (- factor 1.0) 2)
            restore-center (fn [generator] (str "(offset [" offset " " offset " 0.0] " generator ")"))]
        (-> generator
            zoom-from-origin
            restore-center)))

(defn zoom-viewport
  [factor [[ax ay] [bx by]]]
  (let [center-x (/ (+ ax bx) 2)
        center-y (/ (+ ay by) 2)
        half-width (* factor (/ (- bx ax) 2))
        half-height (* factor (/ (- by ay) 2))]
    [[(- center-x half-width) (- center-y half-height)]
     [(+ center-x half-width) (+ center-y half-height)]]))


(def zoom-spinner
  (spinner :model (spinner-model 1.0 :from 0.0 :by 1.0)))

(def zoom-panel
  (horizontal-panel
    :border (rounded-border)
    :items [(button :text "Zoom In"
                    :listen [:action (fn [_] (set-viewport! (zoom-viewport (/ 1 (value zoom-spinner))
                                                                           (:viewport @app-state))))])
            "Factor:"
            zoom-spinner
            (button :text "Zoom Out"
                    :listen [:action (fn [_] (set-viewport! (zoom-viewport (value zoom-spinner)
                                                                           (:viewport @app-state))))])]))




;; VIEWPORT

(def viewport-grid
  (grid-panel
    :columns 5
    :items ["From:" "x" (spinner :id :ax :model (spinner-model 0.0 :by 0.5))
            "y" (spinner :id :ay :model (spinner-model 0.0 :by 0.5))
            "To:" "x" (spinner :id :bx :model (spinner-model 1.0 :by 0.5))
            "y" (spinner :id :by :model (spinner-model 1.0 :by 0.5))]))

;; update viewport-grid when :viewport in app-state changes:
(b/bind
  app-state
  (b/transform (fn [a] (let [[[ax ay] [bx by]] (:viewport a)]
                         {:ax ax :ay ay :bx bx :by by})))
  (b/tee
    (b/bind
      (b/transform (fn [m] (spinner-model (:ax m) :by 0.5)))
      (b/property (select viewport-grid [:#ax]) :model))
    (b/bind
      (b/transform (fn [m] (spinner-model (:ay m) :by 0.5)))
      (b/property (select viewport-grid [:#ay]) :model))
    (b/bind
      (b/transform (fn [m] (spinner-model (:bx m) :by 0.5)))
      (b/property (select viewport-grid [:#bx]) :model))
    (b/bind
      (b/transform (fn [m] (spinner-model (:by m) :by 0.5)))
      (b/property (select viewport-grid [:#by]) :model))))


(def vp-set-values
  (horizontal-panel
    :border (rounded-border)
    :items [viewport-grid
            (button :text "Set values"
                    :listen [:action (fn [_]
                                       (let [{:keys [ax ay bx by]} (value viewport-grid)]
                                         (set-viewport! [[ax ay] [bx by]])))])]))


(def viewport-buttons
  (horizontal-panel :items [(button :text "Default"
                                    :listen [:action (fn [_]
                                                       (set-viewport! DEFAULT-VIEWPORT))])
                            (button :text "Center at Origin"
                                    :listen [:action (fn [_]
                                                       (set-viewport! ORIGIN-VIEWPORT))])]))


(def viewport-panel
  (vertical-panel
    :border (titled-border "Viewport")
    :items [viewport-buttons
            zoom-panel
            vp-set-values
            (button :text "Apply viewport to image"
                    :listen [:action (fn [_]
                                       (merge-viewport!))])]))




;; EXPRESSION EDITOR

(def editor (editor-pane
              :background Color/BLACK
              :foreground Color/WHITE))

(b/bind app-state
        (b/transform #(:generator %))
        (b/property editor :text))


(def expression-panel
  (vertical-panel
    :border (compound-border (titled-border "Expression" :color Color/WHITE)
                             (line-border :color Color/WHITE :thickness 1))
    :background Color/BLACK
    :items [editor
            (button :text "Apply"
                    :listen [:action (fn [_] (set-generator! (text editor)))])]))




;; UNDO/REDO

(def nav-buttons
  (horizontal-panel
    :background Color/LIGHT_GRAY
    :items [(button :text "Undo"
                    :listen [:action (fn [_] (do-undo))])
            (button :text "Redo"
                    :listen [:action (fn [_] (do-redo))])]))



(def control-panel
  (vertical-panel
    :background Color/LIGHT_GRAY
    :items [#_antialias-panel
            viewport-panel
            tiling-panel
            expression-panel
            nav-buttons]))




#_
    (defn toy
      []
      (invoke-later
        (-> (frame :title "Hello"
                   :content control-panel
                   :on-close :nothing)
            pack!
            show!)))