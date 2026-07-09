(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str
   "\n### Resource Comment A\n\nfoo = Foo\n\n### Resource Comment B\n\nbar = Bar\n")]

["### Resource Comment A\n\nfoo = Foo\n\n### Resource Comment B\n\nbar = Bar"]
