(ns jungwoo.reitit.home-route)

(def ^:private ok 200)

(defn ^:private home
  [app-config]
  (fn [_]
    {:status ok
     :body   "Hello world"}))

;; PUBLIC API

(defn routes
  [app-config]
  ["/"
   {:get {:handler (home app-config)}}])
