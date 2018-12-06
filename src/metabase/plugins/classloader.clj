(ns metabase.plugins.classloader
  (:require [clojure.tools.logging :as log]
            [dynapath.util :as dynapath]
            [metabase.util.i18n :refer [trs]])
  (:import clojure.lang.DynamicClassLoader
           java.net.URL))

(defn- classloader
  ^DynamicClassLoader []
  (let [sysloader (ClassLoader/getSystemClassLoader)]
    (if-not (instance? DynamicClassLoader sysloader)
      (log/error (trs "Error: System classloader is not an instance of clojure.lang.DynamicClassLoader.")
                 (trs "Make sure you start Metabase with {0}"
                      "-Djava.system.class.loader=clojure.lang.DynamicClassLoader"))
      sysloader)))

(defn class-for-name ^Class [^String classname]
  (Class/forName classname (boolean :initialize) (classloader)))

(defn add-url-to-classpath! [^URL url]
  (when-let [sysloader (classloader)]
    (dynapath/add-classpath-url sysloader url)
    (log/info (trs "Added {0} to classpath" url))))
