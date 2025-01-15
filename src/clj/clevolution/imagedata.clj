(ns clevolution.imagedata
  (:require [clisk.core :as clisk]
            [clevolution.app.imagefunctions :refer [PENDING-IMAGE ERROR-IMAGE]]
            [clevolution.file-input :refer [read-image-from-file]]
            [clevolution.cliskstring :refer [random-clisk-string]]
            [clevolution.evolve :refer [replace-random-subtree]]
            [clevolution.cliskeval :refer [clisk-eval]])
  (:import [clevolution ClassPatch]
           (clojure.lang Var)))

(defonce DEFAULT-VIEWPORT [[0.0 0.0] [1.0 1.0]])
(defonce ORIGIN-VIEWPORT [[-1.0 -1.0] [1.0 1.0]])

(defonce ERROR-NODE (clisk.node/node ERROR-IMAGE))

;; The image is a function of z, viewport, and generator.
;; When we display the image or save the file we call merge-view-elements:

;; TODO save the viewport separately in its own PNG header field

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

(defn image-from-status
  [image-data]
  (condp = (:image-status image-data)
    :ok (:image image-data)
    :dirty PENDING-IMAGE
    :failed ERROR-IMAGE))

;; When clisk/image is called from a future,
;; the Compiler's LOADER is unbound.
;; So we set it before calling clisk/image:
(defn clisk-image
  [node size]
  (try
    (when (not-any? #{clojure.lang.DynamicClassLoader}
                    (map class (vals (Var/getThreadBindings))))
      (ClassPatch/pushClassLoader))
    (clisk/image node :size size)
    (catch Exception e
      (println "clisk-image failed")
      (throw e))))

(defn set-image-in-image-data
  [image data status error-message]
  (assoc data
    :image image
    :image-status status
    :error-message error-message))

(defn set-image-in-image-data!
  "Update the :image, :status, and :error-message in the ref"
  [image-data-ref image image-data status error-message]
  (ref-set image-data-ref
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

(defn make-image-data-ref
  "Create an image-data ref from the given generator"
  [generator image-size context]
  (ref {:image-size          image-size
        :command             nil
        :generator           generator
        :image               nil
        :image-status        :dirty
        :context             context
        :viewport            ORIGIN-VIEWPORT
        :z                   0.0}))

(defn mutate-generator-string
  [generator depth]
  (let [current-generator-form (read-string generator)
        new-subform (read-string (random-clisk-string :depth depth))]
    (println-str
      (replace-random-subtree
        current-generator-form
        new-subform))))

(defn make-mutation-ref
  "Returns a new image-data ref representing a mutation of the input image-data"
  [source-image-data depth]
  (let [new-generator-string (mutate-generator-string (:generator source-image-data) depth)]
    (make-image-data-ref new-generator-string
                          (:image-size source-image-data)
                          (:context source-image-data))))







