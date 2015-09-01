(ns clevolution.appdata
  (:require [clisk.core :as clisk]
            [clevolution.file-input :refer [read-image-from-file]]
            [clevolution.cliskstring :refer [random-clisk-string]]
            [clevolution.evolve :refer [replace-random-subtree]]
            [clevolution.cliskeval :refer [clisk-eval]])
  (:import [clevolution ClassPatch]))


(defonce DEFAULT-VIEWPORT [[0.0 0.0] [1.0 1.0]])
(defonce ORIGIN-VIEWPORT [[-1.0 -1.0] [1.0 1.0]])

(defonce ERROR-NODE (clisk.node/node (read-image-from-file "resources/Error.png")))


;; The image is a function of z, viewport, and generator.
;; When we display the image or save the file we call merge-view-elements:

(defn merge-z
  [z generator]
  (if (zero? z)
    generator
    (str "(offset [0.0 0.0 " (- z) "] " generator ")")))

(defn merge-viewport
  [viewport generator]
  (let [[a b] viewport]
    (if (= viewport DEFAULT-VIEWPORT)
      generator
      (str "(viewport " a " " b " " generator ")"))))

(defn merge-view-elements
  [{:keys [viewport z generator] :as app-data}]
  (->> generator
       (merge-z z)
       (merge-viewport viewport)))




(defonce class-loader-undefined? (atom true))

;; When clisk/image is called from the AWT EventQueue thread,
;; the Compiler's LOADER is unbound.
;; So we set it before calling clisk/image:
(defn clisk-image
  [node size]
  (if @class-loader-undefined?
    (ClassPatch/pushClassLoader)
    (reset! class-loader-undefined? false))
  (clisk/image node :size size))




(defn set-image-in-app-data
  [image data status]
  (assoc data
    :image image
    :image-status status))


(defn set-image-from-node!
  [node app-data status image-fn]
  (try
    (image-fn (clisk-image node (:image-size app-data))
              app-data
              status)
    (catch Exception e
      (println "set-image-from-node! failed")
      (throw e))))


(defn set-failed-image!
  [app-data image-fn]
  (set-image-from-node! ERROR-NODE app-data :failed image-fn))


(defn node-from-app-data
  [data]
  (try
    (-> data
        merge-view-elements
        clisk-eval)
    (catch Exception e
      (println "node-from-app-data failed")
      (throw e))))


(defn calc-image!
  [app-data image-fn]
  (let [node (node-from-app-data app-data)]
    (set-image-from-node! node app-data :ok image-fn)))


(defn do-calc
  [app-data image-fn]
  (try
    (calc-image! app-data image-fn)
    (catch Exception e
      (println "calc-image! ERROR:" (.getMessage e))
      (set-failed-image! app-data image-fn))))



(defn make-app-data
  "Create an app-data from the given generator
  and start an async calculation of its image"
  [generator image-size context]
  (let [new-app-data (atom {:image-size          image-size
                            :command             nil
                            :generator           generator
                            :image               nil
                            :image-status        :ok
                            :context             context
                            :viewport            DEFAULT-VIEWPORT
                            :z                   0.0})]
    (future (do-calc @new-app-data set-image-in-app-data))
    new-app-data))


(defn mutate-app-data
  "Returns a new app-data representing a mutation of the input app-data"
  [data depth]
  (let [current-generator-form (read-string (:generator data))
        new-subform (read-string (random-clisk-string :depth depth))
        new-generator-string (println-str
                               (replace-random-subtree
                                 current-generator-form
                                 new-subform))]
    (make-app-data new-generator-string (:image-size data) (:context data))))









