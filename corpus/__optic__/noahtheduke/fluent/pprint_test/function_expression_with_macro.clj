(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = { -term() }\n")]

["foo = { -term() }"]
