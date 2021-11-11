command_file="unit-tests-command.sh"

./gradlew test-debug && \
chmod +x $command_file && \
./$command_file

# Exit code of gradle test command output
exitCode=$?

rm $command_file

# Exit with test command output to set the GH action's check run status
exit $exitCode
