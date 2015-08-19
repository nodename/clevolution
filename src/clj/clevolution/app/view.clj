(ns clevolution.app.view
  (:use [mikera.cljutils.error])
  (:require
    [clojure.pprint :refer [pprint]]
    [clisk.core :as clisk]
    [clisk.functions] ;; include for Test purposes
    [mikera.image.core :as img]
    [mikera.image.colours :as col]
    [clevolution.file-output :refer :all]
    [clevolution.cliskeval :refer :all]
    [clevolution.app.appstate :refer :all]
    [clevolution.app.timetravel :refer [forget-everything!]]
    [clevolution.app.controlpanel :refer [control-panel]]
    [seesaw.core :as seesaw]
    [seesaw.widget-options :refer [widget-option-provider]])
  (:import [java.awt FileDialog Dimension Color]
           [java.awt.image BufferedImage]
           [mikera.gui JIcon BufferedImageIcon]
           [java.awt.event WindowListener]
           [javax.swing JFrame JMenu JMenuBar JPanel]))


;; Java interop code in this namespace proudly stolen from Mike Anderson:
;; https://github.com/mikera/singa-viz/blob/develop/src/main/clojure/mikera/singaviz/main.clj


;; Make JIcon play nice with seesaw:
(widget-option-provider mikera.gui.JIcon seesaw/default-options)


(def FRAMESIZE 800)


(defn frame
  [^BufferedImage bi]
  (let [factor (/ FRAMESIZE (.getWidth bi))]
    (img/zoom bi factor)))



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
    (.setMinimumSize (Dimension. 800 800))
    (.setMaximumSize(Dimension. 800 800))
      #_(Dimension. (.getWidth image nil)
                                 (.getHeight image nil))))


(defn clisk-image
  "Make clisk/image work in the AWT-EventQueue thread"
  [node]
  #_{:pre [(= (class node) clisk.node.Node)]}
  (try
    (eval `(clisk/image ~node :size 512))
    (catch Exception e
      (println "view.clisk-image: ERROR," (.getMessage e))
      nil)))


(defn load-image
  [image]
  (let [image-component (make-component (frame image))]
    (seesaw/config! (:panel @app-state)
                    :items [image-component control-panel])))


(defn set-image-from-node
  [node]
  (let [image (clisk-image node)]
    (if image
      (load-image image)
      (println "clisk-image-from-node: image was nil"))
    (set-image! image)))





(defn redo-image
  [viewport generator]
  (let [view-generator (if (= viewport DEFAULT-VIEWPORT)
                         generator
                         (merge-viewport viewport generator))
        node (clisk-eval view-generator)]
    (set-image-from-node node)))


(defn load-file-dialog
  [frame]
  (let [file-dialog (doto (FileDialog. frame
                                       "Load Image..."
                                       FileDialog/LOAD)
                      (.setFile "*.png")
                      (.setVisible true))]
    (when-let [file-name (.getFile file-dialog)]
      (let [file-path (str (.getDirectory file-dialog) file-name)
            generator (get-generator file-path)]
        (set-generator! generator)))))


(defn save-file-dialog
  [frame]
  (let [file-dialog (doto (FileDialog. frame
                                       "Save Image As..."
                                       FileDialog/SAVE)
                      (.setFile "*.png")
                      (.setVisible true))]
    (when-let [file-name (.getFile file-dialog)]
      (write-image-to-file (:image @app-state)
                           (make-generator-metadata (:generator @app-state) (:context @app-state))
                           (str (.getDirectory file-dialog) file-name)))))


(defn create-image-frame
  [image generator context title]
  (forget-everything!)
  (let [frame (doto (create-frame title)
                (.setMinimumSize (Dimension. (+ 20 800 #_(.getWidth control-panel nil))
                                             (+ 100 800))))

        image-component (make-component image)

        panel (seesaw/horizontal-panel
                :background (Color. 224 224 224)
                :items [image-component control-panel])]

    (.add frame panel)
    (initialize-state! generator image context panel)

    (let [load-menuitem (seesaw/menu-item :text "Load..."
                                          :listen [:action (fn [_] (load-file-dialog frame))])
          save-menuitem (seesaw/menu-item :text "Save As..."
                                          :listen [:action (fn [_] (save-file-dialog frame))])

          file-menu (doto (JMenu. "File")
                      (.add load-menuitem)
                      (.add save-menuitem))

          fullscreen-menuitem (seesaw/menu-item :text "Toggle Full Screen"
                                                :listen [:action (fn [_] (seesaw/toggle-full-screen! frame))])

          view-menu (doto (JMenu. "View")
                      (.add fullscreen-menuitem))

          menu-bar (doto (JMenuBar.)
                     (.add file-menu)
                     (.add view-menu))]

      (doto frame
        (.setJMenuBar menu-bar)
        (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE)
        (.pack)))))


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



(add-watch app-state :generator-watch (fn [k r old-state new-state]
                                        (if (:image-dirty new-state)
                                          (do
                                            (println "Redoing image")
                                            (redo-image (:viewport new-state) (:generator new-state)))
                                          (load-image (:image new-state)))))