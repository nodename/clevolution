(ns clevolution.app.controlpanel
  (:require [seesaw.core :refer [horizontal-panel vertical-panel grid-panel scrollable
                                 editor-pane popup label spinner spinner-model slider button
                                 select replace! config! value text listen action]]
            [seesaw.mig :refer [mig-panel]]
            [seesaw.border :refer [compound-border line-border]]
            [seesaw.bind :as b]
            [clevolution.app.widgets.border :refer [rounded-border titled-border]]
            [clevolution.imagedata :refer [DEFAULT-VIEWPORT ORIGIN-VIEWPORT
                                           merge-view-elements make-mutation-atom]]
            [clevolution.app.state.appstate :refer [app-state
                                              set-imagesize! set-z! set-viewport! set-generator!
                                              set-loaded-data!]]
            [clevolution.app.state.mutationsstate :refer [mutations-state]]
            [clevolution.cliskstring :refer [random-clisk-string]]
            [clevolution.evolve :refer [replace-random-subtree]])
  (:import [java.awt Color Point]
           (javax.swing SpinnerListModel JSlider JEditorPane)
           (java.awt.event MouseEvent)))



(def CONTROL-PANEL-WIDTH 750)


;; IMAGE SIZE

(defn make-imagesize-panel
  []
  (let [imagesize-spinnermodel (SpinnerListModel. [128 256 512 1024 2048])
        imagesize-spinner (spinner
                            :model (doto imagesize-spinnermodel
                                     (.setValue 512)))
        imagesize-panel (horizontal-panel
                          :border (titled-border "Image Size")
                          :items
                          [imagesize-spinner
                           (button
                             :text "Apply"
                             :listen [:action (fn [_] (let [value (value imagesize-spinner)]
                                                        (set-imagesize! value
                                                                        (str "Set Image Size " value))))])])]
    (b/bind
      app-state
      (b/transform (fn [a] (doto imagesize-spinnermodel (.setValue (:image-size a)))))
      (b/property imagesize-spinner :model))
    imagesize-panel))




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

(defn make-z-panel
  []
  (let [z-spinnermodel (spinner-model 0.0 :by 0.005)
        z-spinner (spinner :model (doto z-spinnermodel
                                    (.setValue 0.0)))
        z-panel (horizontal-panel
                  :border (titled-border "Z Level")
                  :items [z-spinner
                          (button :text "Apply"
                                  :listen [:action (fn [_]
                                                     (let [z (value z-spinner)]
                                                       (set-z! z (str "Set Z " z))))])])]

    ;; update z-spinner when :z in app-state changes:
    (b/bind
      app-state
      (b/transform (fn [a] (doto z-spinnermodel (.setValue (:z a)))))
      (b/property z-spinner :model))

    z-panel))




;; Z MOVIE


(def z-movie-button
  (button :text "Z Movie"
          :listen [:action (fn [_] )]))



;; ZOOM


(defn make-zoom-panel
  []
  (let [zoom-viewport (fn [^double factor [[^double ax ^double ay] [^double bx ^double by]]]
                        (let [center-x (/ (+ ax bx) 2)
                              center-y (/ (+ ay by) 2)
                              half-width (* factor (/ (- bx ax) 2))
                              half-height (* factor (/ (- by ay) 2))]
                          [[(- center-x half-width) (- center-y half-height)]
                           [(+ center-x half-width) (+ center-y half-height)]]))
        zoom-spinner (spinner :model (spinner-model 2.0 :from 1.0 :by 1.0))]
    (grid-panel
      :columns 1
      :items [""
              (horizontal-panel
                :border (titled-border "Zoom")
                :items [(button :text "Zoom In"
                                :listen [:action
                                         (fn [_] (let [^double factor (value zoom-spinner)]
                                                   (set-viewport! (zoom-viewport (/ 1 factor)
                                                                                 (:viewport @app-state))
                                                                  (str "Zoom In " factor))))])
                        "Factor:"
                        zoom-spinner
                        (button :text "Zoom Out"
                                :listen [:action
                                         (fn [_] (let [factor (value zoom-spinner)]
                                                   (set-viewport! (zoom-viewport factor
                                                                                 (:viewport @app-state))
                                                                  (str "Zoom Out " factor))))])])
              ""])))


;; TRANSLATE


