#!/usr/bin/env bash
#
# Refreshes the vendored OpenAPI models and ChatEndpoints in stream-chat-android-client from the chat backend's spec.
#
# Only what is already vendored is refreshed: the generator emits every model and endpoint of the spec, but the client
# keeps the ones it uses. ChatEndpoints keeps the functions it already declares, matched by name and by whether they
# take a body (the generator emits a body-less overload next to every endpoint with an optional body).
#
# A vendored file with a `// Patched:` comment is left as it is; the comment says why it differs from the spec and when
# the patch can go. The script lists the fields such a file lacks or has beyond the spec. To refresh it and keep the
# patch, copy the generated version over it, run spotlessApply, and restore the patched lines from the diff. To vendor
# a new model or endpoint, copy it from the generated output in build/ by hand.
#
# Usage:
#   ./generate_openapi.sh                             # backend master, fetched into build/openapi/chat
#   ./generate_openapi.sh --ref=<branch|tag|full sha> # a given backend revision
#   ./generate_openapi.sh --source=<path>             # an existing checkout of GetStream/chat, used as is
#   ./generate_openapi.sh --repo-url=<url>            # fetch from another URL, e.g. over HTTPS
#
# Needs Go and SSH access to the GetStream repos: the generator depends on private modules (GetStream/kit and others),
# which Go fetches over HTTPS unless git rewrites it to SSH. The backend's `make setup` sets the rewrite:
#   git config --global url."git@github.com:".insteadOf "https://github.com/"
# GOPRIVATE='github.com/GetStream/*' also keeps Go from asking the public module proxy and checksum database for them.

set -euo pipefail

REPO_URL="git@github.com:GetStream/chat.git"
REF="master"
SOURCE_PATH=""

for arg in "$@"; do
  case $arg in
    --repo-url=*) REPO_URL="${arg#*=}" ;;
    --ref=*) REF="${arg#*=}" ;;
    --source=*) SOURCE_PATH="${arg#*=}" ;;
    *)
      echo "Unknown argument: $arg" >&2
      exit 1
      ;;
  esac
done

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
BUILD_DIR="$PROJECT_ROOT/build/openapi"
GENERATED_DIR="$BUILD_DIR/generated"
VENDORED_DIR="$PROJECT_ROOT/stream-chat-android-client/src/main/java/io/getstream/chat/android/network"
# Vendored files that are deliberately not the generated ones. WSEvent keeps the interface only: the SDK routes the
# socket events itself, and the generated union adapter would need every event of the spec.
SKIPPED=("models/WSEvent.kt")

# Step 1: get the backend sources.
if [[ -z "$SOURCE_PATH" ]]; then
  SOURCE_PATH="$BUILD_DIR/chat"
  if [[ ! -d "$SOURCE_PATH/.git" ]]; then
    mkdir -p "$SOURCE_PATH"
    git -C "$SOURCE_PATH" init -q
    git -C "$SOURCE_PATH" remote add origin "$REPO_URL"
  fi
  git -C "$SOURCE_PATH" remote set-url origin "$REPO_URL"
  echo "Fetching $REPO_URL at $REF..."
  # A shallow fetch takes a branch, a tag or a full commit SHA, not an abbreviated one.
  git -C "$SOURCE_PATH" fetch -q --depth=1 origin "$REF"
  git -C "$SOURCE_PATH" checkout -q --force --detach FETCH_HEAD
elif [[ "$REF" != "master" ]]; then
  echo "--ref is ignored with --source: generating from $SOURCE_PATH as it is." >&2
fi
BACKEND_SHA="$(git -C "$SOURCE_PATH" rev-parse --short HEAD 2>/dev/null || echo "at $SOURCE_PATH")"
echo "Generating from backend $BACKEND_SHA"

# Step 2: build the generator, the spec and the Kotlin client.
rm -rf "$GENERATED_DIR"
mkdir -p "$GENERATED_DIR"
(cd "$SOURCE_PATH/tools/openapi" && go build -o "$BUILD_DIR/openapi" .)

# The same spec as the published releases/v2/chat-clientside-api. The unix-timestamp flag only retypes response dates
# as numbers in the spec; the Kotlin output doesn't change.
(cd "$SOURCE_PATH" && "$BUILD_DIR/openapi" generate-spec \
  -products chat,common,moderation \
  -version v2 \
  -clientside \
  -encode-time-as-unix-timestamp \
  -output "$BUILD_DIR/spec") >"$BUILD_DIR/spec.log" 2>&1 ||
  { echo "Spec generation failed, see $BUILD_DIR/spec.log" >&2 && exit 1; }

(cd "$SOURCE_PATH" && "$BUILD_DIR/openapi" generate-client \
  --language kotlin \
  --spec "$BUILD_DIR/spec.yaml" \
  --opt api-service-class-name=ChatEndpoints \
  --opt api-service-package=io.getstream.chat.android.network.apis \
  --opt api-service-dir=apis \
  --opt model-package=io.getstream.chat.android.network.models \
  --opt moshi-adapters-package=io.getstream.chat.android.network.infrastructure \
  --opt moshi-adapters-dir=infrastructure \
  --opt classes-to-skip=StopRTMPBroadcastsRequest \
  --opt extend-data-classes=true \
  --opt android-sdk=chat \
  --opt date-time-type=exact-date \
  --output "$GENERATED_DIR") >"$BUILD_DIR/client.log" 2>&1 ||
  { echo "Client generation failed, see $BUILD_DIR/client.log" >&2 && exit 1; }

