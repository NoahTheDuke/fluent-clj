(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = Foo\nbar = Bar\n")]

["foo = Foo\nbar = Bar"]
