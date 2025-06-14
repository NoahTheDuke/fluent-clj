default:
    @just --list

@gen-docs:
    markdown-toc -i --maxdepth 2 README.md

@repl arg="":
    clojure -M{{arg}}:dev:test:repl

@run *args:
    clojure -M:run {{args}}

splint:
    clojure -M:dev:test:splint

lsp:
    clojure-lsp diagnostics

lint:
    just splint
    just lsp

test-clj:
    clojure -M:dev:test:runner

test-cljs:
    npx shadow-cljs compile node-test
    node target/shadow-node-test/node-tests.js

test:
    @just test-clj
    @just test-cljs

jar:
    clojure -T:build jar

today := `date +%F`
current_version := `cat resources/FLUENTCLJ_VERSION | xargs`

# Set version, change all instances of <<next>> to version
@set-version version:
    echo '{{version}}' > resources/FLUENTCLJ_VERSION
    fd '.(clj|edn|md)' . -x sd '<<next>>' '{{version}}' {}
    sd '{{current_version}}' '{{version}}' README.md
    sd '## Unreleased' '## Unreleased\n\n## {{version}}\n\nReleased on {{today}}.' CHANGELOG.md

@clojars:
    env CLOJARS_USERNAME='noahtheduke' CLOJARS_PASSWORD=`cat ../clojars.txt` clojure -T:build deploy

# Builds the uberjar, builds the jar, sends the jar to clojars
@release version:
    echo 'Running tests'
    just test
    echo 'Setting new version {{version}}'
    just set-version {{version}}
    echo 'Commit and tag'
    git commit -a -m 'Bump version for release'
    git tag v{{version}}
    echo 'Pushing to github'
    git push
    git push --tags
    echo 'Building uber'
    clojure -T:build uberjar
    echo 'Deploying to clojars'
    just clojars
