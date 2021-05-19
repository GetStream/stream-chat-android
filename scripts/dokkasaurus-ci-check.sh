set -e

  ./gradlew dokkaHtmlMultiModule
  ./gradlew docusaurusSidebar

diff_output=$(git status)

if [[ $diff_output != *"nothing to commit, working tree clean"* ]]; then
  echo "Error! looks like there are changes in your documentation."
  echo "Please update your documentation. You can run ./scripts/dokkasaurus.sh for that."
  exit 1
fi
