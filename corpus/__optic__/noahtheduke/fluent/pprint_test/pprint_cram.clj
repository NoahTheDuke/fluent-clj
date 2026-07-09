(in-ns 'noahtheduke.fluent.pprint-test)

[(with-out-str (parse-and-print "message = 1"))
 (with-out-str (parse-and-print "# hello\nmessage = 1"))
 (with-out-str (parse-and-print
                 "game_mu-count = { $unused } of { $available } MU unused"))
 (with-out-str
   (parse-and-print
     "\ngame_face-down-count =\n   { $total ->\n       [one] { $total } card, { $facedown } face-down.\n       *[other] { $total } cards, { $facedown } face-down.\n   }"))
 (with-out-str
   (parse-and-print
     "\nfoo =\n    { $a ->\n       *[a]\n            { $b ->\n               *[b] Foo\n            }\n    }"))]

["message = 1\n\n" "\n# hello\nmessage = 1\n\n"
 "game_mu-count = { $unused } of { $available } MU unused\n\n"
 "game_face-down-count =\n    { $total ->\n        [one] { $total } card, { $facedown } face-down.\n       *[other] { $total } cards, { $facedown } face-down.\n    }\n\n"
 "foo =\n    { $a ->\n       *[a]\n            { $b ->\n               *[b] Foo\n            }\n    }\n\n"]
