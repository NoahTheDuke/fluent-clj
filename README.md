# fluent-clj

[Project Fluent](https://projectfluent.org/) is very cool. Each of the available packages is relatively easy to use through interop, but without a unified interface, it's hard to write consistent and testable code.

## Example

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
(def bundle (i18n/build "en" simple-resource))
=> #'bundle

;; Message ids can be specified with strings, keywords, or symbols
(i18n/format bundle :hello)
=> "Hello world!"

;; Argument maps are just plain clojure maps
(i18n/format bundle "welcome" {:user "Noah"})
=> "Welcome, Noah!"

;; And their keys can be strings, keywords, or symbols as well
(i18n/format bundle "email-cnt" {"cnt" 1})
=> "1 email"

(i18n/format bundle "email-cnt" {:cnt 2})
=> "2 emails"
```
