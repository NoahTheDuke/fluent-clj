(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo =\n    Foo { placeable }\n    Bar\n")]

["foo =\n    Foo { placeable }\n    Bar"]
