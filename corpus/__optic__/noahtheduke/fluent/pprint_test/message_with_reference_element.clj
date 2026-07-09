(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = Foo { bar }\n")]

["foo = Foo { bar }"]
