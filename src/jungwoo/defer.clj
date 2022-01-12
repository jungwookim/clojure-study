(ns jungwoo.defer
  (:require [jp.nijohando.deferable :as d]))


(defn defer-test []
  (d/do*
   (prn "start")
   (d/defer (prn "end"))
   (prn "middle")
   (throw (Exception. "my exception message"))
   (prn "middle2")))

(defer-test)
