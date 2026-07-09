(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo =\n    { -bar.baz ->\n       *[a] A\n    }\n")]

["foo =\n    { -bar.baz ->\n       *[a] A\n    }"]
