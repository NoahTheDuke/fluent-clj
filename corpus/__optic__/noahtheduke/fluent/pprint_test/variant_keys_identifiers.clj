(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str
   "\nfoo = { $num ->\n    [one] One\n   *[other] Other\n}\n")]

["foo =\n    { $num ->\n        [one] One\n       *[other] Other\n    }"]
