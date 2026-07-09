(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = Foo\n    Bar\n")]

["foo =\n    Foo\n    Bar"]
