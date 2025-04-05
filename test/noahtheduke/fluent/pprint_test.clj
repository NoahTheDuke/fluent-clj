(ns noahtheduke.fluent.pprint-test
  (:require
   [clojure.test :refer [deftest is]]
   [noahtheduke.fluent.impl.clojure :refer [parse]]
   [noahtheduke.fluent.pprint :as sut]
   [noahtheduke.fluent.test-helpers :refer [defcram]]
   [opticlj.core :as optic :refer [defoptic]]))

(defn parse-and-print
  [s]
  (sut/pprint (parse s)))

(defn parse-and-print-str
  [s]
  (sut/pprint-str (parse s)))

(defcram pprint-cram
  (with-out-str (parse-and-print "message = 1"))
  (with-out-str (parse-and-print "# hello\nmessage = 1"))
  (with-out-str (parse-and-print "game_mu-count = {$unused} of {$available} MU unused"))
  (with-out-str (parse-and-print "game_face-down-count = {$total ->
    [one] {$total} card, {$facedown} face-down.
    *[other] {$total} cards, {$facedown} face-down.
}")))

(defcram pprint-str-cram
  (parse-and-print-str "message = 1")
  (parse-and-print-str "# hello\nmessage = 1")
  (parse-and-print-str "game_mu-count = {$unused} of {$available} MU unused")
  (parse-and-print-str "game_face-down-count = {$total ->
    [one] {$total} card, {$facedown} face-down.
    *[other] {$total} cards, {$facedown} face-down.
}"))

(defcram big-russian-cram
  (parse-and-print-str (slurp "corpus/Russian_ru.ftl")))

(comment
  (optic/adjust-all!))

(deftest roundtripping-test
  (let [s "game_face-down-count = {$total ->
    [one] {$total} card, {$facedown} face-down.
    *[other] {$total} cards, {$facedown} face-down.
}"]
    (is (= (sut/pprint-str (parse s))
           (sut/pprint-str (parse (sut/pprint-str (parse s))))
           (sut/pprint-str (parse (sut/pprint-str (parse (sut/pprint-str (parse s))))))))))
