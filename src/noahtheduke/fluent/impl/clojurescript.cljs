(ns noahtheduke.fluent.impl.clojurescript
  (:require
   ["@fluent/bundle" :refer [FluentBundle FluentDateTime FluentNone
                             FluentNumber FluentResource]]))

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
      (println (ex-info "Errors adding resources:" {:resource resource
                                                  :errors errors})))
    bundle))

(defn build
  [locale-str resource]
  (let [locale (js/Intl.Locale. locale-str)
        bundle (FluentBundle. locale
                              (clj->js {:functions (custom-functions locale-str)
                                        :useIsolating false}))]
    (add-resource bundle resource)))

(defn format
  ([bundle id] (format bundle id nil))
  ([^FluentBundle bundle id args]
   (let [id (clj->js id)
         message (.getMessage bundle id)]
     (when-let [v (and message (.-value message))]
       (try (.formatPattern bundle v (clj->js args))
            (catch js/ReferenceError e
              (set! (.-message e) (str "Error in id: '" id "'"))
              (println e)))))))

(comment
  (let [input "hello-world = {NUMBER($percent, style: \"percent\")}"
        bundle (build "de" input)]
    (prn :prn (format bundle "hello-world" {:percent 0.89}))
    (println :println (format bundle "hello-world" {:percent 0.89}))))
