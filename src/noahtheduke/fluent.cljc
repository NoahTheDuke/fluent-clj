; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.fluent
  #?(:clj (:refer-clojure :exclude [format]))
  (:require
   #?(:clj [noahtheduke.fluent.impl.clojure :as impl]
      :cljs [noahtheduke.fluent.impl.clojurescript :as impl])))

(defn build
  "Create a new FluentBundle for the given locale with the provided resource."
  [locale-str resource]
  (impl/build locale-str resource))

(defn add-resource
  "Creates a copy of the given bundle and adds the provided string resource to it."
  [bundle resource]
  (impl/add-resource bundle resource))

(defn format
  "Find message for id, format it with any args, return it."
  ([bundle id] (impl/format bundle id nil))
  ([bundle id args] (impl/format bundle id args)))
