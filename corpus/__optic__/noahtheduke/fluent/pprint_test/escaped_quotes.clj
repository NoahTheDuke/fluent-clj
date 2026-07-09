(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = { \"escaped \\\" quote\" }\n")]

["foo = { \"escaped \" quote\" }"]
