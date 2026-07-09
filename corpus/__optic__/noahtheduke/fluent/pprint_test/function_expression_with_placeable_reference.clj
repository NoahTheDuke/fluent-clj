(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = { FOO($bar) }\n")]

["foo = { FOO($bar) }"]
