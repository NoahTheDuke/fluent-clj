(ns noahtheduke.fluent.edn-test
  (:require [clojure.test :refer [deftest is]]
            [noahtheduke.fluent.edn :as sut]
            [matcher-combinators.test :refer [match?]]
            [noahtheduke.fluent.impl.clojure :refer [parse]]))

(defn parse-to-edn
  [s]
  (sut/fluent->edn (parse s)))

(deftest fluent->edn-test
  (is (match? {:type :ast/resource
               :entries
               [{:type :ast/message
                 :comment nil
                 :identifier {:type :ast/identifier
                              :key "message"}
                 :pattern {:type :ast/pattern
                           :elements [{:type :ast/text-element
                                       :value "1"}]}}]}
              (parse-to-edn "message = 1")))
  (is (match? {:type :ast/resource
               :entries
               [{:type :ast/message
                 :comment {:type :ast/comment
                           :text "hello"}
                 :identifier {:type :ast/identifier
                              :key "message"}
                 :pattern {:type :ast/pattern
                           :elements [{:type :ast/text-element
                                       :value "1"}]}}]}
              (parse-to-edn "# hello\nmessage = 1")))
  (is (match? {:type :ast/resource
               :entries
               [{:type :ast/message
                 :identifier {:type :ast/identifier
                              :key "game_mu-count"}
                 :pattern {:type :ast/pattern
                           :elements [{:type :ast/placeable
                                       :expression {:type :ast/variable-reference
                                                    :identifier {:type :ast/identifier
                                                                 :key "unused"}}}
                                      {:type :ast/text-element
                                       :value " of "}
                                      {:type :ast/placeable
                                       :expression {:type :ast/variable-reference
                                                    :identifier {:type :ast/identifier
                                                                 :key "available"}}}
                                      {:type :ast/text-element
                                       :value " MU unused"}]}}]}
              (parse-to-edn "game_mu-count = {$unused} of {$available} MU unused")))
  (is (match? {:type :ast/resource
               :entries
               [{:type :ast/message
                 :identifier {:type :ast/identifier
                              :key "game_face-down-count"}
                 :pattern {:type :ast/pattern
                           :elements
                           [{:type :ast/placeable
                             :expression {:type :ast/select-expression
                                          :selector {:type :ast/variable-reference
                                                     :identifier {:type :ast/identifier
                                                                  :key "total"}}
                                          :variants
                                          [{:type :ast/variant
                                            :default false
                                            :key {:type :ast/identifier
                                                  :key "one"}
                                            :value {:type :ast/pattern
                                                    :elements
                                                    [{:type :ast/placeable
                                                      :expression {:type :ast/variable-reference
                                                                   :identifier {:type :ast/identifier
                                                                                :key "total"}}}
                                                     {:type :ast/text-element
                                                      :value " card, "}
                                                     {:type :ast/placeable
                                                      :expression {:type :ast/variable-reference
                                                                   :identifier {:type :ast/identifier
                                                                                :key "facedown"}}}
                                                     {:type :ast/text-element
                                                      :value " face-down."}]}}
                                           {:type :ast/variant
                                            :default true
                                            :key {:type :ast/identifier
                                                  :key "other"}
                                            :value {:type :ast/pattern
                                                    :elements
                                                    [{:type :ast/placeable
                                                      :expression {:type :ast/variable-reference
                                                                   :identifier {:type :ast/identifier
                                                                                :key "total"}}}
                                                     {:type :ast/text-element
                                                      :value " cards, "}
                                                     {:type :ast/placeable
                                                      :expression {:type :ast/variable-reference
                                                                   :identifier {:type :ast/identifier
                                                                                :key "facedown"}}}
                                                     {:type :ast/text-element
                                                      :value " face-down."}]}}]}}]}}]}
              (parse-to-edn "game_face-down-count = {$total ->
    [one] {$total} card, {$facedown} face-down.
    *[other] {$total} cards, {$facedown} face-down.
}"))))

(deftest roundtripping-test
  (let [s "game_face-down-count = {$total ->
    [one] {$total} card, {$facedown} face-down.
    *[other] {$total} cards, {$facedown} face-down.
}"]
    (is (= (sut/fluent->edn (parse s))
           (->> (parse s)
                (sut/fluent->edn)
                (sut/edn->fluent)
                (sut/fluent->edn))
           (->> (parse s)
                (sut/fluent->edn)
                (sut/edn->fluent)
                (sut/fluent->edn)
                (sut/edn->fluent)
                (sut/fluent->edn))))))
