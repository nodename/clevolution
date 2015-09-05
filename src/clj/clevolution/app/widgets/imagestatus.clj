(ns clevolution.app.widgets.imagestatus
  (:require [seesaw.bind :as b]
            [seesaw.core :refer [horizontal-panel]]
            [seesaw.swingx :refer [busy-label]]
            [clevolution.app.widgets.border :refer (titled-border)]
            [clevolution.app.appstate :refer [app-state]])
  (:import (java.awt Color)))



(def status-line (busy-label :text ""
                             :busy? false))


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
            (b/property status-line :text))
          (b/bind
            (b/transform (fn [a] (condp = (:image-status a)
                                   :dirty true
                                   :ok false
                                   :failed false)))
            (b/property status-line :busy?))))


(def image-status-panel (horizontal-panel
                    :border (titled-border "Image Status")
                    :items [status-line]))
