(ns clevolution.app.mutationstab
  (:require [seesaw.core :as seesaw]
            [clevolution.imagedata :refer [do-calc set-image-in-image-data!]]
            [clevolution.app.imagefunctions :refer [make-image-icon
                                                    PENDING-IMAGE ERROR-IMAGE]]
            [clevolution.app.appstate :refer [app-state]]))


(defn make-image-component
  [image id size]
  (let [icon (make-image-icon image size)]
    (seesaw/border-panel :id id
                         :center icon)))


(defn make-mutations-grid-panel
  [mutation-atoms]
  (seesaw/grid-panel
    :id :mutations-grid-panel
    :columns 4
    :items (mapv (fn [index mutation-atom]
                   (make-image-component
                     (condp = (:image-status @mutation-atom)
                       :ok (:image @mutation-atom)
                       :dirty PENDING-IMAGE
                       :failed ERROR-IMAGE)
                     (keyword (str "mutation-" index))
                     150))
                 (range)
                 mutation-atoms)))


(defn make-mutations-component
  [mutation-atoms]
  (seesaw/border-panel
    :id :mutations-component
    :center (make-mutations-grid-panel mutation-atoms)))


(defn make-mutations-tab
  [mutation-atoms]
  (seesaw/border-panel
    :id :mutations-tab
    :center (make-mutations-component mutation-atoms)))


(defn replace-mutations
  [mutations-component mutation-atoms]
  (println "replace mutations")
  (let [grid-panel (make-mutations-grid-panel mutation-atoms)]
    (.removeAll mutations-component)
    (.add mutations-component grid-panel)))



(defn replace-mutation
  [new-image index]
  (let [content-panel (:content-panel @app-state)
        mutations-grid-panel (seesaw/select content-panel [:#mutations-grid-panel])
        component-id (keyword (str "mutation-" index))
        old-component (seesaw/select mutations-grid-panel [(keyword (str "#mutation-" index))])
        new-component (make-image-component new-image component-id 150)]
    (seesaw/replace! mutations-grid-panel old-component new-component)))


(defn kick-off
  [image-data-atom index]
  ;; add change watcher on mutation:
  (add-watch image-data-atom :image-watch
             (fn [k r old-state new-state]
               (when (not= (:image-status old-state) (:image-status new-state))
                 (condp = (:image-status new-state)
                   :ok (replace-mutation (:image new-state) index)
                   :failed (replace-mutation ERROR-IMAGE index)))))

  ;; kick off image calc on mutation:
  (future (do-calc @image-data-atom (partial set-image-in-image-data! image-data-atom))))


(defn display-mutations
  [mutation-atoms]
  (let [content-panel (:content-panel @app-state)
        display-tabs (seesaw/select content-panel [:#display-tabs])
        mutations-tab (seesaw/select content-panel [:#mutations-tab])
        mutations-component (seesaw/select content-panel [:#mutations-component])]
    (replace-mutations mutations-component mutation-atoms)
    (.revalidate content-panel)
    (.repaint content-panel)
    (.setSelectedComponent display-tabs mutations-tab)

    (loop [mutation-atoms mutation-atoms
           index 0]
      (kick-off (first mutation-atoms) index)
      (recur (rest mutation-atoms) (inc index)))))



(add-watch app-state :mutations-watch (fn [k r old-state new-state]
                                        (when (not= (:mutations old-state) (:mutations new-state))
                                          (println "Mutations changed!")
                                          (display-mutations (:mutations new-state)))))