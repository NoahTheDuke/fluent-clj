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
  ^String [contents]
  (let [ast (parse contents)]
    (when-let [errors (seq (FluentResource/.errors ast))]
      (throw (ex-info (str "found errors: " (ex-message (last errors))) {:errors errors})))
    (-> (pprint-str ast)
        (str/trim)
        (str "\n"))))

(defn fmt
  "Format files in :dir or single file at :file. No-op if given neither."
  [{:keys [dir file]}]
  (cond
    dir (doseq [f (->> (io/file (name dir))
                       (file-seq)
                       (filter #(File/.isFile %))
                       (filter #(str/ends-with? (str %) ".ftl")))
                :let [formatted-file (safe-read-and-fmt (slurp f))]]
          (spit f formatted-file))
    file (as-> (slurp file) $
           (safe-read-and-fmt $)
           (spit file $))))

(comment
  (fmt {:dir "corpus"}))
