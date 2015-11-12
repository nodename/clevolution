(ns clevolution.app.frames
  (:import (java.awt Container)
           (javax.swing JFrame)))

(def last-frame (atom nil))


(defn create-new-frame
  [^String title]
  (let [frame (doto (JFrame. title)
                (.setVisible true)
                (.pack)
                (.setDefaultCloseOperation 2))]
    (reset! last-frame frame)
    frame))


(defn reuse-frame
  [^JFrame frame title]
  (.setTitle frame title)
  (.removeAll ^Container (.getContentPane frame))
  (if-not (.isVisible frame)
    (.validate frame)
    (.setVisible frame true))
  (.repaint frame)
  frame)
