command_file="build/tmp/unit-tests-command.sh"

#./gradlew test-debug && \
#chmod +x $command_file && \
#./$command_file && \
#rm $command_file
./gradlew :testCoverage