# Step 3: refresh the vendored files.
json_names() { grep -o '@Json(name = "[^"]*")' "$1" | sed 's/.*"\(.*\)".*/\1/' | sort -u; }
for vendored in "$VENDORED_DIR"/models/*.kt "$VENDORED_DIR"/infrastructure/*.kt; do
  relative="${vendored#"$VENDORED_DIR"/}"
  generated="$GENERATED_DIR/$relative"
  if [[ " ${SKIPPED[*]} " == *" $relative "* ]]; then
    echo "Kept (not generated): $relative"
  elif grep -q "// Patched:" "$vendored"; then
    echo "Kept (patched): $relative"
    # A patched file still has to follow the spec's fields: list the ones it is missing or no longer in the spec.
    if [[ ! -f "$generated" ]]; then
      echo "  no longer generated"
    else
      fields="$(comm -3 <(json_names "$generated") <(json_names "$vendored"))"
      if [[ -n "$fields" ]]; then
        while IFS= read -r line; do
          if [[ "$line" == $'\t'* ]]; then
            echo "  not in the spec any more: ${line#$'\t'}"
          else
            echo "  missing, merge it by hand: $line"
          fi
        done <<<"$fields"
        echo "  generated version: $generated"
      fi
    fi
  elif [[ ! -f "$generated" ]]; then
    echo "No longer generated: $relative"
  elif [[ "$relative" == "infrastructure/Serializer.kt" ]]; then
    # The generated Serializer registers an adapter for every enum of the spec: keep the ones for vendored classes.
    while IFS= read -r line || [[ -n "$line" ]]; do
      if [[ "$line" =~ \.add\(io\.getstream\.chat\.android\.network\.(models|infrastructure)\.([A-Za-z0-9_]+) ]] &&
        [[ ! -f "$VENDORED_DIR/${BASH_REMATCH[1]}/${BASH_REMATCH[2]}.kt" ]]; then
        # Moshi fails at runtime on a vendored model using a platform type (BigDecimal, UUID...) whose adapter isn't
        # vendored, so say so.
        type="${BASH_REMATCH[2]%Adapter}"
        if [[ "${BASH_REMATCH[1]}" == "infrastructure" ]] && grep -qw "$type" "$VENDORED_DIR"/models/*.kt; then
          echo "Needed but not vendored: infrastructure/${BASH_REMATCH[2]}.kt (a vendored model uses $type)" >&2
        fi
        continue
      fi
      printf '%s\n' "$line"
    done <"$generated" >"$vendored"
  else
    cp "$generated" "$vendored"
  fi
done

# Refresh the ChatEndpoints functions already vendored, keyed by name and by whether they take a body. A vendored
# function the generator no longer emits is kept as it is, like a model.
VENDORED_API="$VENDORED_DIR/apis/ChatEndpoints.kt"
if grep -q "// Patched:" "$VENDORED_API"; then
  echo "Kept (patched): apis/ChatEndpoints.kt"
else
  awk -v out="$BUILD_DIR/ChatEndpoints.kt" '
    # A function runs from its KDoc, or its method annotation when it has none, to the line closing its parameters.
    !inside && (/^    \/\*\*/ || /^    @(GET|POST|PUT|PATCH|DELETE)\(/) {
      inside = 1; block = ""; name = ""; body = "nobody"; started[FILENAME] = 1
    }
    !started[FILENAME] { if (FILENAME == ARGV[2]) header = header $0 "\n"; next }
    !inside { next }
    { block = block $0 "\n" }
    name != "" && /@Body/ { body = "body" }
    name == "" && match($0, /suspend fun [A-Za-z0-9_]+/) {
      name = substr($0, RSTART + 12, RLENGTH - 12)
      if (substr($0, RSTART + RLENGTH) ~ /@Body/) body = "body"
    }
    name != "" && /\): / {
      inside = 0
      key = name " " body
      if (FILENAME == ARGV[1]) { if (!(key in vendored)) { vendored[key] = block; order[++count] = key } }
      else if ((key in vendored) && !(key in generated)) generated[key] = block
    }
    END {
      if (!count) { print "No functions parsed from " ARGV[1] > "/dev/stderr"; exit 1 }
      if (!started[ARGV[2]]) { print "No functions parsed from " ARGV[2] > "/dev/stderr"; exit 1 }
      printf "%s", header > out
      for (i = 1; i <= count; i++) {
        key = order[i]
        if (key in generated) printf "\n%s", generated[key] > out
        else {
          print "No longer generated: ChatEndpoints." key " (kept as vendored)"
          printf "\n%s", vendored[key] > out
        }
      }
      print "}" > out
      print "Refreshed ChatEndpoints: " count " functions"
    }
  ' "$VENDORED_API" "$GENERATED_DIR/apis/ChatEndpoints.kt" || exit 1
  mv "$BUILD_DIR/ChatEndpoints.kt" "$VENDORED_API"
fi

# Models the vendored ones reference but which aren't vendored yet: the build fails until they are.
{ grep -ho "io\.getstream\.chat\.android\.network\.models\.[A-Z][A-Za-z0-9_]*" \
  "$VENDORED_DIR"/models/*.kt "$VENDORED_DIR"/infrastructure/*.kt "$VENDORED_API" || true; } |
  sed 's/.*\.//' | sort -u |
  while IFS= read -r name; do
    if [[ ! -f "$VENDORED_DIR/models/$name.kt" ]]; then
      echo "Referenced but not vendored: models/$name.kt (copy it from $GENERATED_DIR/models)"
    fi
  done

# Step 4: format like the rest of the module.
(cd "$PROJECT_ROOT" && ./gradlew -q :stream-chat-android-client:spotlessApply)

echo "Refreshed the vendored files from backend $BACKEND_SHA: git status shows what changed. Review the diff, and"
echo "update the baseline-prof.txt entries of any model whose properties changed."
