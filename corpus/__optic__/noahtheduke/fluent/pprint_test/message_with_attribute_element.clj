(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = Foo { bar.baz }\n")]

["foo = Foo { bar.baz }"]
