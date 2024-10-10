---
sidebar_position: 3
---

# More Examples

6328 has discussed several examples of how we've used logging in our [OpenAlliance 2022 build thread](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread). Here are some highlights:

- While working between shop meetings, we were able to debug a subtle issue with the code's vision pipeline by recording extra outputs from a log saved from a previous testing session. [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/21#logging).
- During an event, we retuned our odometry pipeline to more aggressively adjust based on vision data (tuning that requires a normal match environment with other robots). We tested the change using replay and deployed it in the next match, and saw much more reliable odometry data for the remainder of the event. [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/112#odometry-tuning).
- After an event, we determined that our hood was not zeroing currently during match conditions. This involved replaying the matches with a manually adjusted hood position and comparing the quality of the vision data processed with different angles (the robot's Limelight was mounted to the hood). [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/145#software-jonahb55).
- After adding a new point visualization tool to AdvantageScope, we replayed log files from a previous event and extracted more detailed information from the middle of the vision pipeline. [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/112#odometry-tuning).
- We used log data from across the full season to determine that the FMS adds ~300ms of extra time to auto and teleop (rather than following a strict 15s and 135s). [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/112#how-long-is-a-match)
- We replayed our Einstein matches while pulling in Zebra data to check the accuracy of the on-robot odometry. [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/134#zebra-analysis)
- We compiled log data from the robot's full lifetime to generate some fun statistics. For example, the total energy usage of the robot was equivalent to the potential energy of a grand piano dropped from an airplane at cruising altitude. [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/134#software-update-9-beware-of-falling-pianos).
