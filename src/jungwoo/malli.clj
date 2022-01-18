(ns jungwoo.malli
  (:require [malli.core :as m]
            [malli.dev :as dev]
            [malli.error :as me]
            [malli.generator :as mg]
            [malli.edn]
            [malli.transform :as mt]
            [malli.dev.pretty :as pretty]))
   


(defn foo-meta
  "schema via var metadataz"
  {:malli/schema [:=> [:cat :int] :int]}
  [x]
  (inc x))

(defn pure-foo [x] (inc x))

(m/=> foo-declare [:=> [:cat :int] :int])
(defn foo-declare
  "schema via separate declaration"
  [x]
  (inc x))

(defn foo-vector
  {:malli/schema [:=> [:cat [:vector :int] :string] :int]}
  [vec-int str-arg]
  (prn vec-int str-arg)
  vec-int)

(defn foo-bar
  {:malli/schema [:=> [:cat :int :string [:vector :int] [:map [:x :int]]] :int]}
  [int-arg str-arg vector-arg map-arg]
  (prn int-arg str-arg vector-arg map-arg)
  str-arg)

(def subsidy-schema
  [:and
   [:map
    [:id int?]
    [:area int?]
    [:area-ineqaulity [:enum ">=" ">" "<" "<="]]]
   [:fn {:error/message "area and area-inequality는 nil match가 되어야함"}
    (fn [{:keys [area area-ineqaulity]}]
      (or (and (nil? area) (nil? area-ineqaulity))
          (and (some? area) (some? area-ineqaulity))))]])

(def pants-schema
  [:and
   [:map
    [:id int?]
    [:size {:optional true} [:maybe :int]]
    [:size-alphabet {:optional true} [:maybe [:enum "S" "M" "L"]]]]
   [:fn {:error/message "size and size alphabet should be nil-matched"}
    '(fn [{:keys [size size-alphabet]}]
       (or (and (nil? size) (nil? size-alphabet))
           (and (some? size) (some? size-alphabet))))]])

(def insert-return-schema
 [:map
  {:closed true}
  [:GENERATED_KEY pos-int?]])

(defn do-something-with-pants
  {:malli/schema [:=> [:cat pants-schema] :nil]}
  [pants]
  (prn pants))

(defn insert-subsidy-info
  {:malli/schema [:=> [:cat subsidy-schema] :nil]}
  [subsidy]
  (prn subsidy))

(defn insert-subsidy-push
  {:malli/schema [:=> [:cat :any string? :string] insert-return-schema]}
  [db user-id subsidy-id]
  (prn db
       {:user_id    user-id
        :subsidy_id subsidy-id})
  {:GENREATED_KEY 1})
  
(def valid?
  (m/validator
   [:map
    [:x :boolean]
    [:y {:optional true} [:maybe :int]]
    [:z :string]]))

(valid? {:x true
         :y nil
         :z "kikka"})

(def Adult
  [:map {:registry {::age [:and int? [:> 18]]}}
   [:age ::age]])

(mg/generate Adult {:size 10, :seed 1})
(-> Adult
    (malli.edn/write-string)
    (malli.edn/read-string))


(m/decode int? "42" mt/string-transformer)
(m/decode int? 42 mt/string-transformer)

(m/encode int? 42 mt/string-transformer)
(m/encode int? "42" mt/string-transformer)

;; (dev/start! {:report (pretty/reporter)})

; => {:age 92}
(comment
  (foo-meta 1)
  (foo-meta "3")

  (pure-foo 2)
  (pure-foo "3")


  (foo-declare 1)
  (foo-declare "2")

  (try (foo-vector [1 2 3] "3")
       (catch Exception e (prn e)))


  (foo-vector 1 2)
  (foo-vector [:1 :2 "3"] 2)

  (foo-bar 1 2 [3 "4"] {:a :b})
  (insert-subsidy-push [] 1 "3")
  (insert-subsidy-push {} 1 "3")

  (insert-subsidy-info {:id   1
                        :area 100})
  (try (insert-subsidy-info {:id   1
                             :area 100})
       (catch Exception e (prn (me/error-message e))))

  (m/validate pants-schema {:id   1
                            :size nil
                            :size-alphabet "S"})
  
  (mg/generate pants-schema {:seed 2})
                            
  (m/validate [:maybe [:enum "S" "M"]] nil)
  (m/validate [:maybe int?] 2)
  (m/validate [:maybe int?] "2")

  (do-something-with-pants {:id   1
                            :size 90})


  (-> pants-schema
      (m/explain {:id 1})
      (me/humanize))



  (insert-subsidy-info {:id              1
                        :area            100
                        :area-ineqaulity "초과"}))
  
  

; vector syntax

(def non-empty-string
  (m/schema [:string {:min 1}]))

(m/schema? non-empty-string)

(m/validate non-empty-string "")

(m/validate non-empty-string "greenlabs")

(m/form non-empty-string)

; map syntax

(def non-empty-string-2
  (m/from-ast {:type       :string
               :properties {:min 1}}))

(m/schema? non-empty-string-2)

(m/validate non-empty-string-2 "")

(m/validate non-empty-string-2 "greenlabs")

(m/ast non-empty-string-2)

; 왜 2개의 syntax가 있는지?
; vector syntax가 성능 문제가 있어서 schema AST를 이용한 것이 훨씬 빨라서 만들었음

; bs

(def bs
  [:map
   [:id int?]
   [:description string?]
   [:area int?]])

(comment
  (m/validate bs {:id          1
                  :description "test desc"
                  :area        10})
  (-> bs
      (m/explain {:id          1
                  :description "test desc"
                  :area        "Abc"})
      (me/humanize)))
