(in-ns 'noahtheduke.fluent.pprint-test)

[(parse-and-print-str "\nfoo = Foo { placeable }\n    Bar\n")]

["foo =\n    Foo { placeable }\n    Bar"]
