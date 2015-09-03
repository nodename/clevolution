(ns clevolution.app.view
  (:use [mikera.cljutils.error])
  (:require
    [mikera.image.core :as img]
    [clevolution.file-input :refer [read-image-from-file]]
    [clevolution.file-output :refer :all]
    [clevolution.imagedata :as state :refer [merge-view-elements do-calc]]
    [clevolution.app.appstate :as appstate :refer [app-state]]
    [seesaw.core :as seesaw]
    [seesaw.selector :as selector]
    [seesaw.widget-options :refer [widget-option-provider]]
    [seesaw.border :refer [line-border]])
  (:import [java.awt Dimension]
           [java.awt.image BufferedImage]
           [mikera.gui JIcon]
           (javax.swing JLabel)))


;; Make JIcon play nice with seesaw:
(widget-option-provider mikera.gui.JIcon seesaw/default-options)


(defonce PENDING-IMAGE (read-image-from-file "resources/Pending.png"))




(defn to-display-size
  [^BufferedImage bi]
  (let [factor (/ (:image-display-size @app-state) (.getWidth bi))]
    (img/zoom bi factor)))


(defn make-image-icon
  [image size]
  (doto (JIcon. image)
    (.setMinimumSize (Dimension. size size))
    (.setMaximumSize (Dimension. size size))))


(defn make-image-component
  [image id size]
  (let [icon (make-image-icon image size)]
    (seesaw/border-panel :id id
                         :center icon)))


(defn make-current-image-component
  [image]
  (let [display-size (:image-display-size @appstate/app-state)
        icon (make-image-icon (to-display-size image) display-size)]
    (seesaw/border-panel :id :image-component
                         :center icon)))


(defn make-mutations-component
  [mutation-atoms & [id]]
  (let [xform-mutation (fn [image-data-atom] (-> image-data-atom
                                                 deref
                                                 (get :image)
                                                 (make-image-icon 150)))
        component (seesaw/border-panel
                    :size [600 :by 600]
                    :center (seesaw/grid-panel
                              :id :mutations-grid-panel
                              :columns 4
                              :items (mapv (fn [image index] (make-image-component
                                                               image
                                                               (keyword (str "mutation-" index))
                                                               150))
                                           (repeat PENDING-IMAGE)
                                           (range 16))))]
    (when id (selector/id-of! component id))
    component))




(defn make-display-panel
  [& [image]]
  (seesaw/border-panel
    :size [600 :by 640]
    :center (seesaw/tabbed-panel
              :id :display-tabs
              :tabs [{:title "Current Image"
                      :content (make-current-image-component (or image PENDING-IMAGE))}
                     {:title "Mutations"
                      :content (make-mutations-component
                                 []
                                 :mutations-component)}])))

#_
(def panel-behaviors
  [{:name "Current Image"
    :update-fn (fn [root image]
                 (seesaw/config! (seesaw/select root [:#image-component])
                                 :center (make-image-icon image (:image-display-size @app-state))))}])

#_
    (defn refresh
      [e]
      (let [root (to-frame e)]
        (doseq [{:keys [name update-fn]} panel-behaviors]
          (future
            (invoke-later
              (update-fn root data))))))




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
  (let [content-panel (:content-panel @app-state)
        display-tabs (seesaw/select content-panel [:#display-tabs])
        content (.getComponentAt display-tabs 0)]
    (.removeAll content)
    (.add content (make-current-image-component image))
    (.revalidate content)
    (.repaint content)
    (seesaw/selection! display-tabs 0)))



(defn display-mutations
  [mutations]
  (let [content-panel (:content-panel @app-state)
        display-tabs (seesaw/select content-panel [:#display-tabs])
        content (.getComponentAt display-tabs 1)]
    (.removeAll content)
    (.add content (make-mutations-component mutations :mutations-component))
    (.revalidate content)
    (.repaint content)
    (seesaw/selection! display-tabs 1)))





(add-watch app-state :generator-watch (fn [k r old-state new-state]
                                        (if (= :dirty (:image-status new-state))
                                          (do
                                            (println "image changed")
                                            ;(display-image PENDING-IMAGE)
                                            (cancel-current-calc)
                                            (reset! current-calc (start-calc new-state)))
                                          (display-image (:image new-state)))))




(add-watch app-state :mutations-watch (fn [k r old-state new-state]
                                        (when (not= (:mutations old-state) (:mutations new-state))
                                          (println "Mutations changed!")
                                          (display-mutations (:mutations new-state)))))
