(ns clevolution.state
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
  [{:keys [viewport z generator] :as state}]
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




(defn set-image-in-state
  [image state status]
  (assoc state
    :image image
    :image-status status))


(defn set-image-from-node!
  [node state status image-fn]
  (try
    (image-fn (clisk-image node (:image-size state))
              state
              status)
    (catch Exception e
      (println "set-image-from-node! failed")
      (throw e))))


(defn set-failed-image!
  [state image-fn]
  (set-image-from-node! ERROR-NODE state :failed image-fn))


(defn node-from-state
  [state]
  (try
    (-> state
        merge-view-elements
        clisk-eval)
    (catch Exception e
      (println "node-from-state failed")
      (throw e))))


(defn calc-image!
  [state image-fn]
  (let [node (node-from-state state)]
    (set-image-from-node! node state :ok image-fn)))


(defn do-calc
  [state image-fn]
  (try
    (calc-image! state image-fn)
    (catch Exception e
      (println "calc-image! ERROR:" (.getMessage e))
      (set-failed-image! state image-fn))))



(defn make-state
  "Create a state from the given generator
  and start an async calculation of its image"
  [generator image-size context]
  (let [new-state (atom {:image-size          image-size
                         :command             nil
                         :generator           generator
                         :image               nil
                         :image-status        :ok
                         :context             context
                         :viewport            DEFAULT-VIEWPORT
                         :z                   0.0})]
    (future (do-calc @new-state set-image-in-state))
    new-state))


(defn mutate-state
  "Returns a new state representing a mutation of the input state"
  [state depth]
  (let [current-generator-form (read-string (:generator state))
        new-subform (read-string (random-clisk-string :depth depth))
        new-generator-string (println-str
                               (replace-random-subtree
                                 current-generator-form
                                 new-subform))]
    (make-state new-generator-string (:image-size state) (:context state))))









