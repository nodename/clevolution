(ns clevolution.app.mutationsstate
  (:require [clevolution.app.appstate :refer [app-state]]
            [clevolution.imagedata :refer [ORIGIN-VIEWPORT]]))


(defonce DEFAULT-IMAGE-DATA
         {:command "Initial State"
          :generator "black"
          :image nil
          :image-status :dirty
          :error-message nil
          :context "clisk"
          :viewport ORIGIN-VIEWPORT})

(defonce mutations-state (atom {:source         DEFAULT-IMAGE-DATA
                                :mutation-atoms (repeat (:num-mutations @app-state) (atom DEFAULT-IMAGE-DATA))}))
