(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = { FOO(\"bar\") }\n")
 (parse-and-print-str "\nfoo = { FOO(1) }\n")]

["foo = { FOO(\"bar\") }" "foo = { FOO(1) }"]
