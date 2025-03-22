(ns noahtheduke.fluent
  #?(:clj (:refer-clojure :exclude [format]))
  (:require
   #?(:clj [noahtheduke.fluent.impl.clojure :as impl]
      :cljs [noahtheduke.fluent.impl.clojurescript :as impl]))
  #?(:clj (:import
           (fluent.bundle FluentBundle))))

#?(:clj (set! *warn-on-reflection* true))

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
  "Find message for id, format it with any args, return it."
  ([bundle id] (impl/format bundle id nil))
  ([bundle id args] (impl/format bundle id args)))
