(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = { FOO(bar: \"bar\", baz: \"baz\") }\n")]

["foo = { FOO(bar: \"bar\", baz: \"baz\") }"]
