(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str
   "\nfoo =\n    { $a ->\n       *[a]\n            { $b ->\n               *[b] Foo\n            }\n    }\n")]

["foo =\n    { $a ->\n       *[a]\n            { $b ->\n               *[b] Foo\n            }\n    }"]
