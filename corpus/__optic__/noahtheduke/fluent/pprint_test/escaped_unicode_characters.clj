(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = { \"\\\\u0065\" }")]

["foo = { \"\\u0065\" }"]
