(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = { FOO() }\n")]

["foo = { FOO() }"]
