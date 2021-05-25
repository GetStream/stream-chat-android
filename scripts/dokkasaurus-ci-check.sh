set -e

./gradlew dokkaHtmlMultiModule
./gradlew docusaurusSidebar

if [[ $(git status --porcelain | wc -l) -gt 0 ]]; then
  echo "Error! looks like there are changes in your documentation."
  echo "Please update your documentation. You can run for that:"
  echo "./gradlew dokkaHtmlMultiModule"
  echo "./gradlew docusaurusSidebar"
  exit 1
else
  echo "Documentation is up to date"
fi
