(ns clevolution.app.appstate)

(defonce DEFAULT-VIEWPORT [[0.0 0.0] [1.0 1.0]])
(defonce ORIGIN-VIEWPORT [[-1.0 -1.0] [1.0 1.0]])


(def app-state (atom {:panel nil
                      :frame-size 800
                      :image-size 512
                      :command nil
                      :generator nil
                      :image nil
                      :image-dirty false
                      :context nil
                      :viewport nil}))

(defn initialize-state!
  [generator image context panel]
  (swap! app-state assoc
         :command "Initial State"
         :generator generator
         :image image
         :image-dirty false
         :context context
         :panel panel
         :viewport DEFAULT-VIEWPORT))


(defn set-imagesize!
  [size]
  (swap! app-state assoc :image-size size))


(defn set-viewport!
  [viewport command]
  (let [old-viewport (:viewport @app-state)]
    (when (not= viewport old-viewport)
      (swap! app-state assoc
             :command command
             :viewport viewport
             :image-dirty true))))


(defn set-generator!
  [generator command]
  (let [old-generator (:generator @app-state)]
    (when (not= generator old-generator)
      (swap! app-state assoc
             :command command
             :generator generator
             :image-dirty true))))


(defn set-image!
  [image]
  (if image
    (swap! app-state assoc
           :image image
           :image-dirty false)))


(defn merge-viewport
  [viewport generator]
  (let [[a b] viewport]
    (if (= viewport DEFAULT-VIEWPORT)
      generator
      (str "(viewport " a " " b " " generator ")"))))

(defn merge-viewport!
  []
  (when (not= (:viewport @app-state) DEFAULT-VIEWPORT)
    (swap! app-state assoc
           :command "Merge Viewport"
           :generator (merge-viewport (:viewport @app-state) (:generator @app-state))
           :viewport DEFAULT-VIEWPORT)))


