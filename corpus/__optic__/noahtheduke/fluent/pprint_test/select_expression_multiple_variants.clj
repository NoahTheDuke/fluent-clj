(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str
   "\nfoo =\n    { $sel ->\n       *[a] A\n        [b] B\n    }\n")]

["foo =\n    { $sel ->\n       *[a] A\n        [b] B\n    }"]
