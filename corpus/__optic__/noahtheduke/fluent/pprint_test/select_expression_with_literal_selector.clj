(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo =\n    { 1 ->\n       *[a] A\n    }\n")
 (parse-and-print-str "\nfoo =\n    { \"bar\" ->\n       *[a] A\n    }\n")]

["foo =\n    { 1 ->\n       *[a] A\n    }"
 "foo =\n    { \"bar\" ->\n       *[a] A\n    }"]
