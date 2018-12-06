(ns metabase.plugins.classloader
  (:require [clojure.tools.logging :as log]
            [dynapath.util :as dynapath]
            [metabase.util.i18n :refer [trs]])
  (:import clojure.lang.DynamicClassLoader
           java.net.URL))

(defn- top-level-dynamic-classloader
  (^DynamicClassLoader []
   (top-level-dynamic-classloader (.getContextClassLoader (Thread/currentThread))))
  (^DynamicClassLoader [^ClassLoader classloader]
   (println "classloader:" classloader) ; NOCOMMIT
   (if-not (instance? DynamicClassLoader classloader)
     (let [classloader (DynamicClassLoader. classloader)]
       (.setContextClassLoader (Thread/currentThread) classloader)
       classloader)
     (let [parent (.getParent classloader)]
       (if (instance? DynamicClassLoader parent)
         (recur parent)
         classloader)))))

#_(defn- classloader
  ^DynamicClassLoader []
  (let [sysloader (ClassLoader/getSystemClassLoader)]
    (if-not (instance? DynamicClassLoader sysloader)
      (log/error (trs "Error: System classloader is not an instance of clojure.lang.DynamicClassLoader.")
                 (trs "Make sure you start Metabase with {0}"
                      "-Djava.system.class.loader=clojure.lang.DynamicClassLoader"))
      sysloader)))

(defn class-for-name ^Class [^String classname]
  (Class/forName classname (boolean :initialize) (top-level-dynamic-classloader)))

(defn add-url-to-classpath! [^URL url]
  (assert (dynapath/add-classpath-url (top-level-dynamic-classloader) url))
  (log/info (metabase.util/format-color 'blue (trs "Added {0} to classpath" url))))
