# Publishes all artifacts to maven local to enable rapid testing with a GradleRIO FRC project
# Calling the script with "athena" in the arguments will additionally build for athena

# This script skips building a javadoc jar for published dependencies since it takes forever
# and there is no real need for it if you have the source open anyways.

bazel run --define "publishing_javadoc=false" //conduit/api:api-export.publish
bazel run --define "publishing_javadoc=false" //conduit/wpilibio:pom.publish
bazel run --define "publishing_javadoc=false" //conduit/wpilibio:nativezip.publish -c opt
bazel run --define "publishing_javadoc=false" //junction/core:core-export.publish
bazel run --define "publishing_javadoc=false" //junction/shims/wpilib:wpilib-export.publish
if [[ "$*" == athena ]]
then
    bazel run --define "publishing_javadoc=false" --config=athena //conduit/wpilibio:nativezip.publish -c opt
fi