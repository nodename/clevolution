(ns clevolution.app.imagefunctions
  (:require [seesaw.core :as seesaw]
            [seesaw.widget-options :refer [widget-option-provider]]
            [mikera.image.core :as img]
            [clevolution.file-input :refer [read-image-from-file]])
  (:import [mikera.gui JIcon]
           [java.awt Dimension]
           [java.awt.image BufferedImage]))


(defonce PENDING-IMAGE (read-image-from-file "resources/Pending.png"))
(defonce ERROR-IMAGE (read-image-from-file "resources/Error.png"))

;; Make JIcon play nice with seesaw:
(widget-option-provider mikera.gui.JIcon seesaw/default-options)


(defn to-display-size
  [^BufferedImage bi ^long size]
  (let [factor (/ size (.getWidth bi))]
    (img/zoom bi factor)))


(defn make-image-icon
  [image size]
  (doto (JIcon. ^BufferedImage (to-display-size image size))
    (.setMinimumSize (Dimension. size size))
    (.setMaximumSize (Dimension. size size))))