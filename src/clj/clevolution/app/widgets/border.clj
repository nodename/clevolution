(ns clevolution.app.widgets.border
  (:require [seesaw.border :refer [custom-border]])
  (:import (java.awt Color Graphics)
           (javax.swing.border TitledBorder)))

(defn rounded-border
  [& [color]]
  (let [^Color color (or color Color/BLACK)]
    (custom-border
      :insets 10
      :paint (fn [c ^Graphics g x y w h]
               (doto g
                 (.setColor color)
                 (.drawRoundRect (+ 5 ^double x) (+ 5 ^double y) (- ^double w 10) (- ^double h 10) 15 15))))))

(defn titled-border
  [title & {:keys [color]
            :or {color Color/BLACK}}]
  (TitledBorder. (rounded-border color) title
                 TitledBorder/LEADING TitledBorder/TOP nil color))
