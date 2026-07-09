(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\n# a multiline\n# message comment\nfoo = Foo\n")]

["# a multiline\n# message comment\nfoo = Foo"]
