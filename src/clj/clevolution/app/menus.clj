(ns clevolution.app.menus
  (:require
    [seesaw.core :as seesaw :refer [action return-from-dialog value to-frame alert
                                    custom-dialog label combobox slider flow-panel
                                    pack! show!]]
    [seesaw.mig :refer [mig-panel]]
    [seesaw.border :refer [line-border]]
    [seesaw.font :refer [font default-font]]

    [clevolution.file-output :refer [get-generator make-generator-metadata write-image-to-file]]
    [clevolution.app.state.appstate :as appstate :refer [app-state]]
    [clevolution.app.state.currentimagestate :as currentimagestate :refer [current-image-state]]
    [clevolution.app.state.currentimagetimetravel :as image-timetravel]
    [clevolution.imagedata :refer [merge-view-elements]])

  (:import
    (java.awt FileDialog Color Container)
    (javax.swing JFrame JMenu JMenuBar JPanel JMenuItem JTabbedPane)
    (java.awt.event WindowListener)))

(defn open-preferences-dialog
  []
  (let [ok-act (action
                 :name "Ok"
                 :handler (fn [e] (return-from-dialog e (value (to-frame e)))))
        cancel-act (action :name "Cancel"
                           :handler (fn [e] (return-from-dialog e nil)))]
    (-> (custom-dialog
          :title  "Preferences"
          :modal? true
          :resizable? false
          :content (mig-panel
                     :border (line-border)
                     :items [[(label :font (font :from (default-font "Label.font") :style :bold)
                                     :text "Clevolution options")
                              "gaptop 10, wrap"]

                             [:separator "growx, wrap, gaptop 10, spanx 2"]

                             ["Number of Mutations"]

                             [(slider :id :num-mutations
                                      :min 0 :max 20 :value (:num-mutations @app-state)
                                      :minor-tick-spacing 1 :major-tick-spacing 10
                                      :paint-labels? true)
                              "wrap"]

                             [(flow-panel :align :right :items [ok-act cancel-act])
                              "spanx 2" "alignx right"]]))
        pack!
        show!)))

(defn make-clevolution-menu
  []
  (let [preferences-menu-item (seesaw/menu-item
                                :text "Preferences..."
                                :listen [:action (fn [_]
                                                   (let [value (open-preferences-dialog)]
                                                     (swap! app-state
                                                            merge value)))])]

    (doto (JMenu. "Clevolution")
      (.add preferences-menu-item))))

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


(defn make-file-menu
  [frame]
  (let [^JMenuItem load-menuitem (seesaw/menu-item
                                   :text "Load..."
                                   :listen [:action (fn [_] (load-file-dialog frame))])
        ^JMenuItem save-menuitem (seesaw/menu-item
                                   :text "Save As..."
                                   :listen [:action (fn [_] (save-file-dialog frame))])
        ^JMenuItem save-history-menu-item (seesaw/menu-item
                                            :text "Save History As..."
                                            :listen [:action (fn [_] (save-history-dialog frame))])]

    (doto (JMenu. "File")
      (.add load-menuitem)
      (.add save-menuitem)
      (.add save-history-menu-item))))
