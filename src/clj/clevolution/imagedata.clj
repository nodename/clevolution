(ns clevolution.imagedata
  (:require [clisk.core :as clisk]
            [clevolution.app.imagefunctions :refer [ERROR-IMAGE]]
            [clevolution.file-input :refer [read-image-from-file]]
            [clevolution.cliskstring :refer [random-clisk-string]]
            [clevolution.evolve :refer [replace-random-subtree]]
            [clevolution.cliskeval :refer [clisk-eval]])
  (:import [clevolution ClassPatch]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defonce DEFAULT-VIEWPORT [[0.0 0.0] [1.0 1.0]])
(defonce ORIGIN-VIEWPORT [[-1.0 -1.0] [1.0 1.0]])


(defonce ERROR-NODE (clisk.node/node ERROR-IMAGE))


;; The image is a function of z, viewport, and generator.
;; When we display the image or save the file we call merge-view-elements:

(defn merge-z
  [^double z generator]
  (if (zero? z)
    generator
    (str "(offset [0.0 0.0 " z "] " generator ")")))

(defn merge-viewport
  [viewport generator]
  (let [[a b] viewport]
    (if (= viewport DEFAULT-VIEWPORT)
      generator
      (str "(viewport " a " " b " " generator ")"))))

(defn merge-view-elements
  [{:keys [viewport z generator] :as image-data}]
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



(defn set-image-in-image-data
  [image data status error-message]
  (assoc data
    :image image
    :image-status status
    :error-message error-message))


(defn set-image-in-image-data!
  "Update the :image, :status, and :error-message in the atom"
  [image-data-atom image image-data status error-message]
  (reset! image-data-atom
          (set-image-in-image-data image image-data status error-message)))


(defn set-image-from-node!
  [node image-data status error-message continuation]
  (try
    (continuation (clisk-image node (:image-size image-data))
              image-data
              status
              error-message)
    (catch Exception e
      (println "set-image-from-node! failed")
      (throw e))))


(defn set-failed-image!
  [image-data error-message continuation]
  (set-image-from-node! ERROR-NODE image-data :failed  error-message continuation))


(defn node-from-image-data
  [data]
  (try
    (-> data
        merge-view-elements
        clisk-eval)
    (catch Exception e
      (println "node-from-image-data failed")
      (throw e))))


(defn calc-image!
  [image-data continuation]
  (let [node (node-from-image-data image-data)]
    (set-image-from-node! node image-data :ok nil continuation)))


(defn do-calc
  "Calculate an image from the image-data.
  Send the result on to the continuation function to update the target."
  [image-data continuation]
  (try
    (calc-image! image-data continuation)
    (catch Exception e
      (println "calc-image! ERROR:" (.getMessage e))
      (println "generator:" (:generator image-data))
      (set-failed-image! image-data (.getMessage e) continuation))))



(defn make-image-data-atom
  "Create an image-data atom from the given generator"
  [generator image-size context]
  (atom {:image-size          image-size
         :command             nil
         :generator           generator
         :image               nil
         :image-status        :dirty
         :context             context
         :viewport            ORIGIN-VIEWPORT
         :z                   0.0}))


(defn make-mutation-atom
  "Returns a new image-data atom representing a mutation of the input image-data"
  [data depth]
  (let [current-generator-form (read-string (:generator data))
        new-subform (read-string (random-clisk-string :depth depth))
        new-generator-string (println-str
                               (replace-random-subtree
                                 current-generator-form
                                 new-subform))]
    (make-image-data-atom new-generator-string (:image-size data) (:context data))))









