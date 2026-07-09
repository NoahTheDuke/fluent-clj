(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\n# a multiline\n# message comment\n\nfoo = Foo\n")]

["# a multiline\n# message comment\n\nfoo = Foo"]
