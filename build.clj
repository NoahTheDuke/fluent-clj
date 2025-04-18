; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns build
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [deps-deploy.deps-deploy :as dd]) 
  (:import
    [java.io File]))

(defn make-version []
  (str/trim (slurp "./resources/FLUENTCLJ_VERSION")))

(defn make-opts [opts]
  (let [version (make-version)
        lib 'io.github.noahtheduke/fluent-clj]
    (merge {:lib lib
            :version version
            :scm {:url "https://github.com/noahtheduke/fluent-clj"
                  :tag (str "v" version)}
            :main 'fluent-clj.main
            :basis (b/create-basis {:project "deps.edn"})
            :class-dir "target/classes"
            :jar-file (str (io/file "target" (format "%s-%s.jar" (name lib) version)))
            :uber-file (str (io/file "target" (format "%s-%s-standalone.jar" (name lib) version)))
            :src-dirs ["src"]
            :java-src-dirs ["src"]
            :resource-dirs ["resources"]
            :javac-opts ["--release" "17"]
            :pom-data [[:licenses
                        [:license [:name "MPL-2.0"] [:url "https://mozilla.org/MPL/2.0"]]]]}
           opts)))

(defn clean [opts]
  (let [opts (make-opts opts)]
    (b/delete {:path (:class-dir opts)})))

(defn copy-src [opts]
  (let [opts (make-opts opts)]
    (b/copy-dir {:src-dirs (concat (:src-dirs opts) (:resource-dirs opts))
                 :target-dir (:class-dir opts)})))

(defn javac [opts]
  (let [opts (make-opts opts)]
    (when (->> (map io/file (:java-src-dirs opts))
               (mapcat file-seq)
               (filter #(and (.isFile ^File %)
                             (str/ends-with? (str %) ".java")))
               (seq))
      (println "Compiling" (str/join ", " (:java-src-dirs opts)))
      (b/javac {:src-dirs (:java-src-dirs opts)
                :class-dir (:class-dir opts)
                :basis (:basis opts)
                :javac-opts (:javac-opts opts)}))))

(defn jar [opts]
  (let [opts (make-opts opts)]
    (copy-src opts)
    (javac opts)
    (b/jar opts)
    (println "Created" (str (b/resolve-path (:jar-file opts))))))

(defn write-pom
  [opts]
  (let [opts (make-opts opts)]
    (b/write-pom opts)))

(defn uberjar [opts]
  (let [opts (make-opts opts)]
    (copy-src opts)
    (javac opts)
    (b/write-pom opts)
    (b/compile-clj opts)
    (b/uber opts)
    (println "Created" (str (b/resolve-path (:uber-file opts))))))

(defn deploy [opts]
  (let [opts (make-opts opts)]
    (clean opts)
    (copy-src opts)
    (javac opts)
    (b/write-pom opts)
    (b/jar opts)
    (dd/deploy {:installer :remote
                :artifact (b/resolve-path (:jar-file opts))
                :pom-file (b/pom-path opts)})))

(defn install [opts]
  (let [opts (make-opts opts)]
    (clean opts)
    (jar opts)
    (b/install opts)
    (println "Installed version" (:lib opts) (:version opts))))
