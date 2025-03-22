(ns noahtheduke.fluent
  #?(:clj (:refer-clojure :exclude [format]))
  (:require
   #?(:clj [noahtheduke.fluent.impl.clojure :as impl]
      :cljs [noahtheduke.fluent.impl.clojurescript :as impl]))
  #?(:clj (:import
           (fluent.bundle FluentBundle))))

(defn build
  "Create a new FluentBundle for the given locale with the provided resource."
  #?(:clj ^FluentBundle [locale-str resource]
     :cljs [locale-str resource])
  (impl/build locale-str resource))

(defn add-resource
  "Creates a copy of the given bundle and adds the provided string resource to it."
  [bundle resource]
  (impl/add-resource bundle resource))

(defn format
  "Convert pattern "
  ([bundle message] (impl/format bundle message nil))
  ([bundle message args] (impl/format bundle message args)))
