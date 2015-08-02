(ns clevolution.foo)

(def resources  {   :automobile (resource/put "automobile")   :car (resource/put "car")   :truck (resource/put "truck")   :honda-accord (resource/put "Honda Accord")   :color (resource/put "color")   :blue (resource/put "blue")   :red (resource/put "red")   :is-a (resource/put "is a" :transitive)   })(statement/put  {:subject (:car resources)   :predicate (:is-a resources)   :object (:automobile)})

(statement/query  {:subject (:honda-accord resources)   :predicate (:color resources)   :object (:blue resources)});; true