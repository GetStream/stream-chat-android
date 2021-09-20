command_file="kt-selected-command.sh"

./gradlew dag-command selected-ktlint && \
chmod +x $command_file && \
./$command_file

rm $command_file
