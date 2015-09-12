(ns clevolution.app.appstate
  (:require [clevolution.imagedata :refer [DEFAULT-VIEWPORT ORIGIN-VIEWPORT
                                           merge-view-elements
                                           set-image-in-image-data]]))


(defonce app-state (atom {:content-panel       nil
                          :image-display-size  600
                          :num-mutations       16

                          :image-size          512
                          :command             nil
                          :generator           nil
                          :image               nil
                          :image-status        :ok
                          :error-message       nil
                          :context             nil
                          :viewport            nil
                          :z                   0.0}))


(defn initialize-state!
  [generator image context panel]
  (swap! app-state assoc
         :command "Initial State"
         :generator generator
         :image image
         :image-status :ok
         :error-message nil
         :context context
         :content-panel panel
         :viewport ORIGIN-VIEWPORT))


(defn set-imagesize!
  [size command]
  (let [old-image-size (:image-size @app-state)]
    (when (not= size old-image-size)
      (swap! app-state assoc
             :command command
             :image-size size
             :image-status :dirty))))


(defn set-viewport!
  [viewport command]
  (let [old-viewport (:viewport @app-state)]
    (when (not= viewport old-viewport)
      (swap! app-state assoc
             :command command
             :viewport viewport
             :image-status :dirty))))


(defn set-generator!
  [generator command]
  (let [old-generator (:generator @app-state)]
    (when (not= generator old-generator)
      (swap! app-state assoc
             :command command
             :generator generator
             :image-status :dirty))))


(defn set-z!
  [z command]
  (let [old-z (:z @app-state)]
    (when (not= z old-z)
      (swap! app-state assoc
             :command command
             :z z
             :image-status :dirty))))


(defn set-loaded-data!
  [generator command]
  (swap! app-state assoc
         :generator generator
         :viewport (if (= command "Load File") DEFAULT-VIEWPORT ORIGIN-VIEWPORT)
         :z 0
         :command command
         :image-status :dirty))


(defn set-image!
  "Update the :image in app-state, but not if app-state's image data has changed.
  (Called asynchronously)"
  [image target-state status & [error-message]]
  (when (and image
             (= (merge-view-elements target-state) (merge-view-elements @app-state)))
    (reset! app-state
            (set-image-in-image-data image @app-state status error-message))))
