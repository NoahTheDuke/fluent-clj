; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.fmt-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [noahtheduke.fluent.fmt :as fmt]
   [noahtheduke.fluent.impl.clojure :as clj]))

(defn parse-and-fmt [s]
  (fmt/fmt->string (clj/parse s)))

(deftest fmt-test
  (let [in (->> ["### Coolest Fluent File around"
                 "## Subsection 1: Greetings"
                 "# A simple hello message"
                 "hello = Hello {$name}!"]
                (str/join "\n"))
        out (->> ["### Coolest Fluent File around"
                  "## Subsection 1: Greetings"
                  "# A simple hello message"
                  "hello = Hello {$name}!"]
                 (str/join "\n"))]
    (is (= out (parse-and-fmt in)))))
