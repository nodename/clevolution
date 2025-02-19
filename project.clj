(defproject com.nodename/clevolution "0.1.1"
  :description "Evolutionary art library in Clojure"
  :source-paths ["src/clj" #_"../clisk/src/main/clojure" #_"../seesaw/src"]
  :java-source-paths ["src/java" #_"../clisk/src/main/java"]
  :repositories {"local" ~(str (.toURI (java.io.File. "maven_repository")))}
  :dependencies [[org.clojure/clojure "1.8.0-alpha5"]
                 [net.mikera/clisk "0.11.0"]
                 [net.mikera/vectorz-clj "0.48.0"]
                 [net.mikera/imagez "0.12.0"]
                 [net.mikera/mikera-gui "0.3.1"]
                 [instaparse "1.5.0"]
                 [net.mikera/telegenic "0.0.1"]
                 #_[jmagick/jmagick "6.6.9"]
                 [seesaw "1.5.0" :exclusions [org.clojure/clojure]]
                 #_[com.miglayout/miglayout "3.7.4"]
                 #_[com.jgoodies/forms "1.2.1"]
                 #_[org.swinglabs.swingx/swingx-core "1.6.3"]
                 #_[j18n "1.0.2"]
                 #_[com.fifesoft/rsyntaxtextarea "2.5.6"]]
  :plugins [[lein-marginalia "0.9.2"]]

  :main clevolution.main
  )
