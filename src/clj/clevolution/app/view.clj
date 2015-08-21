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
    [clevolution.app.timetravel :refer [forget-everything! app-history]]
    [clevolution.app.controlpanel :refer [control-panel]]
    [seesaw.core :as seesaw]
    [seesaw.widget-options :refer [widget-option-provider]])
  (:import [java.awt FileDialog Dimension Color]
           [java.awt.image BufferedImage]
           [mikera.gui JIcon]
           [java.awt.event WindowListener]
           [javax.swing JFrame JMenu JMenuBar]
           (clevolution ClassPatch)))


;; Java interop code in this namespace proudly stolen from Mike Anderson:
;; https://github.com/mikera/singa-viz/blob/develop/src/main/clojure/mikera/singaviz/main.clj


;; Make JIcon play nice with seesaw:
(widget-option-provider mikera.gui.JIcon seesaw/default-options)



(defn frame
  [^BufferedImage bi]
  (let [factor (/ (:frame-size @app-state) (.getWidth bi))]
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


(defonce class-loader-undefined? (atom true))

;; When clisk/image is called from the AWT EventQueue thread,
;; the Compiler's LOADER is unbound.
;; So we set it before calling clisk/image:
(defn clisk-image
  [node & {:keys [size]
           :or {size (:image-size @app-state)}}]
  (if class-loader-undefined?
    (ClassPatch/pushClassLoader)
    (reset! class-loader-undefined? false))
  (clisk/image node :size size))


(defn load-image
  [image]
  (let [image-component (make-component (frame image))]
    (seesaw/config! (:panel @app-state)
                    :items [image-component control-panel])))


(defn set-image-from-node
  [node & {:keys [size]
           :or {size (:image-size @app-state)}}]
  (let [image (clisk-image node :size size)]
    (if image
      (load-image image)
      (println "set-image-from-node: image was nil"))
    (set-image! image)))


(defn redo-image
  [viewport generator]
  (let [view-generator (merge-viewport viewport generator)
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
        (set-generator! generator "Load File")))))


(defn save-file-dialog
  [frame]
  (let [file-dialog (doto (FileDialog. frame
                                       "Save Image As..."
                                       FileDialog/SAVE)
                      (.setFile "*.png")
                      (.setVisible true))]
    (when-let [file-name (.getFile file-dialog)]
      (write-image-to-file (:image @app-state)
                           (make-generator-metadata (merge-viewport (:viewport @app-state)
                                                                    (:generator @app-state))
                                                    (:context @app-state))
                           (str (.getDirectory file-dialog) file-name)))))


(defn save-history-dialog
  [frame]
  (let [file-dialog (doto (FileDialog. frame
                                       "Save History As..."
                                       FileDialog/SAVE)
                      (.setFile "*")
                      (.setVisible true))]
    (when-let [file-name (.getFile file-dialog)]
      (doseq [[index state] (map-indexed vector @app-history)]
        (write-image-to-file (:image state)
                             (make-generator-metadata (merge-viewport (:viewport state)
                                                                      (:generator state))
                                                      (:context state))
                             (str (.getDirectory file-dialog) file-name index ".png"))))))


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

    (let [load-menuitem (seesaw/menu-item
                          :text "Load..."
                          :listen [:action (fn [_] (load-file-dialog frame))])
          save-menuitem (seesaw/menu-item
                          :text "Save As..."
                          :listen [:action (fn [_] (save-file-dialog frame))])
          save-history-menu-item (seesaw/menu-item
                                   :text "Save History As..."
                                   :listen [:action (fn [_] (save-history-dialog frame))])

          file-menu (doto (JMenu. "File")
                      (.add load-menuitem)
                      (.add save-menuitem)
                      (.add save-history-menu-item))

          fullscreen-menuitem (seesaw/menu-item
                                :text "Toggle Full Screen"
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