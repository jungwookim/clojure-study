(ns jungwoo.async.example
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout take! put! pub sub go-loop]]))

; core.async의 핵심은 process이다.

(def echo-chan (chan)) ; create channel communicating messages
; go create a new process
; go block안의 모든 것들은 분리된 쓰레드에서 동시에 실행됨
(go (println (<! echo-chan)))
(>!! echo-chan "ketchup")


; 첫번째 !는 사이드이펙트를 뜻함
; 두번째 !는 thread blocking을 의미한다
(let [c (chan 2)]
  (thread (doseq [x (range 1 5)]
            (>!! c x)
            (prn "putting value " x " on channel")))
  (thread 
    (Thread/sleep 1000)
    (doseq [x (range 1 3)]
            (println "from chan" (<!! c)))))

;; callback
(let [c (chan)]
  (thread (put! c "On the code again" (fn [sent?] (println "has been sent? " sent?))))
  (thread
    (take! c (fn [value]
               (println "taken= " value)))))


;; go block
(let [c (chan)]
  (go (doseq [x (range 1 5)]
            (>! c x)
            (prn "putting value " x " on channel")))
  (go
    (doseq [x (range 1 3)]
      (println "from chan" (<! c)))))

;; pub sub
(def input-chan (chan))
(def our-pub (pub input-chan :msg-type)) ; our-pub is a publication
; :msg-type은 topic-fn으로 싸용됨
; a publication is not a channel

(def output-chan (chan))
(sub our-pub output-chan :greeting)

(go-loop []
  (let [{:keys [text]} (<! output-chan)]
    (println text)
    (recur)))

(>!! input-chan {:msg-type :greeting
                 :text     "hi"})

(comment
  ;; go block
  (do
    (prn "before start")
    (let [c (chan)]
      (go (doseq [x (range 1 5)]
            (Thread/sleep 1000)
            (>! c x)
            (prn "putting value " x " on channel")))
      (go
        (doseq [x (range 1 3)]
          (println "from chan" (<! c)))))
    (prn "after done"))
  )


(comment
  ;; pub/sub
  (def input-chan (chan))
  (def our-pub (pub input-chan :msg-type)) ; our-pub is a publication
; :msg-type은 topic-fn으로 싸용됨
; a publication is not a channel

  (def output-chan (chan))
  (sub our-pub :greeting output-chan)

  (go-loop []
    (let [{:keys [text]
           :as   value} (<! output-chan)]
      (println value)
      (println text)
      (recur)))

  (>!! input-chan {:msg-type :greeting
                   :text     "hi"}))

(comment
  ; publisher is just a normal channel
  (def publisher (chan))

; publication is a thing we subscribe to
  (def publication
    (pub publisher #(:topic %)))

  ; define a bunch of subscribers
  (def subscriber-one (chan))
  (def subscriber-two (chan))
  (def subscriber-three (chan))

; subscribe
  (sub publication :account-created subscriber-one)
  (sub publication :account-created subscriber-two)
  (sub publication :user-logged-in  subscriber-two)
  (sub publication :change-page     subscriber-three)

  (defn take-and-print [channel prefix]
    (go-loop []
      (println prefix ": " (<! channel))
      (recur)))

  (take-and-print subscriber-one "subscriber-one")
  (take-and-print subscriber-two "subscriber-two")
  (take-and-print subscriber-three "subscriber-three")

  (go (>! publisher {:topic :change-page
                     :dest  "/#home"}))
  )