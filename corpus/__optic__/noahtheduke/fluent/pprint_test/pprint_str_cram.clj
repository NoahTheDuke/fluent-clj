(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "message = 1")
 (parse-and-print-str "# hello\nmessage = 1")
 (parse-and-print-str "game_mu-count = { $unused } of { $available } MU unused")
 (parse-and-print-str
   "\ngame_face-down-count =\n    { $total ->\n        [one] { $total } card, { $facedown } face-down.\n        *[other] { $total } cards, { $facedown } face-down.\n    }")
 (parse-and-print-str
   "foo =\n    { $a ->\n       *[a]\n            { $b ->\n               *[b] Foo\n            }\n    }")]

["message = 1" "# hello\nmessage = 1"
 "game_mu-count = { $unused } of { $available } MU unused"
 "game_face-down-count =\n    { $total ->\n        [one] { $total } card, { $facedown } face-down.\n       *[other] { $total } cards, { $facedown } face-down.\n    }"
 "foo =\n    { $a ->\n       *[a]\n            { $b ->\n               *[b] Foo\n            }\n    }"]
