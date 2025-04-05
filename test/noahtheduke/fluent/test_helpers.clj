(ns noahtheduke.fluent.test-helpers
  (:require
   [clojure.test :refer [deftest is]]
   [opticlj.core :as optic]
   [opticlj.file :as optic.file]
   [clojure.string :as str]
   [clojure.java.io :as io]) 
  (:import
    [java.io File]))

(set! *warn-on-reflection* true)

(optic/set-dir! "corpus/__optic__")

(defmacro defcram
  [test-name & body]
  (let [kw (keyword (str *ns*) (name test-name))]
    `(do (optic/defoptic ~kw [~@body])
         (deftest ~test-name
           (optic/run ~kw)
           (is (optic/check (get-in @optic/system* [:optics ~kw])))))))

(defn promote
  [_arg]
  (prn _arg)
  (doseq [err-file (->> (file-seq (io/file (:dir @optic/system*)))
                        (filter #(str/ends-with? (str %) ".err.clj")))
          :let [file (io/file (str/replace (str err-file) ".err.clj" ".clj"))
                _ (prn file)]
          :when (File/.exists file)]
    (optic.file/rename file err-file)))

(promote nil)
