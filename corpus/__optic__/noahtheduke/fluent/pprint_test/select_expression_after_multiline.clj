(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str
   "\nfoo =\n    Foo\n    Bar { $sel ->\n       *[a] A\n        [b] B\n    }\n")]

["foo =\n    Foo\n    Bar { $sel ->\n       *[a] A\n        [b] B\n    }"]
