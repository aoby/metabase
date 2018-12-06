(ns metabase.plugins.initialize
  (:require [metabase.plugins.jdbc-proxy :as jdbc-proxy]))

(defmulti ^:private do-init-step!
  {:arglists '([m])}
  (comp keyword :step))

(defmethod do-init-step! :load-namespace [{nmspace :namespace}]
  (println "[INIT PLUGIN] LOADING NAMESPACE ::" nmspace)
  (require (symbol nmspace)))

(defmethod do-init-step! :register-jdbc-driver [{class-name :class}]
  (jdbc-proxy/create-and-register-proxy-driver! class-name))

(defn initialize! [init-steps]
  (doseq [step init-steps]
    (do-init-step! step)))
