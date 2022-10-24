(ns jungwoo.reitit.router
  (:require [reitit.coercion.malli :as rcm]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.spec :as rs]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.cors :refer [wrap-cors]]
            [jungwoo.reitit.test-route :as test-route]
            [jungwoo.reitit.home-route :as home-route]
            [muuntaja.core :as m])
  (:import
   [org.eclipse.jetty.server Server]))


(def ^:private cors-middleware
  [wrap-cors
   :access-control-allow-origin [#".*"]
   :access-control-allow-methods [:delete :get :patch :post :put]])


(defn ^:private encode-error
  [{:keys [errors]
    :as   _coercion-failure}]
  (let [{:keys [message path value]} (first errors)]
    (format "What is going on?? There is a '%s'. I was expecting a value for '%s' but got '%s' instead!" message path value)))

(defn ^:private router
  [app-config]
  (ring/router
   [(merge ["/api"]
           (test-route/routes app-config))
    (home-route/routes app-config)]
   
   {:validate rs/validate
    :data     {:coercion   (rcm/create (assoc rcm/default-options :encode-error encode-error))
               :muuntaja   m/instance
               :middleware [cors-middleware
                            muuntaja/format-middleware
                            parameters/parameters-middleware
                            coercion/coerce-exceptions-middleware
                            coercion/coerce-request-middleware
                            coercion/coerce-response-middleware]}}))

;; CLIP Lifecycle Functions

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start
  [{:keys [runtime-config]
    :as   app-config}]
  (jetty/run-jetty
   (ring/ring-handler (router app-config) (ring/create-default-handler))
   (merge (:jetty runtime-config) {:allow-null-path-info true
                                   :send-server-version? false
                                   :send-date-header?    false
                                   :join?                false}))) ;; false so that we can stop it at the repl!

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn stop
  [^Server server]
  (.stop server) ; stop is async
  (.join server)) ; so let's make sure it's really stopped!