(defn translate-viewport
  [^double amount direction [[^double ax ^double ay] [^double bx ^double by]]]
  (let [horizontal? (or (= direction "Left") (= direction "Right"))
        width (- bx ax)
        height (- by ay)
        amount (if (or (= direction "Up") (= direction "Left")) (- amount) amount)
        ax (if horizontal? (+ ax (* width amount)) ax)
        ay (if horizontal? ay (+ ay (* height amount)))
        bx (if horizontal? (+ bx (* width amount)) bx)
        by (if horizontal? by (+ by (* height amount)))]
    [[ax ay] [bx by]]))


(defn make-translate-grid
  []
  (let [translate-spinner (spinner :model (spinner-model 0 :from 0 :by 1))
        translate-action (fn [direction]
                           (fn [_] (let [amount (/ ^double (value translate-spinner) 100)]
                                     (set-viewport! (translate-viewport amount direction
                                                                        (:viewport @app-state))
                                                    (str direction " " amount)))))
        up-button (button :text "^"
                          :listen [:action (translate-action "Up")])
        left-button (button :text "<"
                            :listen [:action (translate-action "Left")])
        right-button (button :text ">"
                             :listen [:action (translate-action "Right")])
        down-button (button :text "v"
                            :listen [:action (translate-action "Down")])]
    (grid-panel
      :border (titled-border "Translate")
      ;:size (Dimension. 200 120)
      :columns 3
      :items ["" up-button ""
              left-button (horizontal-panel :items [translate-spinner "%"]) right-button
              "" down-button ""])))


;; VIEWPORT

(defn make-viewport-panel
  []
  (let [viewport-buttons (horizontal-panel
                           :items [(button :text "Default"
                                           :listen [:action (fn [_]
                                                              (set-viewport! DEFAULT-VIEWPORT
                                                                             "Set Default Viewport"))])
                                   (button :text "Center at Origin"
                                           :listen [:action (fn [_]
                                                              (set-viewport! ORIGIN-VIEWPORT
                                                                             "Set Origin Viewport"))])])
        viewport-grid (grid-panel
                        :columns 5
                        :items ["From:" "x" (spinner :id :ax :model (spinner-model 0.0 :by 0.5))
                                "y" (spinner :id :ay :model (spinner-model 0.0 :by 0.5))
                                "To:" "x" (spinner :id :bx :model (spinner-model 1.0 :by 0.5))
                                "y" (spinner :id :by :model (spinner-model 1.0 :by 0.5))])]

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

    (let [vp-button (button :text "Set values"
                            :listen [:action
                                     (fn [_]
                                       (let [{:keys [ax ay bx by]} (value viewport-grid)]
                                         (set-viewport! [[ax ay] [bx by]]
                                                        (str "Set Viewport " ax " " ay " " bx " " by))))])

          vp-set-values (horizontal-panel
                          :border (titled-border "Current Viewport")
                          :items [viewport-grid
                                  vp-button])]

      (vertical-panel
        :border (titled-border "Viewport")
        :items [viewport-buttons
                (horizontal-panel
                  :items [(make-zoom-panel)
                          (make-translate-grid)])
                vp-set-values]))))



;; TILING

(defn make-tiling-panel
  []
  (let [seamless-tile (fn
                        [scale generator]
                        (str "(seamless " scale " " generator ")"))
        seamless-scale (doto ^JSlider (slider :min 0
                                              :max 200
                                              :value 100)
                         (.setMajorTickSpacing 10)
                         (.setPaintTicks true)
                         (.setPaintLabels true))
        seamless-button (button :text "Apply"
                                :listen [:action (fn [_]
                                                   (let [^double value (value seamless-scale)]
                                                     (set-generator! (seamless-tile
                                                                       (/ value 100)
                                                                       (merge-view-elements @app-state))
                                                                     (str "Seamless Tile " value "%"))))])]
    (vertical-panel
      :size [CONTROL-PANEL-WIDTH :by 80]
      :border (titled-border "Seamless Tiling")
      :items [(horizontal-panel
                :items [seamless-scale
                        seamless-button])])))




;; EXPRESSION EDITOR

(def cut-action (action :name "Cut"))
(def copy-action (action :name "Copy"))
(def paste-action (action :name "Paste"))

(def editor-popup (popup :border (rounded-border)
                         :items [cut-action copy-action paste-action]))


(config! cut-action :handler (fn [e] (println "Cut: " e " " editor-pane)))



(defn evaluate!
  [s]
  (set-generator! s
                  "Edited Generator"))

(defn generate-and-evaluate!
  [depth]
  (set-loaded-data! (random-clisk-string :depth depth)
                    "Random Generator"))

