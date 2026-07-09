(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo =\n    { $sel ->\n       *[a] A\n    }\n")]

["foo =\n    { $sel ->\n       *[a] A\n    }"]
