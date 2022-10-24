(ns jungwoo.reitit.test-route)


(def ^:private ok 200)

(defn ^:private test
  [app-config]
  (fn [req]
    {:status ok :body {:hello (-> req :parameters :query :id)}}))

;; PUBLIC API

(defn routes
  [app-config]
  ["/foo"
   {:get {:handler (test app-config)
          :parameters {:query [:map [:id string?]]}}}])
