(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str
   "\nfoo = { $num ->\n    [-123456789] Minus a lot\n    [0] Zero\n   *[3.14] Pi\n    [007] James\n}\n")]

["foo =\n    { $num ->\n        [-123456789] Minus a lot\n        [0] Zero\n       *[3.14] Pi\n        [7] James\n    }"]
