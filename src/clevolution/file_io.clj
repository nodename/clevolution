(ns clevolution.file-io
	(:import (java.lang String)
          (javax.imageio ImageIO IIOImage)
          (javax.imageio.stream FileImageOutputStream)
          (com.sun.imageio.plugins.png PNGMetadata)
          (java.io File)))

;; See the following document for requirements
;; for upper- and lower-case letters in the four-letter chunk name:
;; http://en.wikipedia.org/wiki/Portable_Network_Graphics#.22Chunks.22_within_the_file
(def generator-chunk-name "gnTr")
(def clevolution-version-chunk-name "clVn")
(def clevolution-version "0.1.1")
(def first-named-version "0.1.1")

(defn get-png-imagewriter
	"Return an ImageWriter for PNG images"
	[]
	(let [iterator (ImageIO/getImageWritersBySuffix "png")]
	(if-not (.hasNext iterator) 
		(throw (Exception. "No image writer for PNG")))
	(.next iterator)))


(defn make-generator-metadata
	"Create a PNGMetadata containing generator-string and clevolution-version"
	[generator-string]
	(let [png-metadata (PNGMetadata.)]
	(.add (.unknownChunkType png-metadata) generator-chunk-name)
	(.add (.unknownChunkData png-metadata) (.getBytes generator-string))
	(.add (.unknownChunkType png-metadata) clevolution-version-chunk-name)
	(.add (.unknownChunkData png-metadata) (.getBytes clevolution-version))
	png-metadata))


(defn write-image-to-file
	[image metadata uri]
	(let [iio-image (IIOImage. image nil metadata)
	     imagewriter (get-png-imagewriter)
	     output (FileImageOutputStream. (File. uri))]
	(.setOutput imagewriter output)
	(.write imagewriter nil iio-image nil)
	(.flush output)
	(.close output)
	(.dispose imagewriter)))

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

(defmulti get-header-string (fn [source _] (class source)))

(defmethod get-header-string PNGMetadata
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

(defmethod get-header-string String
  [uri chunk-name]
  (let [input-stream (ImageIO/createImageInputStream (File. uri))
        imagereader (get-imagereader input-stream)
        _ (.setInput imagereader input-stream true)
        image-index 0
        input-metadata (.getImageMetadata imagereader image-index)
        _ (.close input-stream)
        _ (.dispose imagereader)]
    (get-header-string input-metadata chunk-name)))


(defn get-generator-string
  [source]
  (get-header-string source generator-chunk-name))

(defn get-clevolution-version
  [source]
  (let [version (get-header-string source clevolution-version-chunk-name)]
    (if (= "" version)
      first-named-version
      version)))

  
(defn save-image
	"Generate and save an image from generator"
	[generator uri]
	(let [metadata (make-generator-metadata (str generator))
	      image (eval generator)]
   (write-image-to-file image metadata uri)))


