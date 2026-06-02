; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

#_:splint/disable
(ns dev
  (:require virgil
            [clj-async-profiler.core :as prof]))

(virgil/watch-and-recompile ["src"])

(prof/serve-ui 8080)
