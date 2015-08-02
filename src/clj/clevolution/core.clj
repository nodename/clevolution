(ns clevolution.core
  (:require [clevolution.util :refer :all]
            [clevolution.context :refer :all]
            [clevolution.file-io :refer :all]
            [clevolution.cliskstring :refer [random-clisk-expression]]
            [clevolution.legacy :refer [generate-expression]]
            [clevolution.cliskenv :refer [make-clisk-image]]
            [clevolution.view.view :refer [show frame]] :reload-all))


(def default-depth 5)
(def default-method :full)
(def default-input-files [])


;; sample usage to override a default: (random-clisk-string :depth 8)
(defn random-clisk-string
  [& {:keys [depth method input-files]
      :or {depth default-depth
           method default-method
           input-files default-input-files}}]
  (let [expr (random-clisk-expression depth method input-files)]
    (println expr)
    (with-out-str (print expr))))


(defmulti clisk-eval (fn [x _ _] (class x)))

(defmethod clisk-eval :default
  [form w h]
   (let [orig-ns *ns*]
     (in-ns 'clevolution.cliskenv)
     (try
       (make-clisk-image
         form w h)
       (catch Exception e
         (.printStackTrace e)
         (make-clisk-image 0.0 w h))
       (finally (in-ns (ns-name orig-ns))))))

(defmethod clisk-eval String
  ([^String generator w h]
   (clisk-eval (read-string generator) w h)))


(defn show-clisk-image
  "Generate an image from a generator string and show it in a JFrame"
  [^String generator]
  (try
    (let [eval-it (fn [generator] (clisk-eval generator 512 512))
          show-it (fn [frame] (show frame :generator generator :title "Clevolution"))]
      (-> generator
          eval-it
          frame
          show-it))
    (catch Exception e
      (.printStackTrace e))))


(defn save-clisk-image
  "Generate an image from generator string and save it as a file"
  ([^String generator ^String uri]
   (save-clisk-image generator 512 512 uri))
  ([^String generator w h ^String uri]
   (let [context-name "clisk"
         _ (dbg generator)
         metadata (make-generator-metadata generator context-name)
         image (clisk-eval generator w h)]
     (write-image-to-file image metadata uri))))


(defn uri-for-index
  [file-path index]
  (str file-path (format "%04d" index) ".png"))


(defn make-random-clisk-file
  [output-file-path index]
  (let [output-uri (uri-for-index output-file-path index)]
    (save-clisk-image (random-clisk-string) output-uri)))


(defn get-generator-string
  [source]
  (get-chunk-data source generator-chunk-name))


(defn resize-file
  "Same image, stretched or compressed to fit new dimensions"
  [in-uri w h out-uri]
  (let [gstring (get-generator-string in-uri)]
    (save-clisk-image gstring w h out-uri)))


(defn zoom-file
  "Zoom in: factor > 1; zoom out: factor < 1; works from top left, not center"
  ([in-uri factor out-uri]
   (zoom-file in-uri factor (get-width in-uri) (get-height in-uri) out-uri))
  ([in-uri factor w h out-uri]
   (let [gstring (get-generator-string in-uri)
         nstring (str "(scale " factor " " gstring ")")]
     (save-clisk-image nstring w h out-uri))))





(comment
  ;; LEGACY VERSION code examples are commented out but still work for now, but SLOWLY:

  (load-file "src/clevolution/core.clj")
  (require ['clevolution.core :refer :all])

  (def output-file "images/test.png")

  ;;(def input-files ["images/Dawn_on_Callipygea.png" "images/galois.png"])

  (def default-depth 2)

  ;; generate a random expression:
  ;; (generate-expression default-depth ((contexts :version0-1-1) :ops))
  ;; OR:
  ;; (generate-expression depth ((contexts :version0-1-1) :ops) input-files)
  (random-clisk-string)

  ;; generate a random expression and evaluate it, saving the resulting image to a file:
  ;; (generate-random-image-file output-file depth "version0-1-1")
  ;; OR:
  ;; (generate-random-image-file output-file depth "version0-1-1" input-files)
  (save-clisk-image (random-clisk-string) output-file)

  ;; evaluate an explicit expression, saving the resulting image to a file
  ;; (This one is a Galois field (http://nklein.com/2012/05/visualizing-galois-fields/):
  ;; (save-image "(xor (X) (Y))" "version0-1-1" output-file)
  (save-clisk-image "(vxor x y)" output-file)

  ;; generate 1000 random expressions, saving each with its image to a file:
  (def output-file-path "F:\\clisk-images\\")
  (dotimes [n 1000]
    (make-random-clisk-file output-file-path n))

  ;; read back the expression that generated the image in a file:
  (get-generator-string output-file)

  ;; re-evaluate an image's generator expression at a given width and height, and save it to another file:
  (resize-file output-file 800 800 big-output-file)
  )
