(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\n## Group A\n\nfoo = Foo\n\n## Group B\n\nbar = Bar\n")]

["## Group A\n\nfoo = Foo\n\n## Group B\n\nbar = Bar"]
