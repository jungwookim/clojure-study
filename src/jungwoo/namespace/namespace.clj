(ns jungwoo.namespace.namespace
  (:require [jungwoo.namespace2 :as jn]))

(def a :a)
(def b ::a)

(comment
  a
  b
  jn/a
  jn/b
  jn/c
  :jn/a
  :jn/b
  :jn/c
  ::jn/a
  ::jn/b
  ::jn/c
  (= a jn/a)
  (= a jn/b)
  (= b jn/b)
  (= b jn/a)
  )
