command_file="unit-tests-selected-command.sh"

./gradlew dag-command selected-test-debug && \
chmod +x $command_file && \
./$command_file

rm $command_file
