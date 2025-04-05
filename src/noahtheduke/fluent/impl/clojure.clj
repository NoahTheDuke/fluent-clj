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
   (fluent.syntax.AST Pattern)
   (fluent.syntax.parser FTLParser FTLStream)
   (java.util
    ArrayList
    Locale
    Map
    Optional)))

(set! *warn-on-reflection* true)

(defn parse
  "Undocumented, useful for tests"
  {:no-doc true}
  ^FluentResource [^String s]
  (FTLParser/parse (FTLStream/of s) false))

(defn- add-resource-impl
  ^FluentBundle [^FluentBundle$Builder builder ^Locale locale ^FluentResource ftl-res]
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
  ^FluentBundle [^FluentBundle bundle ^String resource]
  (let [locale (FluentBundle/.locale bundle)
        builder (-> (FluentBundle/builderFrom bundle)
                    (FluentBundle$Builder/.withFunctionFactory
                      CLDRFunctionFactory/INSTANCE))
        ftl-res (parse resource)]
    (add-resource-impl builder locale ftl-res)))

(defn build
  ^FluentBundle [^String locale-str ^String resource]
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

(defn format
  (^String [bundle id] (format bundle (k->str id) nil))
  (^String [^FluentBundle bundle id args]
   (when bundle
     (let [id (k->str id)
           args (if args (update-keys args k->str) {})
           pattern (Optional/.orElse (FluentBundle/.getMessagePattern bundle id) nil)
           errors (ArrayList.)]
       (if pattern
         (let [ret (FluentBundle/.formatPattern bundle ^Pattern pattern ^Map args ^ArrayList errors)]
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
                                                                        :args args})))))))

(comment
  (let [input "hello-world = {NUMBER($percent, style:\"percent\")}"
        bundle (build "en" input)]
    (println (format bundle "hello-world" {:ercent 0.8
                                           :style "percent"}))
    #_(println (format bundle :hello-user {:uer-name "Noah"})))
  )
