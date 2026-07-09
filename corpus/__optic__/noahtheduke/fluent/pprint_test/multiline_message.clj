(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo =\n    Foo\n    Bar\n")]

["foo =\n    Foo\n    Bar"]
