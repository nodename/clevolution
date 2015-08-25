(ns clevolution.app.controlpanel
  (:require [clevolution.app.appstate :refer :all]
            [clevolution.app.timetravel :refer [do-rewind do-undo do-redo do-end app-history]]
            [clevolution.cliskstring :refer [random-clisk-string]]
            [seesaw.core :refer :all]
            [seesaw.mig :refer [mig-panel]]
            [seesaw.border :refer [custom-border compound-border line-border]]
            [seesaw.bind :as b]
            [seesaw.core :as seesaw])
  (:import [java.awt Color Dimension]
           [javax.swing.border TitledBorder]
           (javax.swing SpinnerListModel)))


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

(def imagesize-spinnermodel
  (SpinnerListModel. [128 256 512 1024 2048]))

(def imagesize-spinner (spinner
                         :model (doto imagesize-spinnermodel
                                  (.setValue 512))))

(def imagesize-panel
  (horizontal-panel
    :border (titled-border "Image Size")
    :items [imagesize-spinner
            (button
              :text "Apply"
              :listen [:action (fn [_] (let [value (value imagesize-spinner)]
                                         (set-imagesize! value
                                                         (str "Set Image Size " value))))])]))

(b/bind
  app-state
  (b/transform (fn [a] (doto imagesize-spinnermodel (.setValue (:image-size a)))))
  (b/property imagesize-spinner :model))




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





;; Z LEVEL

(def z-spinnermodel (spinner-model 0.0 :by 0.005))

(def z-spinner (spinner :model (doto z-spinnermodel
                                 (.setValue 0.0))))

(def z-panel
  (horizontal-panel
    :border (titled-border "Z Level")
    :items [z-spinner
            (button :text "Apply"
                    :listen [:action (fn [_]
                                       (let [z (value z-spinner)]
                                         (set-z! z (str "Set Z " z))))])]))

;; update z-spinner when :z in app-state changes:
(b/bind
  app-state
  (b/transform (fn [a] (doto z-spinnermodel (.setValue (:z a)))))
  (b/property z-spinner :model))




;; Z MOVIE


(def z-movie-button
  (button :text "Z Movie"
          :listen [:action (fn [_] )]))



;; ZOOM


(defn zoom-viewport
  [factor [[ax ay] [bx by]]]
  (let [center-x (/ (+ ax bx) 2)
        center-y (/ (+ ay by) 2)
        half-width (* factor (/ (- bx ax) 2))
        half-height (* factor (/ (- by ay) 2))]
    [[(- center-x half-width) (- center-y half-height)]
     [(+ center-x half-width) (+ center-y half-height)]]))


(def zoom-spinner
  (spinner :model (spinner-model 2.0 :from 1.0 :by 1.0)))

(def zoom-panel
  (grid-panel
    :columns 1
    :items [""
            (horizontal-panel
              :border (titled-border "Zoom")
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
                                                                        (str "Zoom Out " factor))))])])
            ""]))


;; TRANSLATE


(defn translate-viewport
  [amount direction [[ax ay] [bx by]]]
  (let [horizontal? (or (= direction "Left") (= direction "Right"))
        width (- bx ax)
        height (- by ay)
        amount (if (or (= direction "Up") (= direction "Left")) (- amount) amount)
        ax (if horizontal? (+ ax (* width amount)) ax)
        ay (if horizontal? ay (+ ay (* height amount)))
        bx (if horizontal? (+ bx (* width amount)) bx)
        by (if horizontal? by (+ by (* height amount)))]
    [[ax ay] [bx by]]))


(def translate-spinner
  (spinner :model (spinner-model 0 :from 0 :by 1)))

(defn translate-action
  [direction]
  (fn [_]
    (let [amount (/(value translate-spinner) 100)]
      (set-viewport! (translate-viewport amount direction (:viewport @app-state))
                     (str direction " " amount)))))

(def up-button
  (button :text "^"
          :listen [:action (translate-action "Up")]))
(def left-button
  (button :text "<"
          :listen [:action (translate-action "Left")]))
(def right-button
  (button :text ">"
          :listen [:action (translate-action "Right")]))
(def down-button
  (button :text "v"
          :listen [:action (translate-action "Down")]))

(def translate-grid
  (grid-panel
    :border (titled-border "Translate")
    :columns 3
    :items ["" up-button ""
            left-button (horizontal-panel :items [translate-spinner "%"]) right-button
            "" down-button ""]))


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
            (horizontal-panel :items
                              [zoom-panel
                               translate-grid])
            vp-set-values]))



;; TILING

(defn seamless-tile
  [scale generator]
  (str "(seamless " scale " " generator ")"))


(def seamless-scale
  (doto (slider :min 0
                :max 200
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
                                                 (merge-view-elements @app-state))
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

(defn generate!
  []
  (.setText editor (random-clisk-string :depth 2)))

(defn evaluate!
  []
  (set-generator! (text editor)
                  "Edited Generator"))

(defn generate-and-evaluate!
  [depth]
  (set-loaded-data! (random-clisk-string :depth depth) "Random Generator"))

(def depth-spinner
  (spinner :model (spinner-model 2 :from 0 :by 1)))


(def expression-panel
  (vertical-panel
    :border (compound-border (titled-border "Expression Editor" :color Color/WHITE)
                             (line-border :color Color/WHITE :thickness 1))
    :background Color/BLACK
    :items [editor
            (horizontal-panel
              :background Color/BLACK
              :items [(horizontal-panel
                        :background Color/BLACK
                        :foreground Color/WHITE
                        :border (titled-border "Generate and Evaluate" :color Color/WHITE)
                        :items [(label :foreground Color/WHITE :text "Depth:")
                                depth-spinner
                                (button :text "Go"
                                        :listen [:action (fn [_] (generate-and-evaluate!
                                                                   (value depth-spinner)))])])
                      (button :text "Evaluate"
                              :listen [:action (fn [_] (evaluate!))])])]))




;; STATUS LINE

(def status-line (label :text ""))

(def status-panel (horizontal-panel
                    :border (titled-border "Status")
                    :items [status-line]))

(b/bind app-state
        (b/tee
          (b/bind
            (b/transform (fn [a] (condp = (:image-status a)
                                   :dirty Color/BLACK
                                   :ok Color/GREEN
                                   :failed Color/RED)))
            (b/property status-line :foreground))
          (b/bind
            (b/transform (fn [a] (condp = (:image-status a)
                                   :dirty "Calculating image..."
                                   :ok "Image loaded"
                                   :failed "FAILED to calculate image")))
            (b/property status-line :text))))




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




;; CONTROL PANEL



(def control-panel
  (horizontal-panel
    :minimum-size (Dimension. 800 800)
    :maximum-size (Dimension. 800 800)
    :background Color/LIGHT_GRAY
    :items [(vertical-panel :background Color/LIGHT_GRAY
                            :items [(horizontal-panel
                                      :items [(vertical-panel
                                                :items [imagesize-panel
                                                        z-panel])
                                              viewport-panel])
                                    tiling-panel
                                    expression-panel
                                    status-panel
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