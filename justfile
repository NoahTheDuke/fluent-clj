default:
    @just --list

@repl arg="":
    clojure -M{{arg}}:repl

@run *args:
    clojure -M:run {{args}}

test-all:
    clojure -M:dev:test:runner
    npx shadow-cljs compile node-test

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
    just test-all
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
