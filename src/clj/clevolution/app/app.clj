(ns clevolution.app.app
  (:require [seesaw.core :as seesaw]
            [clevolution.imagedata :refer [merge-view-elements do-calc]]
            [clevolution.file-output :refer [get-generator make-generator-metadata write-image-to-file]]

            [clevolution.app.state.appstate :as appstate :refer [app-state]]
            [clevolution.app.state.currentimagestate :as currentimagestate :refer [current-image-state]]
            [clevolution.app.state.mutationsstate :refer [mutations-state]]
            [clevolution.app.state.currentimagetimetravel :as image-timetravel]
            [clevolution.app.state.mutationstimetravel :as m-timetravel]

            [clevolution.app.imagefunctions :refer [PENDING-IMAGE ERROR-IMAGE]]

            [clevolution.app.currentimagetab :refer [make-current-image-component replace-image]]
            [clevolution.app.controlpanel :refer [make-control-panel]]
            [clevolution.app.mutationstab :refer [make-mutations-component]]

            [clevolution.app.widgets.imagestatus :refer [make-image-status-panel]]
            [clevolution.app.widgets.timetravelnav :refer [make-nav-buttons]])

  (:import (java.awt FileDialog Color Container)
           (javax.swing JFrame JMenu JMenuBar JPanel JMenuItem JTabbedPane)
           (java.awt.event WindowListener)))

#_(set! *warn-on-reflection* true)
#_(set! *unchecked-math* :warn-on-boxed)



(def last-frame (atom nil))


(defn create-new-frame
  [^String title]
  (let [frame (doto (JFrame. title)
                (.setVisible true)
                (.pack)
                (.setDefaultCloseOperation 2))]
    (reset! last-frame frame)
    frame))


(defn reuse-frame
  [^JFrame frame title]
  (.setTitle frame title)
  (.removeAll ^Container (.getContentPane frame))
  (if-not (.isVisible frame)
    (.validate frame)
    (.setVisible frame true))
  (.repaint frame)
  frame)


(defn create-frame
  [title]
  (create-new-frame title)
  #_(if @last-frame
      (reuse-frame @last-frame title)
      (create-new-frame title)))




(defn load-file-dialog
  [^JFrame frame]
  (let [file-dialog (doto (FileDialog. frame
                                       "Load Image..."
                                       FileDialog/LOAD)
                      (.setFile "*.png")
                      (.setVisible true))]
    (when-let [file-name (.getFile file-dialog)]
      (let [file-path (str (.getDirectory file-dialog) file-name)
            generator (get-generator file-path)]
        (.setTitle frame file-path)
        (currentimagestate/set-loaded-data! generator "Load File")))))


(defn save-file-dialog
  [^JFrame frame]
  (let [file-dialog (doto (FileDialog. frame
                                       "Save Image As..."
                                       FileDialog/SAVE)
                      (.setFile "*.png")
                      (.setVisible true))]
    (when-let [file-name (.getFile file-dialog)]
      (write-image-to-file (:image @current-image-state)
                           (make-generator-metadata (merge-view-elements @current-image-state)
                                                    (:context @current-image-state))
                           (str (.getDirectory file-dialog) file-name)))))


(defn save-history-dialog
  [^JFrame frame]
  (let [file-dialog (doto (FileDialog. frame
                                       "Save History As..."
                                       FileDialog/SAVE)
                      (.setFile "frame")
                      (.setVisible true))]
    (when-let [file-name (.getFile file-dialog)]
      (doseq [[index state] (map-indexed vector @image-timetravel/app-history)]
        (write-image-to-file (:image state)
                             (make-generator-metadata (merge-view-elements state)
                                                      (:context state))
                             (str (.getDirectory file-dialog) file-name index ".png"))))))


(defn make-current-image-tab
  [image]
  (seesaw/left-right-split
    (seesaw/vertical-panel
      :background Color/LIGHT_GRAY
      :items [(make-current-image-component image)
              (make-image-status-panel)
              (make-nav-buttons image-timetravel/do-rewind
                                image-timetravel/do-undo
                                image-timetravel/do-redo
                                image-timetravel/do-end)])
    (make-control-panel)
    :divider-location 1/2
    :background Color/LIGHT_GRAY))



(defn make-mutations-tab
  [mutations-state]
  (seesaw/border-panel
    :id :mutations-tab
    :center (make-mutations-component (:source-image-data mutations-state)
                                      (:mutation-refs mutations-state)
                                      (make-nav-buttons m-timetravel/do-rewind
                                                        m-timetravel/do-undo
                                                        m-timetravel/do-redo
                                                        m-timetravel/do-end))))


(defn make-tabbed-panel
  [image]
  (seesaw/tabbed-panel
    :id :display-tabs
    :tabs [{:title "Current Image"
            :content (make-current-image-tab (or image PENDING-IMAGE))}
           {:title   "Mutations"
            :content (make-mutations-tab @mutations-state)}]))



(defn create-app-frame
  [image generator context title]
  (image-timetravel/forget-everything!)
  (m-timetravel/forget-everything!)

  (let [^JFrame frame (create-frame title)

        ^JPanel content-panel (seesaw/border-panel
                                :center (make-tabbed-panel image))]


    (.add frame content-panel)
    (appstate/initialize-state! content-panel)
    (currentimagestate/initialize-state! generator image context)

    (let [^JMenuItem load-menuitem (seesaw/menu-item
                                     :text "Load..."
                                     :listen [:action (fn [_] (load-file-dialog frame))])
          ^JMenuItem save-menuitem (seesaw/menu-item
                                     :text "Save As..."
                                     :listen [:action (fn [_] (save-file-dialog frame))])
          ^JMenuItem save-history-menu-item (seesaw/menu-item
                                              :text "Save History As..."
                                              :listen [:action (fn [_] (save-history-dialog frame))])

          file-menu (doto (JMenu. "File")
                      (.add load-menuitem)
                      (.add save-menuitem)
                      (.add save-history-menu-item))

          menu-bar (doto (JMenuBar.)
                     (.add file-menu))]

      (doto frame
        (.setJMenuBar menu-bar)
        (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE)
        (.pack)))))



(defn show
  "Shows a component in a new frame"
  ([component
    & {:keys [generator title on-close]
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




(defn display-image
  "Replace the current image with image and switch to the current image tab"
  [image]
  (let [^JPanel content-panel (:content-panel @app-state)
        ^JTabbedPane display-tabs (seesaw/select content-panel [:#display-tabs])
        current-image-component (seesaw/select content-panel [:#current-image-component])]
    (replace-image current-image-component image)
    (.revalidate content-panel)
    (.repaint content-panel)
    (.setSelectedIndex display-tabs 0)))






(def current-main-image-calc (atom nil))

(defn cancel-current-main-image-calc
  []
  (let [calc @current-main-image-calc]
    (when calc
      (future-cancel calc))))

(defn start-main-image-calc
  [state]
  (future
    (dosync
      (do-calc state currentimagestate/set-image!))))



(add-watch current-image-state
           :generator-watch
           (fn [k r old-state new-state]
             (if (= :dirty (:image-status new-state))
               (do
                 (cancel-current-main-image-calc)
                 (reset! current-main-image-calc
                         (start-main-image-calc new-state)))
               (display-image (:image new-state)))))

