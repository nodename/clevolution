(ns clevolution.app.state.mutationstimetravel
  (:require [clevolution.app.state.mutationsstate :refer [mutations-state]]))


(def app-history (atom [@mutations-state]))
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
  (let [old-watchable-mutations-state (last @app-history)]
    (when-not (= old-watchable-mutations-state new-state)
      (swap! app-history conj new-state))))


;; Undo and redo cause state changes that we want our watch-fn to ignore,
;; hence the ignore atom. (This technique copied from Om's :tx-listen implementation)

(defn do-undo
  []
  (try
    (if (undo-is-possible)
      (do
        (swap! app-future conj (last @app-history))
        (swap! app-history pop)
        (swap! ignore assoc :time-machine true)
        (reset! mutations-state (last @app-history)))
      (println "can't undo: at initial state"))
    (catch Exception e
      (println (.getMessage e)))))

(defn do-redo
  []
  (if (redo-is-possible)
    (do
      (swap! ignore assoc :time-machine true)
      (let [redo-state (last @app-future)]
        (push-onto-undo-stack redo-state)
        (swap! app-future pop)
        (reset! mutations-state redo-state)))
    (println "can't redo: at newest state")))

(defn do-rewind
  []
  (while (do-undo)))

(defn do-end
  []
  (try
    (while (do-redo))
    (catch Exception e
      (println (.getMessage e)))))


