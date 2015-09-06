(ns clevolution.app.widgets.timetravelnav
  (:require [seesaw.core :refer [horizontal-panel button]]
            [clevolution.app.timetravel :refer [do-rewind do-undo do-redo do-end]])
  (:import [java.awt Color]))


(defn make-nav-buttons
  []
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

