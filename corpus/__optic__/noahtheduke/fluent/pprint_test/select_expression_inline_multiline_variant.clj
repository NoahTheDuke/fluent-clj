(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str
   "\nfoo = { $sel ->\n       *[a] AAA\n            BBB\n    }\n")]

["foo =\n    { $sel ->\n       *[a]\n            AAA\n            BBB\n    }"]
