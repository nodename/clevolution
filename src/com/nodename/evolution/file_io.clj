(ns com.nodename.evolution.file-io
	(:import (java.lang String)
          (java.awt.image BufferedImage)
          (java.awt Color GradientPaint)
          (javax.imageio ImageIO IIOImage)
          (javax.imageio.stream FileImageOutputStream)
          (com.sun.imageio.plugins.png PNGMetadata PNGImageReader)
          (java.io ByteArrayOutputStream File)))

;; See the following document for requirements
;; for upper- and lower-case letters in the four-letter chunk name:
;; http://en.wikipedia.org/wiki/Portable_Network_Graphics#.22Chunks.22_within_the_file
(def generator-chunk-name "gnTr")

(defn get-png-imagewriter
	"Return an ImageWriter for PNG images"
	[]
	(let [iterator (ImageIO/getImageWritersBySuffix "png")]
	(if-not (.hasNext iterator) 
		(throw (Exception. "No image writer for PNG")))
	(.next iterator)))


(defn make-generator-metadata
	"Create a PNGMetadata containing generator-string in its generator header chunk"
	[generator-string]
	(let [png-metadata (PNGMetadata.)]
	(.add (.unknownChunkType png-metadata) generator-chunk-name)
	(.add (.unknownChunkData png-metadata) (.getBytes generator-string))
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

(defmulti get-generator-string class)

(defmethod get-generator-string PNGMetadata
	[png-metadata]
	(let [dataArrayList (.unknownChunkData png-metadata)
	      typeArrayList (.unknownChunkType png-metadata)]
	(loop [i 0]
		(cond
			(>= i (.size dataArrayList))
				""
			(= (.get typeArrayList i) generator-chunk-name)
				(String. (.get dataArrayList i))
			:else
				(recur (inc i))))))

(defmethod get-generator-string String
  [file-name]
  (let [input-stream (ImageIO/createImageInputStream (File. file-name))
        imagereader (get-imagereader input-stream)
        _ (.setInput imagereader input-stream true)
        image-index 0
        input-metadata (.getImageMetadata imagereader image-index)
        _ (.close input-stream)
        _ (.dispose imagereader)]
    (get-generator-string input-metadata)))

  
(defn save-image
	"Generate and save an image from generator"
	[generator uri]
	(let [metadata (make-generator-metadata (str generator))
	      image (eval generator)]
	(write-image-to-file image metadata uri)))


(defn get-imagereader
	[inputstream]
	(let [iterator (ImageIO/getImageReaders inputstream)]
	(if-not (.hasNext iterator) 
		(throw (Exception. "No image reader found for stream")))
	(.next iterator)))


