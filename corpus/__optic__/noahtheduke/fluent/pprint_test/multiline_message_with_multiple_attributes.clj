(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str
   "\nfoo =\n    Base Foo\n    Extra Foo\n    .attr-a = Foo Attr\n    .attr-b = Bar Attr\n")]

["foo =\n    Base Foo\n    Extra Foo\n    .attr-a = Foo Attr\n    .attr-b = Bar Attr"]
