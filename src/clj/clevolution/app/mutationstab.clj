(ns clevolution.app.mutationstab
  (:require [seesaw.core :as seesaw]
            [clevolution.imagedata :refer [do-calc set-image-in-image-data!]]
            [clevolution.app.imagefunctions :refer [make-image-icon
                                                    PENDING-IMAGE ERROR-IMAGE]]
            [clevolution.app.appstate :refer [app-state]]
            [clevolution.app.mutationsstate :refer [mutations-state]])
  (:import (javax.swing JTabbedPane JPanel)))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def SOURCE-IMAGE-DISPLAY-SIZE 250)
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
                  :text "Make Current Image"
                  :listen [:action (fn [_]
                                     (reset! app-state
                                             (merge @app-state
                                                    image-data
                                                    {:command "Make Current Image"})))])]))))


(defn make-mutation-image-component
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
                             (let [mutation-atoms (:mutation-atoms @mutations-state)
                                   new-mutation-atoms (remove (fn [m] (= m mutation-atom)) mutation-atoms)]
                               (swap! mutations-state assoc :mutation-atoms new-mutation-atoms)))])]))))


(defn make-mutations-grid-panel
  [mutation-atoms]
  (seesaw/grid-panel
    :id :mutations-grid-panel
    :columns 4
    :items (mapv (fn [index mutation-atom]
                   (make-mutation-image-component
                     (image-from-status @mutation-atom)
                     (mutation-id index)
                     MUTATION-DISPLAY-SIZE
                     mutation-atom))
                 (range)
                 mutation-atoms)))


(defn make-inner-mutations-component
  [source-image-data mutation-atoms timetravel-nav-buttons]
  (seesaw/border-panel
    :west (seesaw/vertical-panel
            :items [(make-source-image-component source-image-data
                                                 SOURCE-IMAGE-DISPLAY-SIZE)
                    timetravel-nav-buttons])
    :center (make-mutations-grid-panel mutation-atoms)))


(defn make-mutations-component
  [source-image-data mutation-atoms timetravel-nav-buttons]
  (seesaw/border-panel
    :id :mutations-component
    :center (make-inner-mutations-component source-image-data mutation-atoms timetravel-nav-buttons)))




(defn replace-mutations
  [^JPanel mutations-component source-image-data mutation-atoms]
  (println "replace mutations")
  (let [timetravel-nav-buttons (seesaw/select mutations-component [:#timetravel-nav])
        ^JPanel inner-component (make-inner-mutations-component source-image-data
                                                                mutation-atoms
                                                                timetravel-nav-buttons)]
    (.removeAll mutations-component)
    (.add mutations-component inner-component)))



(defn replace-image-in-mutations-tab
  [new-image index image-data-atom]
  (let [content-panel (:content-panel @app-state)
        mutations-grid-panel (seesaw/select content-panel [:#mutations-grid-panel])
        component-id (mutation-id index)
        old-component (seesaw/select mutations-grid-panel [(mutation-search-id index)])
        new-component (make-mutation-image-component new-image component-id MUTATION-DISPLAY-SIZE image-data-atom)]
    (seesaw/replace! mutations-grid-panel old-component new-component)))


(defn kick-off-mutation-calc
  [image-data-atom index]
  ;; add change watcher on mutation's image status:
  (add-watch image-data-atom :image-watch
             (fn [k r old-state new-state]
               (when (not= (:image-status old-state) (:image-status new-state))
                 (condp = (:image-status new-state)
                   :ok (replace-image-in-mutations-tab (:image new-state) index image-data-atom)

                   :failed
                   ;; TODO initiate new mutation
                   (replace-image-in-mutations-tab ERROR-IMAGE index image-data-atom)))))

  ;; kick off image calc on mutation:
  (future (do-calc @image-data-atom (partial set-image-in-image-data! image-data-atom))))


(defn kick-off-mutations!
  [mutation-atoms]
  (loop [mutation-atoms mutation-atoms
         index 0]
    (when-let [m-atom (first mutation-atoms)]
      (let [futur (kick-off-mutation-calc m-atom index)]
        ;; NO NO NO, saving the future in the atom makes everything go to hell
        ;; when we try to Undo in the mutations tab
        #_(swap! m-atom assoc
               :calc-future futur))
      (recur (rest mutation-atoms) (inc index)))))


(defn display-mutations
  [new-state]
  (let [source-image-data (:source-image-data new-state)
        mutation-atoms (:mutation-atoms new-state)
        ^JPanel content-panel (:content-panel @app-state)
        ^JTabbedPane display-tabs (seesaw/select content-panel [:#display-tabs])
        mutations-tab (seesaw/select content-panel [:#mutations-tab])
        mutations-component (seesaw/select content-panel [:#mutations-component])]
    (replace-mutations mutations-component source-image-data mutation-atoms)
    (.revalidate content-panel)
    (.repaint content-panel)
    (.setSelectedComponent display-tabs mutations-tab)))
