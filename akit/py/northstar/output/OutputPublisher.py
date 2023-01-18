import math
from typing import List, Union

import ntcore
from config.config import ConfigStore
from vision_types import FiducialPoseObservation


class OutputPublisher:
    def send(self, config_store: ConfigStore, timestamp: float, observations: List[FiducialPoseObservation], fps: Union[int, None] = None) -> None:
        raise NotImplementedError


class NTOutputPublisher(OutputPublisher):
    _init_complete: bool = False
    _observations_pub: ntcore.DoubleArrayPublisher
    _fps_pub: ntcore.IntegerPublisher

    def send(self, config_store: ConfigStore, timestamp: float, observations: List[FiducialPoseObservation], fps: Union[int, None] = None) -> None:
        # Initialize publishers on first call
        if not self._init_complete:
            nt_table = ntcore.NetworkTableInstance.getDefault().getTable(
                "/" + config_store.local_config.device_id + "/output")
            self._observations_pub = nt_table.getDoubleArrayTopic("observations").publish(
                ntcore.PubSubOptions(periodic=0, sendAll=True, keepDuplicates=True))
            self._fps_pub = nt_table.getIntegerTopic("fps").publish()

        # Send data
        if fps != None:
            self._fps_pub.set(fps)
        observation_data: List[float] = []
        for observation in observations:
            observation_data.append(observation.tag_id)
            observation_data += [x[0] for x in observation.tvec_0]
            observation_data += [x[0] for x in observation.rvec_0]
            observation_data.append(observation.error_0)
            observation_data += [x[0] for x in observation.tvec_1]
            observation_data += [x[0] for x in observation.rvec_1]
            observation_data.append(observation.error_1)
        self._observations_pub.set(observation_data, math.floor(timestamp * 1000000))
