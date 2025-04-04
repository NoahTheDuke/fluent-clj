(ns noahtheduke.fluent.pprint-test
  (:require [clojure.test :refer [deftest is]]
            [opticlj.core :as optic :refer [defoptic]]
            [noahtheduke.fluent.pprint :as sut]
            [noahtheduke.fluent.impl.clojure :refer [parse]]))

(defn parse-and-print
  [s]
  (sut/pprint (parse s)))

(defn parse-and-print-str
  [s]
  (sut/pprint-str (parse s)))

(defoptic ::pprint
  (with-out-str
    [(parse-and-print "message = 1")
     (parse-and-print "# hello\nmessage = 1")
     (parse-and-print "game_mu-count = {$unused} of {$available} MU unused")
     (parse-and-print "game_face-down-count = {$total ->
    [one] {$total} card, {$facedown} face-down.
    *[other] {$total} cards, {$facedown} face-down.
}")]))

(defoptic ::pprint-str
  [(parse-and-print-str "message = 1")
   (parse-and-print-str "# hello\nmessage = 1")
   (parse-and-print-str "game_mu-count = {$unused} of {$available} MU unused")
   (parse-and-print-str "game_face-down-count = {$total ->
    [one] {$total} card, {$facedown} face-down.
    *[other] {$total} cards, {$facedown} face-down.
}")])

(comment
  (optic/adjust-all!))

(deftest optics-test
  (is (optic/ok? (optic/review!))))

(deftest roundtripping-test
  (let [s "game_face-down-count = {$total ->
    [one] {$total} card, {$facedown} face-down.
    *[other] {$total} cards, {$facedown} face-down.
}"]
    (is (= (sut/pprint-str (parse s))
           (sut/pprint-str (parse (sut/pprint-str (parse s))))
           (sut/pprint-str (parse (sut/pprint-str (parse (sut/pprint-str (parse s))))))))))
