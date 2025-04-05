(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "message = 1")
 (parse-and-print-str "# hello\nmessage = 1")
 (parse-and-print-str "game_mu-count = {$unused} of {$available} MU unused")
 (parse-and-print-str
   "game_face-down-count = {$total ->\n    [one] {$total} card, {$facedown} face-down.\n    *[other] {$total} cards, {$facedown} face-down.\n}")]

["message = 1" "# hello\nmessage = 1"
 "game_mu-count = {$unused} of {$available} MU unused"
 "game_face-down-count = {$total ->\n    [one] {$total} card, {$facedown} face-down.\n    *[other] {$total} cards, {$facedown} face-down.\n}"]
