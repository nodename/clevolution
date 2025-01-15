(ns clevolution.app.state.mutationsstate
  (:require [clevolution.app.state.appstate :refer [app-state]]
            [clevolution.imagedata :refer [ORIGIN-VIEWPORT]]))

(defonce DEFAULT-IMAGE-DATA
         {:command "Initial State"
          :generator "black"
          :image nil
          :image-status :dirty
          :error-message nil
          :context "clisk"
          :viewport ORIGIN-VIEWPORT})

(defonce mutations-state (atom {:source-image-data DEFAULT-IMAGE-DATA
                                :mutation-refs     (repeat
                                                     (:num-mutations @app-state)
                                                     (ref DEFAULT-IMAGE-DATA))}))
