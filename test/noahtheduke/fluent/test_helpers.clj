(ns noahtheduke.fluent.test-helpers
  (:require
   [clojure.test :refer [deftest is]]
   [opticlj.core :as optic]))

(optic/set-dir! "corpus/__optic__")

(defmacro defcram
  [test-name & body]
  (let [kw (keyword (str *ns*) (name test-name))]
    `(do (optic/defoptic ~kw [~@body])
         (deftest ~test-name
           (optic/run ~kw)
           (is (optic/check (get-in @optic/system* [:optics ~kw])))))))
