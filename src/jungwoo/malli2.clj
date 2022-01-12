(ns jungwoo.malli2
  (:require [malli.core :as m]
            [malli.dev :as dev]
            [malli.error :as me]
            [malli.generator :as mg]
            [malli.edn]
            [malli.transform :as mt]
            [malli.dev.pretty :as pretty]
            [malli.instrument :as mi]
            [malli.edn :as edn]))


(defn foo-meta-2
  "schema via var metadataz"
  {:malli/schema [:=> [:cat :int] [:and int? [:> 18]]]}
  [x]
  (* x x))
;; (dev/start!)


(-> [:and
     [:map
      [:x int?]
      [:y int?]]
     [:fn '(fn [{:keys [x y]}] (> x y))]]
    (edn/write-string))
    ;; (doto prn) ; => "[:and [:map [:x int?] [:y int?]] [:fn (fn [{:keys [x y]}] (> x y))]]"
    ;; (edn/read-string)
    ;; (doto (-> (m/validate {:x 0
    ;;                        :y 1}) prn)) ; => false
    ;; (doto (-> (m/validate {:x 2
    ;;                        :y 1}) prn))) ; => tru

(comment
  (foo-meta-2 1)
  (foo-meta-2 4)
  (foo-meta-2 5)

  (m/function-schemas)
  ;; (mi/collect!)
  (mi/instrument!))
