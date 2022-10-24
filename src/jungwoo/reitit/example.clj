(ns jungwoo.reitit.example)

(require '[muuntaja.core :as m])
(require '[reitit.ring :as ring])
(require '[reitit.coercion.spec])
(require '[reitit.ring.coercion :as coercion])
(require '[reitit.ring.middleware.muuntaja :as muuntaja])
(require '[reitit.ring.middleware.parameters :as parameters])
(require '[reitit.coercion.malli :as rcm])
(require '[reitit.spec :as rs])


(defn ^:private encode-error
  [{:keys [errors]
    :as   _coercion-failure}]
  (let [{:keys [message path value]} (first errors)]
    (format "What is going on?? There is a '%s'. I was expecting a value for '%s' but got '%s' instead!" message path value)))

(def app
  (ring/ring-handler
   (ring/router
    ["/api"
     ["/math" {:get {:parameters {:query [:map
                                          [:x int?]
                                          [:y int?]]}
                     :responses  {200 {:body [:map
                                              [:total int?]]}}
                     :handler    (fn [{{{:keys [x y]} :query} :parameters}]
                                   {:status 200
                                    :body   {:total 3}})}}]]
      ;; router data affecting all routes
    {:validate rs/validate
     :data     {:coercion   (rcm/create (assoc rcm/default-options :encode-error encode-error))
                :muuntaja   m/instance
                :middleware [muuntaja/format-middleware
                             parameters/parameters-middleware
                             coercion/coerce-exceptions-middleware
                             coercion/coerce-request-middleware
                             coercion/coerce-response-middleware]}})))

(def app2
  (ring/ring-handler
   (ring/router
    ["/api"
     ["/math" {:get {:parameters {:query {:x int?
                                          :y int?}}
                     :responses  {200 {:body {:total int?}}}
                     :handler    (fn [{{{:keys [x y]} :query} :parameters}]
                                   {:status 200
                                    :body   {:total "3"}})}}]]
      ;; router data affecting all routes
    {:data {:coercion   reitit.coercion.spec/coercion
            :muuntaja   m/instance
            :middleware [parameters/parameters-middleware
                         coercion/coerce-request-middleware
                         muuntaja/format-response-middleware
                         coercion/coerce-response-middleware]}})))

(comment
  (app {:request-method :get
        :uri            "/api/math"
        :query-params   {:x 1
                         :y 2}})
  (app2 {:request-method :get
        :uri            "/api/math"
        :query-params   {:x 1
                         :y "2"}}))