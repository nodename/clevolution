(ns clevolution.app.appstate)

(defonce DEFAULT-VIEWPORT [[0.0 0.0] [1.0 1.0]])
(defonce ORIGIN-VIEWPORT [[-1.0 -1.0] [1.0 1.0]])


(def app-state (atom {:panel               nil
                      :image-display-size  800
                      :image-size          512
                      :command             nil
                      :generator           nil
                      :image               nil
                      :image-status        :ok
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
         :context context
         :panel panel
         :viewport ORIGIN-VIEWPORT))


(defn set-imagesize!
  [size command]
  (let [old-image-size (:image-size @app-state)]
    (when (not= size old-image-size)
      (swap! app-state assoc
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
  [image & {:keys [status]
            :or {status :ok}}]
  (if image
    (swap! app-state assoc
           :image image
           :image-status status)))



;; The image is a function of z, viewport, and generator.
;; When we display the image or save the file we call merge-view-elements:

(defn merge-z
  [z generator]
  (if (= z 0.0)
    generator
    (str "(offset [0.0 0.0 " (- z) "] " generator ")")))

(defn merge-viewport
  [viewport generator]
  (let [[a b] viewport]
    (if (= viewport DEFAULT-VIEWPORT)
      generator
      (str "(viewport " a " " b " " generator ")"))))

(defn merge-view-elements
  [{:keys [viewport z generator] :as state}]
  (->> generator
       (merge-z z)
       (merge-viewport viewport)))


