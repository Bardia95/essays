(ns cryogen.core
  (:require [cryogen-core.compiler :refer [compile-assets-timed]]
            [cryogen-core.plugins :refer [load-plugins]]))

(defn hi []
  (println "hi"))

(defn -main []
  (load-plugins)
  (compile-assets-timed)
  (System/exit 0))
