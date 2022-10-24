(ns jungwoo.async.example2
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout take! put! pub sub go-loop]]
            [jungwoo.async.example :as example]))

(>!! example/input-chan {:msg-type :greeting
                         :text     "hi"})