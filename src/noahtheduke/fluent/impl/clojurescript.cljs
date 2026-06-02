; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.fluent.impl.clojurescript
  {:no-doc true}
  (:require
   ["@fluent/bundle" :refer [FluentBundle FluentDateTime FluentNone
                             FluentNumber FluentResource]]
   [goog.object :as o]
   [clojure.string :as str]))

(defn custom-functions [_locale-str]
  {:NUMBER (fn NUMBER
             [[arg] opts]
             (cond
               (instance? FluentNone arg) (FluentNone. (str "NUMBER(" (.valueOf arg) ")"))
               (instance? FluentNumber arg) (FluentNumber. (.valueOf arg) opts)
               (instance? FluentDateTime arg) (FluentDateTime. (.valueOf arg) opts)
               :else (throw (js/TypeError. (str "Invalid argument to NUMBER: " arg)))))})

(defn add-resource
  [^FluentBundle bundle resource]
  (let [ftl-res (FluentResource. resource)
        errors (.addResource bundle ftl-res)]
    (when (seq errors)
      (js/console.log (ex-info "Errors adding resources:" {:resource resource
                                                           :errors errors})))
    bundle))

(defn build
  [locale-str resource]
  (let [locale (js/Intl.Locale. locale-str)
        bundle (FluentBundle. locale
                              (clj->js {:functions (custom-functions locale-str)
                                        :useIsolating false}))]
    (add-resource bundle resource)))


(defn ^:private get-pattern
  [^FluentBundle bundle k]
  (let [id (clj->js k)
        ?patt (if-let [idx (str/index-of id ".")]
                (let [id' (subs id 0 idx)
                      attr (subs id (unchecked-inc idx))]
                  (when (str/index-of attr ".")
                    (throw (ex-info (str "Invalid attribute provided: " attr)
                                    {:id id :attr attr})))
                  (o/get (.-attributes (.getMessage bundle id')) attr))
                (.getMessage bundle id))]
    ?patt))

(defn format
  ([bundle id] (format bundle id nil))
  ([^FluentBundle bundle id args]
   (when bundle
     (when-let [message (get-pattern bundle id)]
       (when-let [v (when message
                      (or (.-value message)
                          message))]
         (try (.formatPattern bundle v (clj->js args))
              (catch js/ReferenceError e
                (let [msg (-> (ex-message e)
                              (str/replace "Unknown variable: " "Missing expected keys: "))]
                  (set! (.-message e) msg)
                  (throw e)))))))))

(comment
  (let [input "hello-world = {NUMBER($percent, style: \"percent\")}"
        bundle (build "de" input)]
    (prn :prn (format bundle "hello-world" {:percent 0.89}))
    (println :println (format bundle "hello-world" {:percent 0.89}))))
