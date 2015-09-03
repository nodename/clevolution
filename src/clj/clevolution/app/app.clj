(ns clevolution.app.app
  (:require [clevolution.app.timetravel :refer [forget-everything! app-history]]
            [clevolution.app.appstate :as appstate :refer [app-state]]
            [clevolution.app.view :refer [make-display-panel]]
            [clevolution.app.controlpanel :refer [control-panel]]
            [clevolution.imagedata :refer [merge-view-elements]]
            [clevolution.file-output :refer [get-generator make-generator-metadata write-image-to-file]]
            [seesaw.core :as seesaw])
  (:import (java.awt FileDialog Dimension Color)
           (javax.swing JFrame JMenu JMenuBar)
           (java.awt.event WindowListener)))


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
                      (.setFile "frame")
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

  (let [frame-size (Dimension. (+ 800 (:image-display-size @app-state))
                               (+ 100 (:image-display-size @app-state)))
        frame #_(doto (create-frame title)
                (.setMinimumSize frame-size)
                (.setMaximumSize frame-size))
        (create-frame title)

        content-panel (seesaw/left-right-split
                        (make-display-panel image)
                        control-panel
                        :divider-location 1/2
                        :background (Color. 224 224 224))]

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