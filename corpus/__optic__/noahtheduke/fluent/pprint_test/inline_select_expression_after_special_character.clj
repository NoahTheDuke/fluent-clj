(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = .Foo { $sel ->\n   *[a] A\n    [b] B\n}\n")]

["foo = .Foo { $sel ->\n       *[a] A\n        [b] B\n    }"]
