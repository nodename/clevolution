(ns clevolution.app.appstate)

(defonce DEFAULT-VIEWPORT [[0.0 0.0] [1.0 1.0]])
(defonce ORIGIN-VIEWPORT [[-1.0 -1.0] [1.0 1.0]])


(def app-state (atom {:panel nil
                      :generator nil
                      :image nil
                      :image-dirty false
                      :context nil
                      :viewport nil}))

(defn initialize-state!
  [generator image context panel]
  (swap! app-state assoc
         :generator generator
         :image image
         :image-dirty false
         :context context
         :panel panel
         :viewport DEFAULT-VIEWPORT))


(defn set-viewport!
  [viewport]
  (let [old-viewport (:viewport @app-state)]
    (when (not= viewport old-viewport)
      (swap! app-state assoc
             :viewport viewport
             :image-dirty true))))

(defn set-generator!
  [generator]
  (let [old-generator (:generator @app-state)]
    (when (not= generator old-generator)
      (swap! app-state assoc
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
    (str "(viewport " a " " b " " generator ")")))

(defn merge-viewport!
  []
  (when (not= (:viewport @app-state) DEFAULT-VIEWPORT)
    (swap! app-state assoc
           :generator (merge-viewport (:viewport @app-state) (:generator @app-state))
           :viewport DEFAULT-VIEWPORT)))


