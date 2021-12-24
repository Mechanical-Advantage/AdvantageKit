# Publishes all artifacts to maven local to enable rapid testing with a GradleRIO FRC project

bazel run //conduit/api:api-export.publish
bazel run //conduit/wpilibio:nativezip.publish -c opt
bazel run //junction/core:core-export.publish
bazel run //junction/shims/wpilib:wpilib-export.publish