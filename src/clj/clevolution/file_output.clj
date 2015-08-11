(ns clevolution.file-output
  (:import (java.lang String)
           (java.io File)
           (javax.imageio ImageIO IIOImage)
           (javax.imageio.stream FileImageOutputStream)
           (com.sun.imageio.plugins.png PNGMetadata))
  (:require  [clevolution.cliskenv :refer :all]
             [clevolution.file-input :refer [get-imagereader]]))

;; See the following document for requirements
;; for upper- and lower-case letters in the four-letter chunk name:
;; http://en.wikipedia.org/wiki/Portable_Network_Graphics#.22Chunks.22_within_the_file
(def generator-chunk-name "gnTr")
(def context-chunk-name "ctXt")
(def default-context-name "clisk")


(defn get-png-imagewriter
  "Return an ImageWriter for PNG images"
  []
  (let [iterator (ImageIO/getImageWritersBySuffix "png")]
    (if-not (.hasNext iterator)
      (throw (Exception. "No image writer found for PNG")))
    (.next iterator)))


(defn make-generator-metadata
  "Create a PNGMetadata containing generator and context"
  [^String generator ^String context-name]
  (let [png-metadata (PNGMetadata.)]
    (.add (.unknownChunkType png-metadata) generator-chunk-name)
    (.add (.unknownChunkData png-metadata) (.getBytes generator))
    (.add (.unknownChunkType png-metadata) context-chunk-name)
    (.add (.unknownChunkData png-metadata) (.getBytes context-name))
    png-metadata))


(defn write-image-to-file
  [image metadata uri]
  (let [iio-image (IIOImage. image nil metadata)
        imagewriter (get-png-imagewriter)
        output (FileImageOutputStream. (File. uri))]
    (.setOutput imagewriter output)
    (.write imagewriter metadata iio-image nil)
    (.flush output)
    (.close output)
    (.dispose imagewriter)))


(defn get-png-metadata
  "Get the PNG metadata from a PNG file"
  [uri]
  (let [input-stream (ImageIO/createImageInputStream (File. uri))
        imagereader (get-imagereader input-stream)
        _ (.setInput imagereader input-stream true)
        image-index 0
        input-metadata (.getImageMetadata imagereader image-index)
        _ (.close input-stream)
        _ (.dispose imagereader)]
    input-metadata))


(defmulti get-width class)
(defmethod get-width PNGMetadata
  [png-metadata]
  (.IHDR_width png-metadata))
(defmethod get-width String
  [uri]
  (let [png-metadata (get-png-metadata uri)]
    (get-width png-metadata)))

(defmulti get-height class)
(defmethod get-height PNGMetadata
  [png-metadata]
  (.IHDR_width png-metadata))
(defmethod get-height String
  [uri]
  (let [png-metadata (get-png-metadata uri)]
    (get-height png-metadata)))



(defmulti get-chunk-data (fn [source _] (class source)))

(defmethod get-chunk-data PNGMetadata
  [png-metadata chunk-name]
  (let [dataArrayList (.unknownChunkData png-metadata)
        typeArrayList (.unknownChunkType png-metadata)]
    (loop [i 0]
      (cond
        (>= i (.size dataArrayList))
        ""
        (= (.get typeArrayList i) chunk-name)
        (String. (.get dataArrayList i))
        :else
        (recur (inc i))))))

(defmethod get-chunk-data String
  [uri chunk-name]
  (let [png-metadata (get-png-metadata uri)]
    (get-chunk-data png-metadata chunk-name)))


(defn get-generator
  [source]
  (get-chunk-data source generator-chunk-name))

(defn get-context
  [source]
  (let [context (get-chunk-data source context-chunk-name)]
    (if (= "" context)
      default-context-name
      context)))


(defn eval-in
  [^String generator ^String ns]
  (let [form (read-string generator)
        orig-ns *ns*]
    ;; TODO check that ns is :required!
    (in-ns (symbol ns))
    (let [ret (eval form)]
      (in-ns (ns-name orig-ns))
      ret)))

#_
(defn save-image
  "Generate and save an image from generator"
  [^String generator ^String context-name ^String uri]
  (let [metadata (make-generator-metadata generator context-name)
        context (contexts (keyword context-name))
        ns-name (context :ns)
        image (eval-in generator ns-name)]
    (write-image-to-file image metadata uri)))