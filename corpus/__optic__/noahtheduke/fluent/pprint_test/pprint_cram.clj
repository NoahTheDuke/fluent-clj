(in-ns 'noahtheduke.fluent.pprint-test)

[(with-out-str (parse-and-print "message = 1"))
 (with-out-str (parse-and-print "# hello\nmessage = 1"))
 (with-out-str (parse-and-print
                 "game_mu-count = {$unused} of {$available} MU unused"))
 (with-out-str
   (parse-and-print
     "game_face-down-count = {$total ->\n    [one] {$total} card, {$facedown} face-down.\n    *[other] {$total} cards, {$facedown} face-down.\n}"))]

["message = 1\n" "\n# hello\nmessage = 1\n"
 "game_mu-count = {$unused} of {$available} MU unused\n"
 "game_face-down-count = {$total ->\n    [one] {$total} card, {$facedown} face-down.\n    *[other] {$total} cards, {$facedown} face-down.\n}\n"]
