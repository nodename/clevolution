(ns clevolution.app.widgets.border
  (:require [seesaw.border :refer [custom-border]])
  (:import (java.awt Color)
           (javax.swing.border TitledBorder)))


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
