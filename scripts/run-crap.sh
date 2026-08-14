#!/bin/sh
set -eu

exec clojure -Sdeps '{:deps {io.github.unclebob/crap4clj {:git/url "https://github.com/unclebob/crap4clj" :git/sha "e6e0312fb9fe25cb8bdc55557d60c0e1c0568fb9"}}}' \
  -M -m crap4clj.core \
  --source-root src \
  --use-existing-coverage \
  --lcov target/coverage/lcov.info
