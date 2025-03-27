# Change Log

## Unreleased

- Revert swallowing cljs reference errors. (See [official forums](https://discourse.mozilla.org/t/which-tools-are-available-for-finding-errors-in-ftl-files/70313/2) for reasoning.)
- Bump @fluent/bundle to 0.19.
- Add Clojure and Java formatters. Clojure is more idiomatic, Java is twice as fast. 500us vs 250us for a 2k file.
- Clean up build.clj, add prep task.

## 0.0.2

Released on 2025-03-24.

- Print cljs errors to console, not throw. Frontends shouldn't crash.

## 0.0.1

Released on 2025-03-22.

- Initial version
