(ns clevolution.app.app
  (:require [seesaw.core :as seesaw]
            [clevolution.app.state.appstate :as appstate :refer [app-state]]
            [clevolution.app.state.currentimagestate :as currentimagestate :refer [current-image-state]]
            [clevolution.app.state.mutationsstate :refer [mutations-state]]
            [clevolution.imagedata :refer [do-calc]]

            [clevolution.app.imagefunctions :refer [PENDING-IMAGE ERROR-IMAGE]]

            [clevolution.app.currentimagetab :refer [make-current-image-tab replace-image]]
            [clevolution.app.mutationstab :refer [make-mutations-tab]]
            [clevolution.app.menus :refer [make-clevolution-menu make-file-menu]]
            [clevolution.app.state.currentimagetimetravel :as image-timetravel]
            [clevolution.app.state.mutationstimetravel :as m-timetravel])

  (:import (java.awt FileDialog Color Container)
           (javax.swing JFrame JMenuBar JPanel JMenuItem JTabbedPane)
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

    (let [clevolution-menu (make-clevolution-menu)
          file-menu (make-file-menu frame)

          menu-bar (doto (JMenuBar.)
                     (.add clevolution-menu)
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

