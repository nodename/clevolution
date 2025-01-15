(ns clevolution.app.currentimagetab
  (:use [mikera.cljutils.error])
  (:require
    [seesaw.core :as seesaw]
    [seesaw.widget-options :refer [widget-option-provider]]
    [seesaw.border :refer [line-border]]

    [clevolution.app.widgets.imagestatus :refer [make-image-status-panel]]
    [clevolution.file-input :refer [read-image-from-file]]
    [clevolution.file-output :refer :all]
    [clevolution.app.imagefunctions :refer [to-display-size make-image-icon]]
    [clevolution.app.state.appstate :as appstate :refer [app-state]]
    [clevolution.app.state.currentimagetimetravel :as image-timetravel]
    [clevolution.app.widgets.timetravelnav :refer [make-nav-buttons]]
    [clevolution.app.controlpanel :refer [make-control-panel]])
  (:import
    [java.awt Color]
    (javax.swing JPanel)
    (mikera.gui JIcon)))

(defn make-current-image-component
  [image]
  (let [icon (make-image-icon image
                              (:image-display-size @appstate/app-state))]
    (seesaw/border-panel :id :current-image-component
                         :background Color/LIGHT_GRAY
                         :center icon)))

(defn replace-image
  [^JPanel current-image-component image]
  (let [^JIcon icon (make-image-icon image
                                     (:image-display-size @appstate/app-state))]
    (.removeAll current-image-component)
    (.add current-image-component icon)))

(defn make-current-image-tab
  [image]
  (seesaw/left-right-split
    (seesaw/vertical-panel
      :background Color/LIGHT_GRAY
      :items [(make-current-image-component image)
              (make-image-status-panel)
              (make-nav-buttons image-timetravel/do-rewind
                                image-timetravel/do-undo
                                image-timetravel/do-redo
                                image-timetravel/do-end)])
    (make-control-panel)
    :divider-location 1/2
    :background Color/LIGHT_GRAY))