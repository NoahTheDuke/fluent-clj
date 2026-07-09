(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = Foo { 1 }\n")
 (parse-and-print-str "\nfoo = Foo { \"bar\" }\n")]

["foo = Foo { 1 }" "foo = Foo { \"bar\" }"]
