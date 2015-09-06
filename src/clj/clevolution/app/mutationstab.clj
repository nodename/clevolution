(ns clevolution.app.mutationstab
  (:require [seesaw.core :as seesaw]
            [clevolution.imagedata :refer [do-calc set-image-in-image-data!]]
            [clevolution.app.imagefunctions :refer [make-image-icon
                                                    PENDING-IMAGE ERROR-IMAGE]]
            [clevolution.app.appstate :refer [app-state mutations-state]]))


(def MUTATION-DISPLAY-SIZE 150)

(defn mutation-id
  [i]
  (keyword (str "mutation-" i)))

(defn mutation-search-id
  [i]
  (keyword (str "#mutation-" i)))

(defn mutation-index
  [mutation-id]
  (read-string (.replace (name mutation-id) "mutation-" "")))


(defn image-from-status
  [image-data]
  (condp = (:image-status image-data)
    :ok (:image image-data)
    :dirty PENDING-IMAGE
    :failed ERROR-IMAGE))


(defn make-image-component
  [image id size mutation-atom]
  ;; for some reason, calling image-from-status from this function
  ;; gives a crazy error "BufferedImage cannot be cast to Future",
  ;; so we pass the mutation-atom in as a separqte argument
  (let [icon (make-image-icon image size)]
    (seesaw/border-panel
      :id id
      :center icon
      ;; TODO popup should be on icon, not entire component
      :popup (seesaw/popup
               :items
               [(seesaw/menu-item
                  :text "Show Expression"
                  :listen [:action (fn [_]
                                     (println (:generator @mutation-atom)))])

                (seesaw/menu-item
                  :text "Make Current Image"
                  :listen [:action (fn [_]
                                     (reset! app-state
                                             (merge @app-state
                                                    @mutation-atom
                                                    {:command "Make Current Image"})))])
                (seesaw/menu-item
                  :text "Delete"
                  :listen [:action
                           (fn [_]
                             (let [mutation-atoms (:mutations @mutations-state)
                                   new-mutation-atoms (remove (fn [m] (= m mutation-atom)) mutation-atoms)]
                                 (swap! mutations-state assoc :mutations new-mutation-atoms)))])]))))


(defn make-mutations-grid-panel
  [mutation-atoms]
  (seesaw/grid-panel
      :id :mutations-grid-panel
      :columns 4
      :items (mapv (fn [index mutation-atom]
                     (make-image-component
                       (image-from-status @mutation-atom)
                       (mutation-id index)
                       MUTATION-DISPLAY-SIZE
                       mutation-atom))
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



(defn replace-mutation-image
  [new-image index image-data-atom]
  (let [content-panel (:content-panel @app-state)
        mutations-grid-panel (seesaw/select content-panel [:#mutations-grid-panel])
        component-id (mutation-id index)
        old-component (seesaw/select mutations-grid-panel [(mutation-search-id index)])
        new-component (make-image-component new-image component-id MUTATION-DISPLAY-SIZE image-data-atom)]
    (seesaw/replace! mutations-grid-panel old-component new-component)))


(defn kick-off
  [image-data-atom index]
  ;; add change watcher on mutation:
  (add-watch image-data-atom :image-watch
             (fn [k r old-state new-state]
               (when (not= (:image-status old-state) (:image-status new-state))
                 (condp = (:image-status new-state)
                   :ok (replace-mutation-image (:image new-state) index image-data-atom)
                   :failed (replace-mutation-image ERROR-IMAGE index image-data-atom)))))

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
      (when-let [m-atom (first mutation-atoms)]
        (kick-off m-atom index)
        (recur (rest mutation-atoms) (inc index))))))



(add-watch mutations-state :mutations-watch (fn [k r old-state new-state]
                                        (when (not= (:mutations old-state) (:mutations new-state))
                                          (println "Mutations changed!")
                                          (display-mutations (:mutations new-state)))))