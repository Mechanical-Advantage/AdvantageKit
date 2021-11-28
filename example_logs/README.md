# example_logs

This folder contains various example RLOG log files for testing:

* [robotlog.rlog](/example_logs/robotlog.rlog) (28s) - Simple test on the kit-bot, driving forwards, backwards, and spinning in place. Contains no metadata.
* [robotlog_simulated.rlog](/example_logs/robotlog_simulated.rlog) (28s) - Simulated version of "robotlog.rlog" to add odometry data. Contains only replay metadata.
* [robotlog_invalid.rlog](/example_logs/robotlog_invalid.rlog) (28s) - Same as above, but with log revision zero. Decoding devices should detect the invalid revision and exit cleanly.
* [odometrylog.rlog](/example_logs/odometrylog.rlog) (67s) - Simple log from the kit-bot, but following a longer and more interesting path to test odometry.
* [longodometrylog.rlog](/example_logs/longodometrylog.rlog) (20m) - Extended version of the same log, to test handling of larger amounts of data.
* [longodometrylog_simulated.rlog](/example_logs/longodometrylog_simulated.rlog) (20m) - Simulated version of the same log.
* [noiselog.rlog](/example_log/noiselog.rlog) (76s) - Includes 64 double fields with random data each cycle.