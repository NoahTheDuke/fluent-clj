# fluent-clj

[![Clojars Project](https://img.shields.io/clojars/v/io.github.noahtheduke/fluent-clj.svg)](https://clojars.org/io.github.noahtheduke/fluent-clj)
[![cljdoc badge](https://cljdoc.org/badge/io.github.noahtheduke/fluent-clj)](https://cljdoc.org/d/io.github.noahtheduke/fluent-clj)

[Project Fluent](https://projectfluent.org/) is very cool. The available [Java](https://github.com/xyzsd/fluent) and [Javascript](https://github.com/projectfluent/fluent.js) packages are relatively easy to use through interop, but without a unified interface, it's hard to write consistent and testable code.

This library aims to smooth over those differences, making it easy to build your own translation system.

> [!NOTE]
> Requires Clojure 1.12 because new interop syntax is really nice. I'm not looking to support earlier Clojures at this time.

## Table of Contents

<!-- toc -->

- [Getting Started](#getting-started)
- [Usage](#usage)
- [Formatter](#formatter)
- [Extended example](#extended-example)

<!-- tocstop -->

## Getting Started

Add it to your deps.edn or project.clj:

```clojure
{:deps {io.github.noahtheduke/fluent-clj {:mvn/version "0.1.0"}}}
```

## Usage

```clojure
(require '[noahtheduke.fluent :as i18n])

;; A resource is a string of Fluent messages, terms, etc.
(def sample-resource
  "
hello = Hello world!
welcome = Welcome, {$user}!
email-cnt = {$cnt ->
    [one] {$cnt} email
    *[other] {$cnt} emails
}")
=> #'sample-resource

;; Bundles are native objects that hold the processed Fluent strings. They can be interacted with through interop but generally you only need the provided api functions.
(def bundle (i18n/build "en" sample-resource))
=> #'bundle

;; Message ids can be specified with strings, keywords, or symbols
(i18n/format bundle :hello)
=> "Hello world!"

;; Argument maps are just plain clojure maps
(i18n/format bundle "welcome" {:user "Noah"})
=> "Welcome, Noah!"

;; And their keys can be strings, keywords, or symbols as well
(i18n/format bundle :email-cnt {"cnt" 1})
=> "1 email"

(i18n/format bundle "email-cnt" {:cnt 2})
=> "2 emails"
```

## Formatter

A no-options formatter for Fluent Files is included, available as a `-X` function. To use it, call `clojure -X noahtheduke.fluent.utils/fmt :file "corpus/example.ftl"` to format a single file or `clojure -X noahtheduke.fluent.utils/fmt :dir "corpus"` to format all `.ftl` files in the given directory.

## Extended example

I built this library for a website that uses [Reagent](https://reagent-project.github.io), so I'll share how we do it there.

The translations are stored as both raw text and fluent bundles. During app start, `(load-dictionary! "resources/public/i18n")` is called to load all of the Fluent files. Then on app load, the client sets a `GET` request to the server for the desired translation, and stores it locally with `insert-lang!`. The function `tr` (below) is modeled after [Tempura](https://github.com/taoensso/tempura)'s api, where a fallback value can be passed in with the desired translation: `(i18n/tr :hello)` without fallback, `(i18n/tr [:hello "sup nerd"])` with fallback.

Done in a `.cljc` like this, translations can be tested in a normal clojure repl.

```clojure
(ns example.i18n
  (:require
   [noahtheduke.fluent :as fluent]
   #?(:cljs
     [reagent.core :as r])))

(defonce fluent-dictionary
  #?(:clj (atom nil)
     :cljs (r/atom {})))

(defn insert-lang! [lang content]
  (swap! fluent-dictionary assoc lang {:content content
                                       :ftl (fluent/build lang content)}))

#?(:clj
   (defn load-dictionary!
     [dir]
     (let [langs (->> (io/file dir)
                      (file-seq)
                      (filter #(.isFile ^java.io.File %))
                      (filter #(str/ends-with? (str %) ".ftl"))
                      (map (fn [^java.io.File f]
                             (let [n (str/replace (.getName f) ".ftl" "")
                                   content (slurp f)]
                               [n content]))))
           errors (volatile! [])]
       (doseq [[lang content] langs]
         (try (insert-lang! lang content)
              (catch Throwable t
                (println "Error inserting i18n data for" lang)
                (println (ex-message t))
                (vswap! errors conj lang))))
       @errors)))

(defn get-content
  [lang]
  (get-in @fluent-dictionary [lang :content]))

(defn get-bundle
  [lang]
  (get-in @fluent-dictionary [lang :ftl]))

(defn get-translation
  [bundle id params]
  (when bundle
    (fluent/format bundle id params)))

(defn tr
  ([lang resource] (tr lang resource nil))
  ([lang resource params]
   (let [resource (if (vector? resource) resource [resource])
         [id fallback] resource]
     (or (get-translation (get-bundle lang) id params)
         ;; You can choose to use the fallback directly or use a translation from a different language.
         ;; Project Fluent's javascript implementation has language negotiation libraries already so those can be used directly as desired.
         fallback
         (get-translation (get-bundle "en") id params)))))
```
