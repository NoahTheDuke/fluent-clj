(in-ns 'noahtheduke.fluent.pprint-test)

(with-out-str
  [(parse-and-print "message = 1") (parse-and-print "# hello\nmessage = 1")
   (parse-and-print "game_mu-count = {$unused} of {$available} MU unused")
   (parse-and-print
     "game_face-down-count = {$total ->\n    [one] {$total} card, {$facedown} face-down.\n    *[other] {$total} cards, {$facedown} face-down.\n}")])

"message = 1

# hello
message = 1
game_mu-count = {$unused} of {$available} MU unused
game_face-down-count = {$total ->
    [one] {$total} card, {$facedown} face-down.
    *[other] {$total} cards, {$facedown} face-down.
}
"
