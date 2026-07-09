(ns noahtheduke.fluent.pprint-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [noahtheduke.fluent.impl.clojure :refer [parse]]
   [noahtheduke.fluent.pprint :as sut]
   [noahtheduke.fluent.test-helpers :refer [defcram]]))

(defn parse-and-print
  [s]
  (sut/pprint (parse (str/trim s))))

(defn parse-and-print-str
  [s]
  (sut/pprint-str (parse (str/trim s))))

(defcram simple-message
  (parse-and-print-str "
foo = Foo
"))

(defcram simple-term
  (parse-and-print-str "
-foo = Foo
"))

(defcram two-messages
  (parse-and-print-str "
foo = Foo
bar = Bar
"))

(defcram multiline-message
  (parse-and-print-str "
foo =
    Foo
    Bar
"))

(defcram inlined-multiline-message
  (parse-and-print-str "
foo = Foo
    Bar
"))

(defcram message-with-reference-element
  (parse-and-print-str "
foo = Foo { bar }
"))

(defcram message-with-term-element
  (parse-and-print-str "
foo = Foo { -bar }
"))

(defcram message-with-placeable-element
  (parse-and-print-str "
foo = Foo { $bar }
"))

(defcram message-with-literal-element
  (parse-and-print-str "
foo = Foo { 1 }
")
  (parse-and-print-str "
foo = Foo { \"bar\" }
"))

(defcram message-with-attribute-element
  (parse-and-print-str "
foo = Foo { bar.baz }
"))

(defcram multiline-resource-comment
  (parse-and-print-str "
### a multiline
### resource comment
"))

(defcram multiline-group-comment
  (parse-and-print-str "
foo = Foo

## a multiline
## group comment

bar = Bar
"))

;; broken
#_(defcram multiline-group-comment-with-empty-line
  (parse-and-print-str "
foo = Foo

## a multiline
##
## group comment

bar = Bar
"))

(defcram multiline-standalone-message-comment
  (parse-and-print-str "
# a multiline
# message comment

foo = Foo
"))

(defcram multiline-attached-message-comment
  (parse-and-print-str "
# a multiline
# message comment
foo = Foo
"))

(defcram standalone-message-comment
  (parse-and-print-str "
foo = Foo

# a standalone comment

bar = Bar
"))

(defcram multiline-message-starting-inline
  (parse-and-print-str "
foo = Foo
    Bar
"))

(defcram multiline-message-inline-with-special-character
  (parse-and-print-str "
foo = *Foo
    Bar
"))

(defcram multiline-inline-with-placeable
  (parse-and-print-str "
foo = Foo { placeable }
    Bar
"))

(defcram multiline-with-placeable
  (parse-and-print-str "
foo =
    Foo { placeable }
    Bar
"))

(defcram message-with-attribute
  (parse-and-print-str "
foo =
    .attr = Foo Attr
"))

(defcram message-with-multiple-attributes
  (parse-and-print-str "
foo =
    .attr-a = Foo Attr
    .attr-b = Bar Attr
"))

(defcram multiline-message-with-multiple-attributes (parse-and-print-str "
foo =
    Base Foo
    Extra Foo
    .attr-a = Foo Attr
    .attr-b = Bar Attr
"))

(defcram select-expression
  (parse-and-print-str "
foo =
    { $sel ->
       *[a] A
    }
"))

(defcram select-expression-multiple-variants
  (parse-and-print-str "
foo =
    { $sel ->
       *[a] A
        [b] B
    }
"))

(defcram select-expression-inline-multiline-variant
  (parse-and-print-str "
foo = { $sel ->
       *[a] AAA
            BBB
    }
"))

(defcram select-expression-multiline-variant
  (parse-and-print-str "
foo =
    { $sel ->
       *[a]
            AAA
            BBB
    }
"))

(defcram select-expression-numeric-key
  (parse-and-print-str "
foo =
    { $sel ->
       *[1] 1
    }
"))

(defcram select-expression-after-text
  (parse-and-print-str "
foo =
    Foo { $sel ->
       *[a] A
        [b] B
    }
"))

(defcram inline-select-expression-after-text
  (parse-and-print-str "
foo = Foo { $sel ->
   *[a] A
    [b] B
}
"))

(defcram inline-select-expression-after-special-character
  (parse-and-print-str "
foo = .Foo { $sel ->
   *[a] A
    [b] B
}
"))

(defcram select-expression-after-multiline
  (parse-and-print-str "
foo =
    Foo
    Bar { $sel ->
       *[a] A
        [b] B
    }
"))

(defcram nested-select-expressions
  (parse-and-print-str "
foo =
    { $a ->
       *[a]
            { $b ->
               *[b] Foo
            }
    }
"))

(defcram select-expression-with-literal-selector
  (parse-and-print-str "
foo =
    { 1 ->
       *[a] A
    }
")
  (parse-and-print-str "
foo =
    { \"bar\" ->
       *[a] A
    }
"))

(defcram select-expression-with-attribute-expression
  (parse-and-print-str "
foo =
    { -bar.baz ->
       *[a] A
    }
"))

(defcram function-expression
  (parse-and-print-str "
foo = { FOO() }
"))

(defcram function-expression-with-literal-arg
  (parse-and-print-str "
foo = { FOO(\"bar\") }
")
  (parse-and-print-str "
foo = { FOO(1) }
"))

(defcram function-expression-with-message-reference
  (parse-and-print-str "
foo = { FOO(bar) }
"))

(defcram function-expression-with-placeable-reference
  (parse-and-print-str "
foo = { FOO($bar) }
"))

(defcram function-expression-with-literal-named-arg
  (parse-and-print-str "
foo = { FOO(bar: 1) }
")
  (parse-and-print-str "
foo = { FOO(bar: \"bar\") }
"))

(defcram function-expression-with-multiple-positional-args
  (parse-and-print-str "
foo = { FOO(bar, baz) }
"))

(defcram function-expression-with-multiple-named-args (parse-and-print-str "
foo = { FOO(bar: \"bar\", baz: \"baz\") }
"))

(defcram function-expression-with-both-args
  (parse-and-print-str "
foo = { FOO(bar, 1, baz: \"baz\") }
"))

(defcram function-expression-with-macro
  (parse-and-print-str "
foo = { -term() }
"))

(defcram escaped-placeable
  (parse-and-print-str "
foo = {{ FOO() }}
"))

(defcram escaped-characters
  (parse-and-print-str "
foo = \\{ foo }
"))

(defcram escaped-quotes
  (parse-and-print-str "
foo = { \"escaped \\\" quote\" }
"))

(defcram escaped-unicode-characters (parse-and-print-str "
foo = { \"\\\\u0065\" }"))

(defcram resource-comment-padding
  (parse-and-print-str "
### Resource Comment A

foo = Foo

### Resource Comment B

bar = Bar
"))

(defcram group-comment-padding
  (parse-and-print-str "
## Group A

foo = Foo

## Group B

bar = Bar
"))

(defcram variant-keys-identifiers
  (parse-and-print-str "
foo = { $num ->
    [one] One
   *[other] Other
}
"))

(defcram variant-keys-number-literals
  (parse-and-print-str "
foo = { $num ->
    [-123456789] Minus a lot
    [0] Zero
   *[3.14] Pi
    [007] James
}
"))

(defcram pprint-cram
  (with-out-str (parse-and-print "message = 1"))
  (with-out-str (parse-and-print "# hello\nmessage = 1"))
  (with-out-str (parse-and-print "game_mu-count = { $unused } of { $available } MU unused"))
  (with-out-str (parse-and-print "
game_face-down-count =
   { $total ->
       [one] { $total } card, { $facedown } face-down.
       *[other] { $total } cards, { $facedown } face-down.
   }"))
  (with-out-str (parse-and-print "
foo =
    { $a ->
       *[a]
            { $b ->
               *[b] Foo
            }
    }")))

(defcram pprint-str-cram
  (parse-and-print-str "message = 1")
  (parse-and-print-str "# hello\nmessage = 1")
  (parse-and-print-str "game_mu-count = { $unused } of { $available } MU unused")
  (parse-and-print-str "
game_face-down-count =
    { $total ->
        [one] { $total } card, { $facedown } face-down.
        *[other] { $total } cards, { $facedown } face-down.
    }")
  (parse-and-print-str "foo =
    { $a ->
       *[a]
            { $b ->
               *[b] Foo
            }
    }"))

(comment
  ;; the file is too big to easily use in a cram test
  (spit "corpus/Russian_ru.ftl"
    (parse-and-print-str (slurp "corpus/Russian_ru.ftl"))))

(deftest roundtripping-test
  (let [s "game_face-down-count = { $total ->
    [one] { $total } card, { $facedown } face-down.
    *[other] { $total } cards, { $facedown } face-down.
}"]
    (is (= (sut/pprint-str (parse s))
           (sut/pprint-str (parse (sut/pprint-str (parse s))))
           (sut/pprint-str (parse (sut/pprint-str (parse (sut/pprint-str (parse s))))))))))

(defcram sort-entries
  (sut/pprint-str (parse "
foo = Foo
-example-term = Example term
bar = Bar
")
                  {:sort? true})
  (sut/pprint-str (parse "
# comment about beta
beta = Beta
alpha = Alpha
")
                  {:sort? true})
  (sut/pprint-str (parse "
### file comment
-beta = Beta
alpha = Alpha
## section 1
Greeting = Hello
another-greeting = Hi


## section 2
## formatting stays intact, e.g. multiple    spaces

message = Example message
another-message = {$sel ->
   *[a] AAA
        BBB
}
")
                  {:sort? true}))
