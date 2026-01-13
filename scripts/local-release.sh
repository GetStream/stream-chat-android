#!/bin/bash
# Get the directory where the script is located
script_dir="$(dirname "$0")"
# Change to the scripts directory
cd "$script_dir" || exit
# Go to the buildSrc dir where the Configuration.kt file is located
configuration_dir="../buildSrc/src/main/kotlin/io/getstream/chat/android/"
configuration_file="Configuration.kt"
cd $configuration_dir || exit

# Start time
start_time=$(date +%s)

# Function to print formatted time
format_time() {
  local seconds=$1
  local hours=$((seconds / 3600))
  local minutes=$((seconds % 3600 / 60))
  local seconds=$((seconds % 60))

  if [ $hours -gt 0 ]; then
    printf "%dh %dm %d s" $hours $minutes $seconds
  else
    printf "%dm %ds" $minutes $seconds
  fi
}

# Function to extract version components
extract_version_components() {
  grep "const val $1" "$configuration_file" | awk -F"=" '{print $2}' | tr -d ' ;'
}

# Extracting the major, minor and patchVersion
majorVersion=$(extract_version_components "majorVersion")
minorVersion=$(extract_version_components "minorVersion")
patchVersion=$(extract_version_components "patchVersion")
versionName=$(extract_version_components "versionName")

# Automatically increment the patch version
patchVersion=$((patchVersion+1))

# Combine into a new version string
extracted_version="$majorVersion.$minorVersion.$patchVersion"

# Function to validate the version format
validate_version_format() {
  if [[ $1 =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    return 0 # Valid
  else
    return 1 # Invalid
  fi
}

# Prompting the user for enter version name input (optional)
while true; do
  read -p "Enter version (Press Enter to use $extracted_version or format X.Y.Z): " input_version
  # Use the extracted version if no input is provided
  if [ -z "$input_version" ]; then
    version=$extracted_version
    break
  elif validate_version_format "$input_version"; then
    version=$input_version
    break
  else
    echo "Invalid version format. Please use the format X.Y.Z (e.g., 1.2.3)."
  fi
done

# Use the extracted version if no input is provided
version=${input_version:-$extracted_version}

IFS='.' read -r input_majorVersion input_minorVersion input_patchVersion <<< "$version"

# Function to update version components in the file
update_version_in_file() {
  local component=$1
  local value=$2
  if [[ "$OSTYPE" == "darwin"* ]]; then
    sed -i '' "s/const val $component =.*/const val $component = $value/" "$configuration_file"
  else
    sed -i "s/const val $component =.*/const val $component = $value/" "$configuration_file"
  fi
}

# Get current date in a specific format, YYYYMMDDHHMMSS to use as suffix
current_date=$(date +%Y%m%d%H%M%S)
version_name="$input_majorVersion.$input_minorVersion.$input_patchVersion-local-$current_date"
# Update the version components in the Configuration.kt file
update_version_in_file "majorVersion" "$input_majorVersion"
update_version_in_file "minorVersion" "$input_minorVersion"
update_version_in_file "patchVersion" "$input_patchVersion"
update_version_in_file "versionName" "\"$version_name\""

echo -e "Version updated to: \033[33m$version_name\033[0m"

# Move back to the root of the project to be able to run ./gradlew
# Change to the script's directory
cd ../../../../../../../../scripts || exit
gradleFilePath="publish-module.gradle"
# Create a backup of the original .gradle file
backupFilePath="${gradleFilePath}.backup"
cp "$gradleFilePath" "$backupFilePath"

# Pattern that marks the beginning of the block to remove
startPattern="signing {"

# Pattern that marks the end of the block to remove
endPattern="}"
# sed to remove the block from the startPattern to the endPattern, inclusive
if [[ "$OSTYPE" == "darwin"* ]]; then
  sed -i '' "/$startPattern/,/$endPattern/d" "$gradleFilePath"
else
  sed -i "/$startPattern/,/$endPattern/d" "$gradleFilePath"
fi

# Move back to root
cd ..
# Define the modules to release locally
modules=(
  "stream-chat-android-client"
  "stream-chat-android-compose"
  "stream-chat-android-core"
  "stream-chat-android-markdown-transformer"
  "stream-chat-android-previewdata"
  "stream-chat-android-offline"
  "stream-chat-android-state"
  "stream-chat-android-ui-common"
  "stream-chat-android-ui-components"
  "stream-chat-android-ui-utils"
)

# Function to restore the modified fields in the config file and the gradle file
restore_modified_fields() {
  # Restore publish-module.gradle
  cd "$script_dir" || exit
  cp "$backupFilePath" "$gradleFilePath"
  rm "$backupFilePath"
  # Restore Configuration.kt
  cd ..
  cd "$script_dir" || exit
  cd "$configuration_dir" || exit
  patchVersion=$((patchVersion-1))
  update_version_in_file "majorVersion" "$majorVersion"
  update_version_in_file "minorVersion" "$minorVersion"
  update_version_in_file "patchVersion" "$patchVersion"
  update_version_in_file "versionName" "$versionName"
}

total=${#modules[@]}
completed=0
echo "Publishing to MavenLocal"
# Initial display of the progress bar
printf "[%-20s] %3s%%" "" "0"
echo -n $'\r'
sleep 5 # For effect :)

for module in "${modules[@]}"; do
  #Progress
  ((completed++))
  percent=$((completed * 100 / total))
  bar=$((completed * 20 / total)) # Assuming a 20-char width progress bar
  filled=$(printf '%0.s█' $(seq 1 $bar))
  unfilled=$(printf '%0.s ' $(seq 1 $((20 - bar))))
  # Adjust unfilled to include only necessary spaces
  if [ $bar -eq 20 ]; then
    unfilled=""
  fi
  printf "\r[%s%s] %3d%%" "$filled" "$unfilled" "$percent"

  ./gradlew "${module}":publishToMavenLocal -x test > /dev/null
  if [ $? -ne 0 ]; then
    echo -e "\033[31mPublishing $module failed.\033[0m"
    restore_modified_fields
    echo -e "\033[41m\033[97mBUILD FAILED\033[0m\033[31m: One or more modules failed to publish.\033[0m"
    exit 1
  fi
done

restore_modified_fields
echo ""

# Calculate elapsed time
current_time=$(date +%s)
elapsed_seconds=$((current_time - start_time))
elapsed_time=$(format_time $elapsed_seconds)

echo "Gradle usage:"
for module in "${modules[@]}"; do
  echo -e '\033[33mimplementation\033[0m\033[37m(\033[0m"\033[32mio.getstream:'"$module"':'"$version_name"'\033[0m\033[37m")\033[0m'
done
echo "Elapsed time: $elapsed_time"
echo -e "\033[42m\033[97mBUILD SUCCESS\033[0m\033[32m: All modules published successfully.\033[0m"
