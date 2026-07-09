(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo =\n    .attr = Foo Attr\n")]

["foo =\n    .attr = Foo Attr"]
