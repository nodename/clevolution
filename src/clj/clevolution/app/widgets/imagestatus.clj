(ns clevolution.app.widgets.imagestatus
  (:require [seesaw.bind :as b]
            [seesaw.core :refer [horizontal-panel popup menu-item]]
            [seesaw.swingx :refer [busy-label]]
            [clevolution.app.widgets.border :refer (titled-border)]
            [clevolution.app.state.currentimagestate :refer [current-image-state]])
  (:import (java.awt Color)))

(defn make-status-line
  []
  (let [status-line (busy-label :text ""
                                :busy? false)]
    (b/bind current-image-state
            (b/tee
              (b/bind
                (b/transform (fn [a] (condp = (:image-status a)
                                       :dirty Color/BLACK
                                       :ok Color/GREEN
                                       :failed Color/RED
                                       Color/RED)))
                (b/property status-line :foreground))
              (b/bind
                (b/transform (fn [a] (condp = (:image-status a)
                                       :dirty "Calculating image..."
                                       :ok "Image loaded"
                                       :failed "FAILED to calculate image"
                                       "NO STATUS")))
                (b/property status-line :text))
              (b/bind
                (b/transform (fn [a] (condp = (:image-status a)
                                       :dirty true
                                       :ok false
                                       :failed false
                                       false)))
                (b/property status-line :busy?))))

    status-line))

(defn make-menu-item
  []
  (let [m (menu-item :text "")]
    (b/bind current-image-state
            (b/transform (fn [a] (:error-message a)))
            (b/property m :text))

    m))

(defn make-image-status-panel
  []
  (horizontal-panel
    :border (titled-border "Image Status")
    :items [(make-status-line)]
    :popup (popup :items [(make-menu-item)])))
