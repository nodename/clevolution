(ns clevolution.app.mutationstab
  (:require [seesaw.core :as seesaw]
            [clevolution.imagedata :refer [do-calc set-image-in-image-data! image-from-status]]
            [clevolution.app.imagefunctions :refer [make-image-icon]]
            [clevolution.app.state.appstate :refer [app-state]]
            [clevolution.app.state.currentimagestate :refer [current-image-state]]
            [clevolution.app.state.mutationsstate :refer [mutations-state]])
  (:import (javax.swing JTabbedPane JPanel)))


(def SOURCE-IMAGE-DISPLAY-SIZE 350)
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



(defn make-source-image-component
  [image-data size]
  (let [image (image-from-status image-data)
        icon (make-image-icon image size)]
    (seesaw/border-panel
      :id :source-image
      :center icon
      ;; TODO popup should be on icon, not entire component
      :popup (seesaw/popup
               :items
               [(seesaw/menu-item
                  :text "Show Expression"
                  :listen [:action (fn [_]
                                     (println (:generator image-data)))])

                (seesaw/menu-item
                  :text "Set As Current Image"
                  :listen [:action (fn [_]
                                     (reset! current-image-state
                                             (merge @current-image-state
                                                    image-data
                                                    {:command "Set As Current Image"})))])]))))


(defn make-mutation-image-component
  [image id size mutation-ref]
  ;; for some reason, calling image-from-status from this function
  ;; gives a crazy error "BufferedImage cannot be cast to Future",
  ;; so we pass the image and the mutation-ref in separately
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
                                     (println (:generator @mutation-ref)))])

                (seesaw/menu-item
                  :text "Set As Current Image"
                  :listen [:action (fn [_]
                                     (reset! current-image-state
                                             (merge @current-image-state
                                                    @mutation-ref
                                                    {:command "Set As Current Image"})))])
                (seesaw/menu-item
                  :text "Delete"
                  :listen [:action
                           (fn [_]
                             (let [mutation-refs (:mutation-refs @mutations-state)
                                   new-mutation-refs (remove (fn [m] (= m mutation-ref)) mutation-refs)]
                               (swap! mutations-state assoc :mutation-refs new-mutation-refs)))])]))))


(defn make-mutations-grid-panel
  [mutation-refs]
  (seesaw/grid-panel
    :id :mutations-grid-panel
    :columns 4
    :items (mapv (fn [index mutation-ref]
                   (make-mutation-image-component
                     (image-from-status @mutation-ref)
                     (mutation-id index)
                     MUTATION-DISPLAY-SIZE
                     mutation-ref))
                 (range)
                 mutation-refs)))


(defn make-inner-mutations-component
  [source-image-data mutation-refs timetravel-nav-buttons]
  (seesaw/border-panel
    :west (seesaw/vertical-panel
            :items [(make-source-image-component source-image-data
                                                 SOURCE-IMAGE-DISPLAY-SIZE)
                    timetravel-nav-buttons])
    :center (make-mutations-grid-panel mutation-refs)))


(defn make-mutations-component
  [source-image-data mutation-refs timetravel-nav-buttons]
  (seesaw/border-panel
    :id :mutations-component
    :center (make-inner-mutations-component source-image-data mutation-refs timetravel-nav-buttons)))




(defn replace-mutations
  [^JPanel mutations-component source-image-data mutation-refs]
  (let [timetravel-nav-buttons (seesaw/select mutations-component [:#timetravel-nav])
        ^JPanel inner-component (make-inner-mutations-component source-image-data
                                                                mutation-refs
                                                                timetravel-nav-buttons)]
    (.removeAll mutations-component)
    (.add mutations-component inner-component)))



(defn replace-image-in-mutations-tab
  [new-image index image-data-ref]
  (let [content-panel (:content-panel @app-state)
        mutations-grid-panel (seesaw/select content-panel [:#mutations-grid-panel])
        component-id (mutation-id index)
        old-component (seesaw/select mutations-grid-panel [(mutation-search-id index)])
        new-component (make-mutation-image-component new-image component-id MUTATION-DISPLAY-SIZE image-data-ref)]
    (seesaw/replace! mutations-grid-panel old-component new-component)))


(defn kick-off-mutation-calc
  [image-data-ref index]
  ;; add change watcher on mutation ref's image status:
  (add-watch image-data-ref :image-watch
             (fn [k r old-state new-state]
               (when (not= (:image-status old-state) (:image-status new-state))
                 (replace-image-in-mutations-tab (image-from-status new-state) index image-data-ref))))

  ;; kick off image calc on mutation:
  (future
    (dosync
      (do-calc @image-data-ref (partial set-image-in-image-data! image-data-ref)))))


(defn kick-off-mutation-calcs!
  [mutation-refs]
  (loop [mutation-refs mutation-refs
         index 0]
    (when-let [m-ref (first mutation-refs)]
      (kick-off-mutation-calc m-ref index)
      (recur (rest mutation-refs) (inc index)))))


(defn display-mutations
  [new-state]
  (let [source-image-data (:source-image-data new-state)
        mutation-refs (:mutation-refs new-state)
        ^JPanel content-panel (:content-panel @app-state)
        ^JTabbedPane display-tabs (seesaw/select content-panel [:#display-tabs])
        mutations-tab (seesaw/select content-panel [:#mutations-tab])
        mutations-component (seesaw/select content-panel [:#mutations-component])]
    (replace-mutations mutations-component source-image-data mutation-refs)
    (.revalidate content-panel)
    (.repaint content-panel)
    (.setSelectedComponent display-tabs mutations-tab)))
