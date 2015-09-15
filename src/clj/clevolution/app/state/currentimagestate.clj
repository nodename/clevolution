(ns clevolution.app.state.currentimagestate
  (:require [clevolution.imagedata :refer [DEFAULT-VIEWPORT ORIGIN-VIEWPORT
                                           merge-view-elements
                                           set-image-in-image-data]]))


(defonce current-image-state (atom {:image-size          512
                                    :command             nil
                                    :generator           nil
                                    :image               nil
                                    :image-status        :ok
                                    :error-message       nil
                                    :context             nil
                                    :viewport            nil
                                    :z                   0.0}))


(defn initialize-state!
  [generator image context]
  (swap! current-image-state assoc
         :command "Initial State"
         :generator generator
         :image image
         :image-status :ok
         :error-message nil
         :context context
         :viewport ORIGIN-VIEWPORT))


(defn set-imagesize!
  [size command]
  (let [old-image-size (:image-size @current-image-state)]
    (when (not= size old-image-size)
      (swap! current-image-state assoc
             :command command
             :image-size size
             :image-status :dirty))))


(defn set-viewport!
  [viewport command]
  (let [old-viewport (:viewport @current-image-state)]
    (when (not= viewport old-viewport)
      (swap! current-image-state assoc
             :command command
             :viewport viewport
             :image-status :dirty))))


(defn set-generator!
  [generator command]
  (let [old-generator (:generator @current-image-state)]
    (when (not= generator old-generator)
      (swap! current-image-state assoc
             :command command
             :generator generator
             :image-status :dirty))))


(defn set-z!
  [z command]
  (let [old-z (:z @current-image-state)]
    (when (not= z old-z)
      (swap! current-image-state assoc
             :command command
             :z z
             :image-status :dirty))))


(defn set-loaded-data!
  [generator command]
  (println "set-loaded-data!")
  (swap! current-image-state assoc
         :generator generator
         :viewport (if (= command "Load File") DEFAULT-VIEWPORT ORIGIN-VIEWPORT)
         :z 0
         :command command
         :image-status :dirty))


(defn set-image!
  "Update the :image in current-image-state, but not if current-image-state's image data has changed.
  (Called asynchronously)"
  [image target-state status & [error-message]]
  (when (and image
             (= (merge-view-elements target-state) (merge-view-elements @current-image-state)))
    (reset! current-image-state
            (set-image-in-image-data image @current-image-state status error-message))))

