(ns metabase.plugins
  (:require [clojure.string :as str]
            [metabase.config :as config]
            [metabase.plugins
             [classloader :as classloader]
             [delayed-driver :as delayed-driver]
             [files :as files]
             [initialize :as init]]
            [yaml.core :as yaml])
  (:import java.nio.file.Path))

(defn- plugins-dir-filename ^String []
  (or (config/config-str :mb-plugins-dir)
      (str (System/getProperty "user.dir") "/plugins")))

(defn- ^Path plugins-dir
  "Get a `Path` to the Metabase plugins directory, creating it if needed."
  []
  (let [path (files/get-path (plugins-dir-filename))]
    (files/create-dir-if-not-exists! path)
    path))

(defn- extract-system-modules! []
  (let [plugins-path (plugins-dir)]
    (files/with-open-path-to-resource [modules-path "modules"]
      (files/copy-files-if-not-exists! modules-path plugins-path))))


;;; +----------------------------------------------------------------------------------------------------------------+
;;; |                                               Initialize Plugin                                                |
;;; +----------------------------------------------------------------------------------------------------------------+

(defn- add-to-classpath! [^Path jar-path]
  (classloader/add-url-to-classpath! (-> jar-path .toUri .toURL)))

(defn- plugin-info [^Path jar-path]
  (some-> (files/slurp-file-from-archive jar-path "metabase-plugin.yaml")
          yaml/parse-string))

(defn- init-plugin! [^Path jar-path]
  (when-let [{init-steps :init, {:keys [delay-loading]} :driver, :as info} (plugin-info jar-path)]
    (if delay-loading
      (delayed-driver/register-delayed-load-driver! info)
      (init/initialize! init-steps))))


;;; +----------------------------------------------------------------------------------------------------------------+
;;; |                                                 load-plugins!                                                  |
;;; +----------------------------------------------------------------------------------------------------------------+

(defn- plugins-paths []
  (for [^Path path (files/files-seq (plugins-dir))
        :when      (and (files/regular-file? path)
                        (files/readable? path)
                        (str/ends-with? (.getFileName path) ".jar"))]
    path))

(defn- add-plugins-to-classpath! [paths]
  (doseq [path paths]
    (add-to-classpath! path)))

(defn- init-plugins! [paths]
  (doseq [path paths]
    (init-plugin! path)))

(defn load-plugins! []
  (extract-system-modules!)
  (let [paths (plugins-paths)]
    (add-plugins-to-classpath! paths)
    (init-plugins! paths)))
