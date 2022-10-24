(ns jungwoo.urania.urania
  (:require [urania.core :as u]
            [clojure.set :refer [intersection]]
            [promesa.core :as prom]))
(defn -friends-of
  [id]
  (prn "friends of " id)
  (set (range id))
  )

(defn count-common
  [a b]
  (count (intersection a b)))

(defn count-common-friends
  [x y]
  (count-common (-friends-of x) (-friends-of y)))

(defn count-common-friends-with-urania [x y]
  (u/map count-common
         (-friends-of x)
         (-friends-of y)))

(comment
  (count-common-friends 1 2)
  (u/run! (count-common-friends-with-urania 1 2)))

(defn remote-req [id result]
  (prom/promise
    (fn [resolve reject]
      (let [wait (rand 1000)]
        (println (str "-->[ " id " ] waiting " wait))
        (Thread/sleep wait)
        (println (str "<--[ " id " ] finished, result: " result))
        (resolve result)))))

(defrecord FriendsOf [id]
  u/DataSource
  (-identity [_] id)
  (-fetch [_ _]
    (remote-req id (set (range id)))))

(defn friends-of [id]
  (FriendsOf. id))

(comment
  (remote-req 10 (set (range 10)))
  (friends-of 10)
  (u/run! (friends-of 10))
  (deref (u/run! (friends-of 10)))
  (u/run!! (friends-of 10))
  (u/run!! (u/map count (friends-of 10)))
  (u/run!!
   (u/map dec (u/map count (friends-of 10)))))