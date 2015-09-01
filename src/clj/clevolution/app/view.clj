(ns clevolution.app.view
  (:use [mikera.cljutils.error])
  (:require
    [clisk.functions] ;; include for Test purposes
    [mikera.image.core :as img]
    [mikera.image.colours :as col]
    [clevolution.file-output :refer :all]
    [clevolution.imagedata :as state :refer [merge-view-elements do-calc]]
    [clevolution.app.appstate :as appstate :refer [app-state]]
    [clevolution.app.timetravel :refer [forget-everything! app-history]]
    [clevolution.app.controlpanel :refer [control-panel]]
    [seesaw.core :as seesaw]
    [seesaw.widget-options :refer [widget-option-provider]]
    [seesaw.border :refer [line-border]])
  (:import [java.awt FileDialog Dimension Color]
           [java.awt.image BufferedImage]
           [mikera.gui JIcon]
           [java.awt.event WindowListener]
           [javax.swing JFrame JMenu JMenuBar]))


;; Java interop code in this namespace proudly stolen from Mike Anderson:
;; https://github.com/mikera/singa-viz/blob/develop/src/main/clojure/mikera/singaviz/main.clj


;; Make JIcon play nice with seesaw:
(widget-option-provider mikera.gui.JIcon seesaw/default-options)






(defn to-display-size
  [^BufferedImage bi]
  (let [factor (/ (:image-display-size @app-state) (.getWidth bi))]
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
  (if-not (.isVisible frame)
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
  (let [size (:image-display-size @appstate/app-state)]
    (doto (JIcon. image)
      (.setMinimumSize (Dimension. size size))
      (.setMaximumSize (Dimension. size size)))))


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
        (.setTitle frame file-path)
        (appstate/set-loaded-data! generator "Load File")))))


(defn save-file-dialog
  [frame]
  (let [file-dialog (doto (FileDialog. frame
                                       "Save Image As..."
                                       FileDialog/SAVE)
                      (.setFile "*.png")
                      (.setVisible true))]
    (when-let [file-name (.getFile file-dialog)]
      (write-image-to-file (:image @app-state)
                           (make-generator-metadata (merge-view-elements @app-state)
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
                             (make-generator-metadata (merge-view-elements state)
                                                      (:context state))
                             (str (.getDirectory file-dialog) file-name index ".png"))))))


(defn create-app-frame
  [image generator context title]
  (forget-everything!)

  (let [frame-size (Dimension. (+ 650 (:image-display-size @app-state))
                               (+ 100 (:image-display-size @app-state)))
        frame (doto (create-frame title)
                (.setMinimumSize frame-size)
                (.setMaximumSize frame-size))

        image-component (make-component image)

        content-panel (seesaw/horizontal-panel
                        :background (Color. 224 224 224)
                        :items [image-component control-panel])]

    (.add frame content-panel)
    (appstate/initialize-state! generator image context content-panel)

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
   (let [^JFrame fr (create-app-frame component
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





(def current-calc (atom nil))

(defn cancel-current-calc
  []
  (let [calc @current-calc]
    (when calc
      (future-cancel calc))))

(defn start-calc
  [state]
  (future
    (do-calc state appstate/set-image!)))


(defn display-image
  [image]
  (let [image-component (make-component (to-display-size image))]
    (seesaw/config! (:content-panel @app-state)
                    :items [image-component control-panel])))


(add-watch app-state :generator-watch (fn [k r old-state new-state]
                                        (if (= :dirty (:image-status new-state))
                                          (do
                                            (cancel-current-calc)
                                            (reset! current-calc (start-calc new-state)))
                                          (display-image (:image new-state)))))