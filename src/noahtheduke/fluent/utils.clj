; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.fluent.utils 
  (:require
   [clojure.string :as str]
   [noahtheduke.fluent.impl.clojure :refer [parse]]
   [noahtheduke.fluent.pprint :refer [pprint-str]]
   [clojure.java.io :as io]) 
  (:import
   [fluent.bundle FluentResource]
   [java.io File]))

(defn safe-read-and-fmt
  ^String [s]
  (let [contents (-> s
                     (str/replace #"\\u([0-9A-F]{4})" "__FLUENT_CLJ__u$1")
                     (str/replace #"\\U([0-9A-F]{4})" "__FLUENT_CLJ__U$1"))
        ast (parse contents)
        _ (when-let [errors (seq (FluentResource/.errors ast))]
            (throw (ex-info (str "found errors: " (ex-message (last errors))) {:errors errors})))
        formatted-contents (-> (pprint-str ast)
                               (str/replace #"__FLUENT_CLJ__u([0-9A-F]{4})" "\\\\u$1")
                               (str/replace #"__FLUENT_CLJ__U([0-9A-F]{4})" "\\\\U$1")
                               (str/trim)
                               (str "\n"))]
    formatted-contents))

(defn fmt-dir
  "Format all files in a directory"
  [{:keys [dir]}]
  (doseq [f (->> (io/file (name dir))
                 (file-seq)
                 (filter #(File/.isFile %))
                 (filter #(str/ends-with? (str %) ".ftl")))
          :let [formatted-file (safe-read-and-fmt (slurp f))]]
    (spit f formatted-file)))

(comment
  (fmt-dir {:dir "corpus"}))
