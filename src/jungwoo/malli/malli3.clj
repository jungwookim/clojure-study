(ns jungwoo.malli.malli3
  (:require [malli.core :as m]
            [malli.dev :as dev]
            [malli.error :as me]
            [malli.generator :as mg]
            [malli.edn :as edn]
            [malli.transform :as mt]
            [malli.dev.pretty :as pretty]
            [malli.provider :as mp]
            [malli.util :as mu]
            [clojure.pprint :refer [pprint]]))

; vector 방식
(def non-empty-string-by-vector
  (m/schema [:string {:min 1}]))

(m/schema? non-empty-string-by-vector)

(m/validate non-empty-string-by-vector "")

(m/validate non-empty-string-by-vector "green")

(m/form non-empty-string-by-vector)

; map 방식
(def non-empty-string-by-map
  (m/from-ast {:type :string
               :properties {:min 1}}))

(m/schema? non-empty-string-by-map)

(m/validate non-empty-string-by-map "")

(m/validate non-empty-string-by-map "green")

(m/ast non-empty-string-by-map)

; 기본적인 validation
(m/validate [:map
             [:x int?]
             [:y int?]] {:x 1
                         :y 2})

(m/validate [:sequential any?] (list "this" 'is :number 42))

(m/validate [:vector int?] [1 2 3]) ; using raw-schema index

(m/validate (m/schema [:vector int?]) [1 2 3]) ; using Schema instance

(m/validate [:vector int?] (list 1 2 3))

; fixed length vector
(m/validate [:tuple keyword? string? number?] [:green "labs" 42])



(m/validate [:repeat {:min 2, :max 4} int?] [1])
(m/validate [:repeat {:min 2, :max 4} int?] [1 2])
(m/validate [:repeat {:min 2, :max 4} int?] [1 2 3 4]) ; => true ; :max is inclusive
(m/validate [:repeat {:min 2, :max 4} int?] [1 2 3 4 5])


; string schemas
(m/validate string? "green") ; using a predicate

(m/validate :string "labs") ; using :string schema
;; => true

(m/validate [:string {:min 1, :max 4}] "")
;; => false

; maybe schemas
(m/validate [:maybe string?] "green")

(m/validate [:maybe string?] nil)

(m/validate [:maybe string?] :green)

; fn schemas
(def my-schema
  [:and
   [:map
    [:x int?]
    [:y int?]]
   [:fn (fn [{:keys [x y]}] (> x y))]])
   
(m/validate my-schema {:x 1, :y 0})

(m/validate my-schema {:x 1, :y 2})

(def subsidy-schema
  [:and
   [:map {:closed true}
    [:id int?]
    [:area {:optional true} [:maybe :int]]
    [:area-inequality {:optional true} [:maybe [:enum "초과" "미만"]]]]
   [:fn {:error/message "area and area-inequality는 nil-match가 되어야 함함"}
    '(fn [{:keys [area area-inequality]}]
       (or (and (nil? area) (nil? area-inequality))
           (and (some? area) (some? area-inequality))))]])

(m/validate subsidy-schema {:id            1
                            :area          100
                            :area-inequality nil})
                          

; Errors
(-> subsidy-schema
    (m/explain {:id 1
                :area nil
                :area-inequality "초과"})
    me/humanize)

; Value transformations
(m/decode int? "42" mt/string-transformer)
; 42

(m/encode int? 42 mt/string-transformer)
; "42"

(m/decode
 subsidy-schema
 {:id      "Lillan"
  :area    100
  :area-inequality "초과"}
 mt/json-transformer)

(m/encode
 subsidy-schema
 {:id              "Lillan"
  :area            "100"
  :area-inequality "초과"}
 (mt/key-transformer {:encode name}))
(name :3)

; 종합 예제
(m/encode
 [:map {:default {}}
  [:a [int? {:default 1}]]
  [:b [:vector {:default [1 2 3]} int?]]
  [:c [:map {:default {}}
       [:x [int? {:default 42}]]
       [:y int?]]]
  [:d [:map
       [:x [int? {:default 42}]]
       [:y int?]]]
  [:e int?]]
 nil
 (mt/transformer
  mt/default-value-transformer
  mt/string-transformer))

; Schema transformation
(def Crop
  [:map
   [:id int?]
   [:name string?]])

(-> Crop
    (mu/assoc-in [:provider :id] int?)
    (mu/assoc-in [:provider :country] [:enum "한국" "미국"]))

; Infer Schemas
(def bulk-sale-excel-data [{:online-market-name            "NAVER"
                            :offline-market-name           nil
                            :estimated-purchase-price-max  102
                            :number                        "12-345-678"
                            :name                          "이름없음"
                            :estimated-purchase-price-min  101
                            :type                          "INDIVIDUAL"
                            :estimated-seller-earning-rate 1.2
                            :region                        ""
                            :gl-crop-name                  "한우(암)"
                            :preferred-grade               "중"
                            :experience-year-type          1
                            :url                           "https://moondaddi.dev"
                            :lastyear-income               30121
                            :phone-num                     "01054585037"
                            :code-name-new                 "한우(암)"
                            :delivery-company-name         "우체국택배"
                            :preferred-quantity            "4kg"
                            :progress                      3
                            :created-at                    "2021-11-30T09:07:14"}
                           {:online-market-name            "NAVER"
                            :estimated-purchase-price-max  102
                            :number                        "12-345-678"
                            :name                          "이름없음"
                            :estimated-purchase-price-min  "101"
                            :type                          "INDIVIDUAL"
                            :estimated-seller-earning-rate 1.2
                            :region                        ""
                            :gl-crop-name                  "한우(암)"
                            :preferred-grade               "중"
                            :experience-year-type          1
                            :url                           "https://moondaddi.dev"
                            :lastyear-income               30121
                            :phone-num                     "01054585037"
                            :code-name-new                 "한우(암)"
                            :delivery-company-name         "우체국택배"
                            :preferred-quantity            "4kg"
                            :progress                      "REJECTED"
                            :created-at                    "2021-11-30T09:07:14"}])

(-> bulk-sale-excel-data
    mp/provide
    pprint)

; Value generation
(mg/sample int? {:size 5 :seed 40})
(mg/generate subsidy-schema {:seed 2})

; Function Schemas
(defn foo-meta
  "schema via var metadataz"
  {:malli/schema [:=> [:cat :int] :int]}
  [x]
  (inc x))

(m/=> bar-declare [:=> [:cat :int] :int])
(defn bar-declare
  "schema via separate declaration"
  [x]
  (inc x))

;; (dev/start!)

(comment
  (foo-meta 1) ; ok
  (foo-meta "1") ; clj-kondo가 빨간 줄 그어줌

  (bar-declare 1) ; ok
  (bar-declare "1"))
  
(require '[reitit.core :as r])

(def router
  (r/router
    ["/:company/users/:user-id" ::user-view]))

(r/match-by-path router "/greenlabs/users/123")

; 파라미터 coercion 사용 예시, 아래 4가지가 필요

;; 1. 라우트에 coercion 정의
;; 2. 파라미터에 type 정의
;; 3. coercer 컴파일
;; 4. 적용

(require '[reitit.coercion.malli])
(require '[reitit.coercion :as coercion])


(def router
  (r/router
    ["/:company/users/:user-id" {:name ::user-view
                                 :coercion reitit.coercion.malli/coercion
                                 :parameters {:path [:map
                                                     [:company string?]
                                                     [:user-id int?]]}}]
   {:compile coercion/compile-request-coercers}))

(r/match-by-path router "/greenlabs/users/123")

(coercion/coerce!
 (r/match-by-path router "/greenlabs/users/123"))

#_(coercion/coerce!
 (r/match-by-path router "/greenlabs/users/greeny"))

; ring에서 coercion 사용하기

;; 1. routes에 대한 reitit.coercion/Coercion 정의
;; 2. request(parameter)와 response에 대한 타입 정의하기
;; 3. Coercion Middleware 마운트 하기
;; 4. handler/middleware에서 coerced paramters 사용하기

(require '[reitit.ring.coercion :as rrc])
(require '[reitit.coercion.schema])
(require '[reitit.ring :as ring])

(def app
  (ring/ring-handler
    (ring/router
      ["/api"
       ["/ping" {:name ::ping
                 :get (fn [_]
                        {:status 200
                         :body "pong"})}]
       ["/plus/:z" {:name ::plus
                    :post {:coercion reitit.coercion.malli/coercion
                           :parameters {:query [:map
                                                [:x int?]]
                                        :body [:map
                                               [:y int?]]
                                        :path [:map
                                               [:z int?]]}
                           :responses {200 {:body [:map
                                                   [:total int?]]}}
                           :handler (fn [{:keys [parameters]}]
                                      (let [total (+ (-> parameters :query :x)
                                                     (-> parameters :body :y)
                                                     (-> parameters :path :z))]
                                        {:status 200
                                         :body {:total total}}))}}]]
      {:data {:middleware [rrc/coerce-exceptions-middleware
                           rrc/coerce-request-middleware
                           rrc/coerce-response-middleware]}})))

(app {:request-method :post
      :uri            "/api/plus/3"
      :query-params   {"x" "1"}
      :body-params    {:y 2}})

(app {:request-method :post
      :uri            "/api/plus/3"
      :query-params   {"x" "abba"}
      :body-params    {:y 2}})


; pretty printing errors
(require '[reitit.ring :as ring])
(require '[reitit.ring.coercion :as rrc])
(require '[reitit.coercion.malli :as rcm])


(defn ^:private encode-error
  [{:keys [errors]}]
  (let [{:keys [message path value]} (first errors)]
    (format "problem: '%s'. expected: '%s' actual: '%s'" message path value)))

(def app
  (ring/ring-handler
   (ring/router
    ["/plus"
     {:get {:parameters {:query [:map
                                 [:x int?]
                                 [:y int?]]}
            :responses  {200 {:body [:map
                                     [:total pos-int?]]}}
            :handler    (fn [{{{:keys [x y]} :query} :parameters}]
                          {:status 200
                           :body   {:total (+ x y)}})}}]
    {:data {:coercion   (rcm/create (assoc rcm/default-options :encode-error encode-error))
            :middleware [rrc/coerce-exceptions-middleware
                         rrc/coerce-request-middleware
                         rrc/coerce-response-middleware]}})))

(app
 {:uri            "/plus"
  :request-method :get
  :query-params   {"x" "1"
                   "y" "fail"}})

(app
 {:uri            "/plus"
  :request-method :get
  :query-params   {"x" "1"
                   "y" "-2"}})


(def resolver-settings-schema
  [:map {:closed true}
   [:auth? {:optional true} boolean?]
   [:kebab-case? {:optional true} boolean?]
   [:return-camel-case? {:optional true} boolean?]
   [:required-keys-in-parent {:optional true} vector?]])

(def resolvers-map-schema
  "resolvers 설정에 필요한 스키마 정의"
  [:map {:closed true}
   [:target-ns symbol?]
   [:resolvers [:map
                [:resolve-connection {:optional true} [:map {:closed true}
                                                       [:settings {:optional true} resolver-settings-schema]
                                                       [:node-type keyword?]
                                                       [:db-key keyword?]
                                                       [:post-process-rows symbol?]
                                                       [:table-fetcher symbol?]
                                                       [:pre-process-arguments symbol?]]]
                [:resolve-by-fk {:optional true} [:map {:closed true}
                                                  [:settings {:optional true} resolver-settings-schema]
                                                  [:node-type keyword?]
                                                  [:db-key keyword?]
                                                  [:post-process-rows symbol?]
                                                  [:superfetcher symbol?]
                                                  [:reversed-fk-name keyword?]]]
                [:resolve-create-one {:optional true} [:map {:closed true}
                                                       [:settings {:optional true} resolver-settings-schema]
                                                       [:node-type keyword?]
                                                       [:db-key keyword?]
                                                       [:table-fetcher symbol?]
                                                       [:insert-fn symbol?]]]
                [:resolve-update-one {:optional true} [:map {:closed true}
                                                       [:settings {:optional true} resolver-settings-schema]
                                                       [:node-type keyword?]
                                                       [:db-key keyword?]
                                                       [:table-fetcher symbol?]
                                                       [:update-fn symbol?]]]
                [:resolve-delete-one {:optional true} [:map {:closed true}
                                                       [:settings {:optional true} resolver-settings-schema]
                                                       [:node-type keyword?]
                                                       [:db-key keyword?]
                                                       [:table-fetcher symbol?]
                                                       [:delete-fn symbol?]]]]]])


(comment
  (-> resolvers-map-schema
    (m/explain '{:target-ns farmmorning-bridge.graphql.resolver.bulk-sale-application
                :resolvers {:node-resolver                             {:node-type         :bulk-sale-application
                                                                        :db-key            :farmmorning-db
                                                                        :table-fetcher     farmmorning-bridge.graphql.db.bulk-sale-application/fetch
                                                                        :post-process-rows farmmorning-bridge.graphql.resolver.bulk-sale-application/post-process-rows}
                            :resolve-connection                        {:settings              {:auth? true}
                                                                        :node-type             :bulk-sale-application
                                                                        :db-key                :farmmorning-db
                                                                        :table-fetcher         farmmorning-bridge.graphql.db.bulk-sale-application/fetch
                                                                        :pre-process-arguments clojure.core/identity
                                                                        :post-process-rows     farmmorning-bridge.graphql.resolver.bulk-sale-application/post-process-rows}
                            :resolve-by-fk                             {:settings          {:auth? true}
                                                                        :node-type         :bulk-sale-application
                                                                        :db-key            :farmmorning-db
                                                                        :superfetcher      farmmorning-bridge.graphql.superfetcher.bulk-sale-application/->Fetch
                                                                        :post-process-rows farmmorning-bridge.graphql.resolver.bulk-sale-application/post-process-rows
                                                                        :reversed-fk-name  :bulk-sale-application-id}
                            :resolve-connection-by-farmmorning-user-id {:settings          {:auth? true}
                                                                        :node-type         :bulk-sale-application
                                                                        :superfetcher      farmmorning-bridge.graphql.superfetcher.bulk-sale-application/->FetchByFarmmorningUserId
                                                                        :post-process-rows farmmorning-bridge.graphql.resolver.bulk-sale-application/post-process-rows}
                            :resolve-connection-by-staff-id            {:settings          {:auth? true}
                                                                        :node-type         :bulk-sale-application
                                                                        :superfetcher      farmmorning-bridge.graphql.superfetcher.bulk-sale-application/->FetchByStaffId
                                                                        :post-process-rows farmmorning-bridge.graphql.resolver.bulk-sale-application/post-process-rows}}}
)
    me/humanize))