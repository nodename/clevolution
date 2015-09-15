(ns clevolution.app.state.appstate
  (:require [clevolution.imagedata :refer [DEFAULT-VIEWPORT ORIGIN-VIEWPORT
                                           merge-view-elements
                                           set-image-in-image-data]]))


(defonce app-state (atom {:content-panel       nil
                          :image-display-size  600
                          :num-mutations       16}))


(defn initialize-state!
  [panel]
  (swap! app-state assoc
         :content-panel panel))


