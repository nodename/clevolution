(ns clevolution.file-input
  (:import (java.io File)
           (javax.imageio ImageIO)))

(defn get-imagereader
  [inputstream]
  (let [iterator (ImageIO/getImageReaders inputstream)]
    (if-not (.hasNext iterator)
      (throw (Exception. "No image reader found for stream")))
    (.next iterator)))

(defn read-image-from-file
  [uri]
  (let [input-stream (ImageIO/createImageInputStream (File. uri))
        imagereader (get-imagereader input-stream)
        _ (.setInput imagereader input-stream true)
        image-index 0
        bi (.read imagereader image-index)]
    (.close input-stream)
    (.dispose imagereader)
    bi))
