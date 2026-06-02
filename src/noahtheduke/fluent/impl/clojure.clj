; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.fluent.impl.clojure
  {:no-doc true}
  (:refer-clojure :exclude [format])
  (:require
   [clojure.core :as cc]
   [clojure.string :as str])
  (:import
   (fluent.bundle FluentBundle FluentBundle$Builder FluentResource)
   [fluent.bundle.resolver ReferenceException]
   (fluent.functions FluentFunctionException)
   (fluent.functions.cldr CLDRFunctionFactory)
   (fluent.syntax.parser FTLParser FTLStream)
   (java.util ArrayList Locale Optional)))

(set! *warn-on-reflection* true)

(defn parse
  "Undocumented, useful for tests"
  {:no-doc true}
  ^FluentResource [^String s]
  (FTLParser/parse (FTLStream/of s) false))

(defn- add-resource-impl
  ^FluentBundle [builder locale ftl-res]
  (when-let [errors (seq (FluentResource/.errors ftl-res))]
    (let [err (first errors)]
      (throw (ex-info (str "Error adding resource: " (ex-message err))
                      {:locale locale
                       :errors errors}
                      err))))
  (FluentBundle$Builder/.addResource builder ftl-res)
  (FluentBundle$Builder/.build builder))

(defn add-resource
  "Adds a new resource "
  ^FluentBundle [bundle resource]
  (let [locale (FluentBundle/.locale bundle)
        builder (-> (FluentBundle/builderFrom bundle)
                    (FluentBundle$Builder/.withFunctionFactory
                      CLDRFunctionFactory/INSTANCE))
        ftl-res (parse resource)]
    (add-resource-impl builder locale ftl-res)))

(defn build
  ^FluentBundle [locale-str resource]
  (let [locale (Locale/forLanguageTag locale-str)
        builder (FluentBundle/builder locale CLDRFunctionFactory/INSTANCE)
        ftl-res (parse resource)]
    (add-resource-impl builder locale ftl-res)))

(defn ^:private k->str
  [k]
  (cond (string? k) k
        (keyword? k) (str (symbol k))
        (symbol? k) (str k)
        :else (throw (ex-info (cc/format "Given wrong type: '%s', expected string or keyword" (type k))
                              {:key k}))))

(defn ^:private get-pattern
  [bundle k]
  (let [id (k->str k)
        ?patt (if-let [idx (str/index-of id ".")]
                (let [id' (subs id 0 idx)
                      attr (subs id (unchecked-inc idx))]
                  (when (str/index-of attr ".")
                    (throw (ex-info (str "Invalid attribute provided: " attr)
                                    {:id id :attr attr})))
                  (FluentBundle/.getAttributePattern bundle id' attr))
                (FluentBundle/.getMessagePattern bundle id))]
    (Optional/.orElse ?patt nil)))

(defn format
  (^String [bundle id] (format bundle (k->str id) nil))
  (^String [bundle id args]
   (when bundle
     (if-let [pattern (get-pattern bundle id)]
       (let [args (if args (update-keys args k->str) {})
             errors (ArrayList.)
             ret (FluentBundle/.formatPattern bundle pattern args errors)]
         (if (seq errors)
           (let [new-msg (mapv #(cond (instance? ReferenceException %)
                                      (-> (ex-message %)
                                          (str/replace "Unknown variable: " "Missing expected keys: "))
                                      (instance? FluentFunctionException %)
                                      (-> (ex-message %)
                                          ((fn [s] (str "Function " s)))))
                               errors)]
             (throw (ex-info (cc/format "Error(s) in id \"%s\": %s" id new-msg)
                             {:id id
                              :args args
                              :errors (vec errors)})))
           ret))
       (throw (ex-info (cc/format "Missing message for id: '%s'" id) {:id id
                                                                      :args args}))))))

(comment
  (let [input "
hello-world = {NUMBER($percent, style:\"percent\")}
    .goodbye = goodbye {$name}
"
        bundle (build "en" input)]
    (println (format bundle "hello-world.goodbye" {:name "noah"}))
    #_(println (format bundle :hello-user {:uer-name "Noah"})))
  )
