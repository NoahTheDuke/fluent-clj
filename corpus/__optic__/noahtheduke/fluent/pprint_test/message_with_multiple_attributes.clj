(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str
   "\nfoo =\n    .attr-a = Foo Attr\n    .attr-b = Bar Attr\n")]

["foo =\n    .attr-a = Foo Attr\n    .attr-b = Bar Attr"]
