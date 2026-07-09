(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = \\{ foo }\n")]

["foo = \\{ foo }"]
