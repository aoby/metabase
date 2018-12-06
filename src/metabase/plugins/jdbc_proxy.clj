(ns metabase.plugins.jdbc-proxy
  "JDBC proxy driver used for drivers added at runtime. DriverManager refuses to recognize drivers that weren't loaded
  by the system classloader, so we need to wrap our drivers loaded at runtime with a proxy class loaded at launch time."
  (:require [metabase.plugins.classloader :as classloader])
  (:import [java.sql Driver DriverManager]))

;;; -------------------------------------------------- Proxy Driver --------------------------------------------------

(defn- proxy-driver ^Driver [^Driver driver]
  (reify Driver
    (acceptsURL [_ url]
      (.acceptsURL driver url))
    (connect [_ url info]
     (println "[[proxy driver: connect]]" url info) ; NOCOMMIT
      (.connect driver url info))
    (getMajorVersion [_]
      (.getMajorVersion driver))
    (getMinorVersion [_]
      (.getMinorVersion driver))
    (getParentLogger [_]
      (.getParentLogger driver))
    (getPropertyInfo [_ url info]
      (.getPropertyInfo driver url info))
    (jdbcCompliant [_]
      (.jdbcCompliant driver))))

(defn create-and-register-proxy-driver!
  "Create a new JDBC proxy driver to wrap driver with `class-name`. Registers the driver with JDBC, and deregisters the
  class it wraps if that class is already registered."
  [^String class-name]
  (println "[INIT PLUGIN] REGISTERING JDBC DRIVER ::" class-name)
  (let [klass  (classloader/class-for-name class-name)
        driver (proxy-driver (.newInstance klass))]
    (DriverManager/registerDriver driver)

    ;; deregister the non-proxy version of the driver so it doesn't try to handle our URLs. Most JDBC drivers register
    ;; themseleves when the classes are loaded
    (doseq [driver (enumeration-seq (DriverManager/getDrivers))
            :when (instance? klass driver)]
      (println "DEREGISTER ::" driver)  ; NOCOMMIT
      (DriverManager/deregisterDriver driver))))
