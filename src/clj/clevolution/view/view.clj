(ns clevolution.view.view
  (:use [mikera.cljutils.error])
  (:require
    [clisk.core :as clisk]
    [clisk.functions] ;; include for Test purposes
    [mikera.image.core :as img]
    [mikera.image.colours :as col]
    [clevolution.file-output :refer :all]
    [clevolution.cliskeval :refer :all]
    [clevolution.view.controlpanel :refer [control-panel]]
    [seesaw.core :as seesaw])
  (:import [java.awt FileDialog Dimension Color]
           [java.awt.image BufferedImage]
           [mikera.gui JIcon BufferedImageIcon]
           [java.awt.event WindowListener]
           [javax.swing JFrame JMenu JMenuBar]))


;; Java interop code in this namespace proudly stolen from Mike Anderson:
;; https://github.com/mikera/singa-viz/blob/develop/src/main/clojure/mikera/singaviz/main.clj


(def last-frame (atom nil))


(defn create-new-frame
  [title]
  (let [frame (doto (JFrame. title)
                (.setVisible true)
                (.pack)
                (.setDefaultCloseOperation 2))]
    (reset! last-frame frame)
    frame))


(defn reuse-frame
  [frame title]
  (.setTitle frame title)
  (.removeAll (.getContentPane frame))
  (if (.isVisible frame)
    (.validate frame)
    (.setVisible frame true))
  (.repaint frame)
  frame)


(defn create-frame
  [title]
  (if @last-frame
    (reuse-frame @last-frame title)
    (create-new-frame title)))


(defn make-component
  [image]
  (doto (JIcon. image)
    (.setMinimumSize (Dimension. (.getWidth image nil)
                                 (.getHeight image nil)))))


(defn create-image-frame
  [image generator context title]
  (let [frame (create-frame title)
        image-component (make-component image)

        menu-bar (JMenuBar.)
        menu (JMenu. "File")
        _ (.add menu-bar menu)

        test-eval-action (fn [e]
                           (println (str "Classloader is: " (deref clojure.lang.Compiler/LOADER)))
                           (let [node (clisk-eval "x")]
                             (clisk/image node :size 512)))

        load-file-action (fn [e]
                           (let [file-dialog (doto (FileDialog. frame
                                                                "Load Image..."
                                                                FileDialog/LOAD)
                                               (.setFile "*.png")
                                               (.setVisible true))]
                             (when-let [file-name (.getFile file-dialog)]
                               (let [file-path (str (.getDirectory file-dialog) file-name)
                                     generator (get-generator file-path)
                                     image (image (clisk-eval generator) :size 512)]
                                 (.setIcon image-component (BufferedImageIcon. image))))))

        save-file-action (fn [e]
                           (let [file-dialog (doto (FileDialog. frame
                                                                "Save Image As..."
                                                                FileDialog/SAVE)
                                               (.setFile "*.png")
                                               (.setVisible true))]
                             (when-let [file-name (.getFile file-dialog)]
                               (write-image-to-file image
                                                    (make-generator-metadata generator context)
                                                    (str (.getDirectory file-dialog) file-name)))))

        test-menuitem (seesaw/menu-item :text "Test"
                                        :listen [:action test-eval-action])
        load-menuitem (seesaw/menu-item :text "Load..."
                                        :listen [:action load-file-action])
        save-menuitem (seesaw/menu-item :text "Save As..."
                                        :listen [:action save-file-action])]

    (.add menu test-menuitem)
    (.add menu load-menuitem)
    (.add menu save-menuitem)

    (.setMinimumSize frame (Dimension. (+ 20 (.getWidth image nil)) (+ 100 (.getHeight image nil))))
    (let [panel (seesaw/horizontal-panel
                  :background (Color. 224 224 224)
                  :items [image-component control-panel])]
      (.add frame panel))
    (.setJMenuBar frame menu-bar)
    (.setDefaultCloseOperation frame JFrame/DISPOSE_ON_CLOSE)
    (.pack frame)

    frame))


(defn show
  "Shows a component in a new frame"
  ([component
    & {:keys [^String generator ^String title on-close]
       :as options
       :or {generator nil title nil}}]
   (let [^JFrame fr (create-image-frame component
                                        (str generator)
                                        "clisk"
                                        (str title))]
     (when on-close
       (.addWindowListener fr (proxy [WindowListener] []
                                (windowActivated [e])
                                (windowClosing [e]
                                  (on-close))
                                (windowDeactivated [e])
                                (windowDeiconified [e])
                                (windowIconified [e])
                                (windowOpened [e])
                                (windowClosed [e])))))))


(def FRAMESIZE 800)


(defn frame
  [^BufferedImage bi]
  (let [factor (/ FRAMESIZE (.getWidth bi))]
    (img/zoom bi factor)))


