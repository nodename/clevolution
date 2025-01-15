(ns clevolution.app.state.currentimagetimetravel
  (:require [clevolution.app.state.currentimagestate :refer [current-image-state]]))

(def app-history (atom [@current-image-state]))
(def app-future (atom []))

(def ignore (atom {:time-machine false}))

(defn forget-everything!
  []
  (reset! app-future [])
  (reset! app-history []))

(defn undo-is-possible
  []
  (> (count @app-history) 1))

(defn redo-is-possible
  []
  (> (count @app-future) 0))

(defn push-onto-undo-stack
  [new-state]
  (let [last-state (last @app-history)]
    (when-not (= last-state new-state)
      (swap! app-history conj new-state))))

(defn replace-last-state-with
  [new-state]
  (reset! app-history (conj (vec (butlast @app-history))
                            new-state)))

;; Undo and redo cause state changes that we want our watch-fn to ignore,
;; hence the ignore atom. (This technique copied from Om's :tx-listen implementation)

(defn do-undo
  []
  (if (undo-is-possible)
    (do
      (swap! app-future conj (last @app-history))
      (swap! app-history pop)
      (swap! ignore assoc :time-machine true)
      (reset! current-image-state (last @app-history)))
    (println "can't undo: at initial state")))

(defn do-redo
  []
  (if (redo-is-possible)
    (do
      (swap! ignore assoc :time-machine true)
      (let [redo-state (last @app-future)]
        (push-onto-undo-stack redo-state)
        (swap! app-future pop)
        (reset! current-image-state redo-state)))
    (println "can't redo: at newest state")))

(defn do-rewind
  []
  (while (do-undo)))

(defn do-end
  []
  (while (do-redo)))

(def watch-fn (fn [_ _ old-state new-state]
                (if (@ignore :time-machine)
                  (println "no new state")
                  (cond
                    ;; there is new image or panel data:
                    (or (not= (:generator old-state) (:generator new-state))
                        (not= (:viewport old-state) (:viewport new-state))
                        (not= (:z old-state) (:z new-state)))
                    (do
                      (println "NEW IMAGE STATE")
                      (push-onto-undo-stack new-state))


                    ;; image has been updated in response to already seen new state data:
                    (not= (:image old-state) (:image new-state))
                    (do
                      (println "Only image changed: amending state")
                      (replace-last-state-with new-state))


                    :else
                    (println "no image change")))
                (swap! ignore assoc :time-machine false)))

(add-watch current-image-state :time-machine watch-fn)