(defn mutate!
  [depth]
  (let [current-generator-form (read-string (:generator @app-state))
        new-subform (read-string (random-clisk-string :depth depth))
        new-generator-form (replace-random-subtree
                             current-generator-form
                             new-subform)
        new-generator-string (println-str new-generator-form)]
    (set-loaded-data!
      new-generator-string
      (str "Mutate " depth))))




(defn make-mutation-atoms
  [image-data depth]
  (map (fn [_] (make-mutation-atom image-data depth))
       (range (:num-mutations @app-state))))


(defn mutations!
  [image-data depth]
  (let [mutations (make-mutation-atoms image-data depth)]
    (swap! mutations-state assoc
           :source-image-data image-data
           :mutation-atoms mutations)))



(defn make-generate-and-evaluate-panel
  []
  (let [depth-spinner (spinner :model (spinner-model 2 :from 0 :by 1))]
    (horizontal-panel
      :background Color/BLACK
      :foreground Color/WHITE
      :border (titled-border "Generate" :color Color/WHITE)
      :items [(label :foreground Color/WHITE :text "Depth:")
              depth-spinner
              (button :text "Fresh"
                      :tip "Generate a new expression and evaluate it"
                      :listen [:action (fn [_] (generate-and-evaluate!
                                                 (value depth-spinner)))])])))



(defn make-mutate-panel
  []
  (let [depth-spinner (spinner :model (spinner-model 0 :from 0 :by 1)
                               :tip "Depth of new subexpression")]
    (horizontal-panel
      :background Color/BLACK
      :foreground Color/WHITE
      :border (titled-border "Mutate" :color Color/WHITE)
      :items [(label :foreground Color/WHITE :text "Depth:")
              depth-spinner
              #_(button :text "Mutate"
                      :tip "Replace a random subexpression with a new random subexpression"
                      :listen [:action (fn [_] (mutate! (value depth-spinner)))])
              (button :text "Mutations"
                      :listen [:action (fn [_] (mutations! @app-state (value depth-spinner)))])])))



(defn make-expression-panel
  []
  (let [^JEditorPane editor (editor-pane
                              :background Color/BLACK
                              :foreground Color/WHITE
                              :caret-color Color/WHITE
                              ;; :popup editor-popup
                              )

        show-mouse-char-position (fn [^MouseEvent e]
                                   (let [point (Point. (.getX e) (.getY e))
                                         char-position (.viewToModel editor point)]
                                     (println "position=" char-position)))]
    (b/bind app-state
            (b/transform (fn [state]
                           (with-out-str (clojure.pprint/pprint (read-string (:generator state))))))
            (b/property editor :text))

    (listen editor :mouse-clicked show-mouse-char-position)

    (vertical-panel
      :size [CONTROL-PANEL-WIDTH :by 340]
      :border (compound-border (titled-border "Expression Editor" :color Color/WHITE)
                               (line-border :color Color/WHITE :thickness 1))
      :background Color/BLACK
      :items [(scrollable editor
                          :border nil
                          :hscroll :never
                          ;:preferred-size (Dimension. 600 200)
                          )
              (horizontal-panel
                :size [CONTROL-PANEL-WIDTH :by 50]
                :background Color/BLACK
                :items [(make-generate-and-evaluate-panel)
                        (make-mutate-panel)
                        (button :text "Evaluate"
                                :tip "Evaluate the current text"
                                :listen [:action (fn [_] (evaluate! (text editor)))])])])))


;; HISTORY
#_
    (defn make-history-panel
      []
      (let [history-panel
            (vertical-panel
              :background Color/LIGHT_GRAY
              :items [])]

        (add-watch app-state :history-watch (fn [k r old-state new-state]
                                              (config! history-panel
                                                       :items (mapv #(:command %) @app-history))))

        history-panel))


;; CONTROL PANEL



(defn make-control-panel
  []
  (horizontal-panel
    :id :controlpanel
    :background Color/LIGHT_GRAY
    :items [(vertical-panel :background Color/LIGHT_GRAY
                            :items [(horizontal-panel
                                      :background Color/LIGHT_GRAY
                                      :size [CONTROL-PANEL-WIDTH :by 325]
                                      :items [(vertical-panel
                                                :items [(make-imagesize-panel)
                                                        (make-z-panel)])
                                              (make-viewport-panel)])
                                    (make-tiling-panel)
                                    (make-expression-panel)])
            #_(make-history-panel)]))
