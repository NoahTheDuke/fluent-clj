(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo =\n    { $sel ->\n       *[1] 1\n    }\n")]

["foo =\n    { $sel ->\n       *[1] 1\n    }"]
