(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = { FOO(bar, 1, baz: \"baz\") }\n")]

["foo = { FOO(bar, 1, baz: \"baz\") }"]
