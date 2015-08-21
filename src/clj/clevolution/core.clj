(ns clevolution.core
  (:require [clisk.core :as clisk]
            [clevolution.file-output :refer :all]
            [clevolution.cliskeval :refer [clisk-eval]]
            [clevolution.cliskstring :refer [random-clisk-expression]]
            [clevolution.app.view :refer [show frame]] :reload-all))


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


(defn show-clisk-image
  "Generate an image from a generator string and show it in a JFrame"
  [^String generator & {:keys [title size]
                        :or {size 512}}]
  (try
    (let [make-image (fn [node] (clisk/image node :size size))
          show-it (fn [frame] (show frame
                                    :generator generator
                                    :title (if title
                                             title
                                             "Clevolution")))]
      (-> generator
          clisk-eval
          make-image
          frame
          show-it))
    (catch Exception e
      (.printStackTrace e))))


(defn save-clisk-image
  "Generate an image from generator string and save it as a file"
  [^String generator ^String uri & {:keys [size]
                                    :or {size 512}}]
  (try
    (let [context-name "clisk"
          metadata (make-generator-metadata generator context-name)
          node (clisk-eval generator)
          image (clisk/image node :size size)]
      (write-image-to-file image metadata uri))
    (catch Exception e
      (.printStackTrace e))))


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


(defn depth
  [form]
  (if (and (sequential? form)
           (not (vector? form))) ;; a vector is an rgb color
    (inc (apply max (map depth (rest form))))
    0))

(defn file-depth
  [uri]
  (-> uri
      (get-generator-string)
      (read-string)
      depth))