command_file="unit-tests-command.sh"

./gradlew dag-command selected-test-debug && \
chmod +x $command_file && \
./$command_file

rm $command_file
