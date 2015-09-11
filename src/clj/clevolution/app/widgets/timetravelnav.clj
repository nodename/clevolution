(ns clevolution.app.widgets.timetravelnav
  (:require [seesaw.core :refer [horizontal-panel button]])
  (:import [java.awt Color]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defn make-nav-buttons
  [rewind undo redo end]
  (horizontal-panel
    :id :timetravel-nav
    :background Color/LIGHT_GRAY
    :items [(button :text "<< Rewind"
                    :listen [:action (fn [_] (rewind))])
            (button :text "< Undo"
                    :listen [:action (fn [_] (undo))])
            (button :text "Redo >"
                    :listen [:action (fn [_] (redo))])
            (button :text "End >>"
                    :listen [:action (fn [_] (end))])]))

