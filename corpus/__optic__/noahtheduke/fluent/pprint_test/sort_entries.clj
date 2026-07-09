(in-ns 'noahtheduke.fluent.pprint-test)

[(sut/pprint-str (parse
                   "\nfoo = Foo\n-example-term = Example term\nbar = Bar\n")
                 {:sort? true})
 (sut/pprint-str (parse "\n# comment about beta\nbeta = Beta\nalpha = Alpha\n")
                 {:sort? true})
 (sut/pprint-str
   (parse
     "\n### file comment\n-beta = Beta\nalpha = Alpha\n## section 1\nGreeting = Hello\nanother-greeting = Hi\n\n\n## section 2\n## formatting stays intact, e.g. multiple    spaces\n\nmessage = Example message\nanother-message = {$sel ->\n   *[a] AAA\n        BBB\n}\n")
   {:sort? true})]

["bar = Bar\n-example-term = Example term\nfoo = Foo"
 "alpha = Alpha\n\n# comment about beta\nbeta = Beta"
 "### file comment\n\nalpha = Alpha\n-beta = Beta\n\n## section 1\n\nanother-greeting = Hi\nGreeting = Hello\n\n## section 2\n## formatting stays intact, e.g. multiple    spaces\n\nanother-message =\n    { $sel ->\n       *[a]\n            AAA\n            BBB\n    }\nmessage = Example message"]
