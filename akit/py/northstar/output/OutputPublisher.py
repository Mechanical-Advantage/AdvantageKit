import math
from typing import List, Union

import ntcore
from config.config import ConfigStore
from vision_types import CameraPoseObservation, FiducialPoseObservation


class OutputPublisher:
    def send(self, config_store: ConfigStore, timestamp: float, observation: Union[CameraPoseObservation, None], fps: Union[int, None] = None) -> None:
        raise NotImplementedError


class NTOutputPublisher(OutputPublisher):
    _init_complete: bool = False
    _observations_pub: ntcore.DoubleArrayPublisher
    _fps_pub: ntcore.IntegerPublisher

    def send(self, config_store: ConfigStore, timestamp: float, observation: Union[CameraPoseObservation, None], fps: Union[int, None] = None) -> None:
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
        observation_data: List[float] = [0]
        if observation != None:
            observation_data[0] = 1
            observation_data.append(observation.error_0)
            observation_data.append(observation.pose_0.translation().X())
            observation_data.append(observation.pose_0.translation().Y())
            observation_data.append(observation.pose_0.translation().Z())
            observation_data.append(observation.pose_0.rotation().getQuaternion().W())
            observation_data.append(observation.pose_0.rotation().getQuaternion().X())
            observation_data.append(observation.pose_0.rotation().getQuaternion().Y())
            observation_data.append(observation.pose_0.rotation().getQuaternion().Z())
            if observation.error_1 != None and observation.pose_1 != None:
                observation_data[0] = 2
                observation_data.append(observation.error_1)
                observation_data.append(observation.pose_1.translation().X())
                observation_data.append(observation.pose_1.translation().Y())
                observation_data.append(observation.pose_1.translation().Z())
                observation_data.append(observation.pose_1.rotation().getQuaternion().W())
                observation_data.append(observation.pose_1.rotation().getQuaternion().X())
                observation_data.append(observation.pose_1.rotation().getQuaternion().Y())
                observation_data.append(observation.pose_1.rotation().getQuaternion().Z())
            for tag_id in observation.tag_ids:
                observation_data.append(tag_id)
        self._observations_pub.set(observation_data, math.floor(timestamp * 1000000))
