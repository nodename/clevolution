(ns clevolution.core
  (:require [clisk.core :refer [image]]
            [clevolution.file-output :refer :all]
            [clevolution.cliskeval :refer [clisk-eval]]
            [clevolution.cliskstring :refer [random-clisk-expression]]
            [clevolution.view.view :refer [show frame]] :reload-all))


(def default-depth 3)
(def default-method :full)
(def default-input-files [])


(defn random-clisk-string
  [& {:keys [depth method input-files]
      :or {depth default-depth
           method default-method
           input-files default-input-files}}]
  (let [expr (random-clisk-expression depth method input-files)]
    (with-out-str (print expr))))




(defn recenter
  "Move the origin from the default position (top left) to the center of the image"
  [generator]
  (str "(offset [-0.5 -0.5 0.0] " generator ")"))


(defn zoom-center
  "Zoom in or out from center of the image,
  rather than the default top left corner.
  factor < 1: zoom out; factor > 1: zoom in"
  [factor generator]
  (let [zoom-from-origin (fn [generator] (str "(scale " factor " " generator ")"))
        offset (/ (- factor 1.0) 2)
        restore-center (fn [generator] (str "(offset [" offset " " offset " 0.0] " generator ")"))]
    (-> generator
        zoom-from-origin
        restore-center)))


(defn seamless-tile
  [scale generator]
  (str "(seamless " scale " " generator ")"))



(defn show-clisk-image
  "Generate an image from a generator string and show it in a JFrame"
  [^String generator & {:keys [center seamless zoom title]}]
  (let [generator (if center (recenter generator) generator)
        generator (if seamless (seamless-tile seamless generator) generator)
        generator (if zoom (zoom-center zoom generator) generator)]
    (try
      (let [eval-it (fn [generator] (let [node (clisk-eval generator)]
                                      (image node :size 512)))
            show-it (fn [frame] (show frame :generator generator :title (if title
                                                                          title
                                                                          "Clevolution")))]
        (-> generator
            eval-it
            frame
            show-it))
      (catch Exception e
        (.printStackTrace e)))))


(defn save-clisk-image
  "Generate an image from generator string and save it as a file"
  ([^String generator ^String uri]
   (save-clisk-image generator 512 uri))
  ([^String generator size ^String uri]
   (let [context-name "clisk"
         metadata (make-generator-metadata generator context-name)
         image (image (clisk-eval generator) :size size)]
     (write-image-to-file image metadata uri))))


(defn uri-for-index
  [file-path index]
  (str file-path (format "%04d" index) ".png"))


(defn make-random-clisk-file
  [output-file-path index & more]
  (let [output-uri (uri-for-index output-file-path index)]
    (save-clisk-image (apply random-clisk-string more) output-uri)))


(defn get-generator-string
  [source]
  (get-chunk-data source generator-chunk-name))


(defn show-clisk-file
  [uri & more]
  (apply show-clisk-image (get-generator-string uri) :title uri more))
