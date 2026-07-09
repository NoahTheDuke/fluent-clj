(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = { FOO(bar: 1) }\n")
 (parse-and-print-str "\nfoo = { FOO(bar: \"bar\") }\n")]

["foo = { FOO(bar: 1) }" "foo = { FOO(bar: \"bar\") }"]
