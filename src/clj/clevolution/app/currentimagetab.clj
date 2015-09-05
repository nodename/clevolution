(ns clevolution.app.currentimagetab
  (:use [mikera.cljutils.error])
  (:require
    [seesaw.core :as seesaw]
    [seesaw.widget-options :refer [widget-option-provider]]
    [seesaw.border :refer [line-border]]
    [clevolution.file-input :refer [read-image-from-file]]
    [clevolution.file-output :refer :all]
    [clevolution.app.imagefunctions :refer [to-display-size make-image-icon]]
    [clevolution.app.appstate :as appstate :refer [app-state]]
    [clevolution.app.widgets.imagestatus :refer [image-status-panel]])
  (:import
    [java.awt Color]))






(defn make-current-image-component
  [image]
  (let [icon (make-image-icon image
                              (:image-display-size @appstate/app-state))]
    (seesaw/border-panel :id :current-image-component
                         :background Color/LIGHT_GRAY
                         :center icon)))

(defn replace-image
  [current-image-component image]
  (let [icon (make-image-icon image
                              (:image-display-size @appstate/app-state))]
    (.removeAll current-image-component)
    (.add current-image-component icon)))



#_
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












#_
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




#_
    (add-watch app-state :generator-watch (fn [k r old-state new-state]
                                            (if (= :dirty (:image-status new-state))
                                              (do
                                                (println "image changed")
                                                ;(display-image PENDING-IMAGE)
                                                (cancel-current-calc)
                                                (reset! current-calc (start-calc new-state)))
                                              (display-image (:image new-state)))))




