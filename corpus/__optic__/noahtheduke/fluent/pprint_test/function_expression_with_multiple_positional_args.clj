(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = { FOO(bar, baz) }\n")]

["foo = { FOO(bar, baz) }"]
