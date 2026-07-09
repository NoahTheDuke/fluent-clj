(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str
   "\nfoo = Foo\n\n## a multiline\n## group comment\n\nbar = Bar\n")]

["foo = Foo\n\n## a multiline\n## group comment\n\nbar = Bar"]
