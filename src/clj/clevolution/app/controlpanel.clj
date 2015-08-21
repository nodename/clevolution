(ns clevolution.app.controlpanel
  (:require [clevolution.app.appstate :refer :all]
            [clevolution.app.timetravel :refer [do-rewind do-undo do-redo do-end app-history]]
            [seesaw.core :refer :all]
            [seesaw.mig :refer [mig-panel]]
            [seesaw.border :refer [custom-border compound-border line-border]]
            [seesaw.bind :as b]
            [seesaw.core :as seesaw])
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





;; IMAGE SIZE

(def imagesize-field (text :columns 6))

(def imagesize-panel
  (horizontal-panel
    :border (titled-border "Image Size")
    :items [imagesize-field
            (button :text "Apply"
                    :listen [:action (fn [_] (set-imagesize! (value imagesize-field)))])]))








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
  (spinner :model (spinner-model 1.0 :from 1.0 :by 1.0)))

(def zoom-panel
  (horizontal-panel
    :border (rounded-border)
    :items [(button :text "Zoom In"
                    :listen [:action (fn [_] (let [factor (value zoom-spinner)]
                                               (set-viewport! (zoom-viewport (/ 1 factor)
                                                                             (:viewport @app-state))
                                                              (str "Zoom In " factor))))])
            "Factor:"
            zoom-spinner
            (button :text "Zoom Out"
                    :listen [:action (fn [_] (let [factor (value zoom-spinner)]
                                               (set-viewport! (zoom-viewport factor
                                                                             (:viewport @app-state))
                                                              (str "Zoom Out " factor))))])]))





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
    :border (titled-border "Current Viewport")
    :items [viewport-grid
            (button :text "Set values"
                    :listen [:action
                             (fn [_]
                               (let [{:keys [ax ay bx by]} (value viewport-grid)]
                                 (set-viewport! [[ax ay] [bx by]]
                                                (str "Set Viewport " ax " " ay " " bx " " by))))])]))


(def viewport-buttons
  (horizontal-panel :items [(button :text "Default"
                                    :listen [:action (fn [_]
                                                       (set-viewport! DEFAULT-VIEWPORT
                                                                      "Set Default Viewport"))])
                            (button :text "Center at Origin"
                                    :listen [:action (fn [_]
                                                       (set-viewport! ORIGIN-VIEWPORT
                                                                      "Set Origin Viewport"))])]))


(def viewport-panel
  (vertical-panel
    :border (titled-border "Viewport")
    :items [viewport-buttons
            zoom-panel
            vp-set-values
            (button :text "Apply viewport to image"
                    :listen [:action (fn [_] (merge-viewport!))])]))



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


(def seamless-button
  (button :text "Apply"
          :listen [:action (fn [_]
                             (let [value (value seamless-scale)]
                               (set-generator! (seamless-tile
                                                 (/ value 100)
                                                 (:generator @app-state))
                                               (str "Seamless Tile " value "%"))))]))


(def tiling-panel
  (vertical-panel
    :border (titled-border "Seamless Tiling")
    :items [(horizontal-panel
              :items [seamless-scale
                      seamless-button])]))




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
                    :listen [:action (fn [_] (set-generator! (text editor)
                                                             "Edited Generator"))])]))




;; UNDO/REDO

(def nav-buttons
  (horizontal-panel
    :background Color/LIGHT_GRAY
    :items [(button :text "<< Rewind"
                    :listen [:action (fn [_] (do-rewind))])
            (button :text "< Undo"
                    :listen [:action (fn [_] (do-undo))])
            (button :text "Redo >"
                    :listen [:action (fn [_] (do-redo))])
            (button :text "End >>"
                    :listen [:action (fn [_] (do-end))])]))



;; HISTORY

(def history-panel
  (vertical-panel
    :background Color/LIGHT_GRAY
    :items []))



(def control-panel
  (horizontal-panel
    :background Color/LIGHT_GRAY
    :items [(vertical-panel :background Color/LIGHT_GRAY
                            :items [imagesize-panel
                                    viewport-panel
                                    tiling-panel
                                    expression-panel
                                    nav-buttons])
            history-panel]))



(add-watch app-state :history-watch (fn [k r old-state new-state]
                                      (seesaw/config! history-panel
                                                      :items (mapv #(:command %) @app-history))))


#_
    (defn toy
      []
      (invoke-later
        (-> (frame :title "Hello"
                   :content control-panel
                   :on-close :nothing)
            pack!
            show!)))