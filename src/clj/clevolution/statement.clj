(ns clevolution.statement)

(def db (atom []))

(defn put [fact]
    (swap! db conj fact))

(defn query [fact]
  (some #(= % fact) @db))
