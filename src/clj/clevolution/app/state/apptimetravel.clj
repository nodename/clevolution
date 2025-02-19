(ns clevolution.app.state.apptimetravel
  (:require [clevolution.app.state.appstate :refer [app-state]]))

(def app-history (atom [@app-state]))
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
  (let [old-watchable-app-state (last @app-history)]
    (when-not (= old-watchable-app-state new-state)
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
      (reset! app-state (last @app-history)))
    (println "can't undo: at initial state")))

(defn do-redo
  []
  (if (redo-is-possible)
    (do
      (swap! ignore assoc :time-machine true)
      (let [redo-state (last @app-future)]
        (push-onto-undo-stack redo-state)
        (swap! app-future pop)
        (reset! app-state redo-state)))
    (println "can't redo: at newest state")))

(defn do-rewind
  []
  (while (do-undo)))

(defn do-end
  []
  (while (do-redo)))

(def watch-fn (fn [_ _ old-state new-state]
                (when (not (@ignore :time-machine))
                  (println "new app state"))
                (swap! ignore assoc :time-machine false)))

(add-watch app-state :time-machine watch-fn)
