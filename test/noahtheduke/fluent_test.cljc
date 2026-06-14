; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.fluent-test
  (:require
   [clojure.test :refer [deftest is testing]]
   #?(:cljs ["@fluent/bundle" :refer [FluentBundle]])
   [noahtheduke.fluent :as sut])
  #?(:clj (:import
           (clojure.lang ExceptionInfo)
           (fluent.bundle FluentBundle))))

#?(:clj (set! *warn-on-reflection* true))

(def simple-resource
  "
hello = Hello world!
welcome = Welcome, {$user}!
email-cnt = {$cnt ->
    [one] {$cnt} email
    *[other] {$cnt} emails
}
")

(def second-resource
  "
goodbye = Goodbye world!
  .attr = Goodbye attr!
  .named = Goodbye {$name}!
")

(deftest build-test
  (is (instance? FluentBundle (sut/build "en" simple-resource)))
  (is (thrown? #?(:clj Exception :cljs js/Error)
               (sut/build "DOES-NOT-MATCH" simple-resource))))

(deftest add-resource-test
  (let [original (sut/build "en" simple-resource)
        with-extra (sut/add-resource original second-resource)]
    (is (instance? FluentBundle with-extra))
    (is (= "Goodbye world!" (sut/format with-extra :goodbye)))
    (is (= "Goodbye attr!" (sut/format with-extra :goodbye.attr)))
    (is (= "Goodbye noah!" (sut/format with-extra :goodbye.named {:name "noah"})))
    (is (thrown-with-msg? ExceptionInfo
          #"Invalid attribute provided: attr.attr2"
          (sut/format with-extra :goodbye.attr.attr2)))))

(deftest format-test
  (let [bundle (sut/build "en" simple-resource)]
    (testing "if asked for the wrong name"
      #?(:clj (is (thrown-with-msg? Exception
                    #"Missing message for id: 'missing'"
                    (sut/format bundle "missing")))
         :cljs (is (nil? (sut/format bundle "missing"))))
      #?(:clj (is (thrown-with-msg? Exception
                    #"Missing message for id: 'missing.attr'"
                    (sut/format bundle "missing.attr")))
         :cljs (is (nil? (sut/format bundle "missing.attr")))))
    (testing "can handle strings, keywords, and symbols"
      (is (= "Hello world!" (sut/format bundle "hello")))
      (is (= "Hello world!" (sut/format bundle 'hello)))
      (is (= "Hello world!" (sut/format bundle :hello))))
    (testing "can take args"
      (testing "and requires used args"
        (is (thrown-with-msg?
              #?(:clj Exception :cljs js/ReferenceError)
              #"Missing expected keys: \$cnt"
              (sut/format bundle :email-cnt)))
        (is (thrown-with-msg?
              #?(:clj Exception :cljs js/ReferenceError)
              #"Missing expected keys: \$cnt"
              (sut/format bundle :email-cnt {}))))
      (is (= "Welcome, Noah!" (sut/format bundle :welcome {"user" "Noah"})))
      (is (= "0 emails" (sut/format bundle :email-cnt {:cnt 0})))
      (is (= "1 email" (sut/format bundle :email-cnt {:cnt 1})))
      (is (= "2 emails" (sut/format bundle 'email-cnt {'cnt 2})))
      (is (= "100 emails" (sut/format bundle "email-cnt" {"cnt" 100})))
      (testing "and ignores extra args"
        (is (= "1 email" (sut/format bundle :email-cnt {:cnt 1 :extra :arg})))))))
