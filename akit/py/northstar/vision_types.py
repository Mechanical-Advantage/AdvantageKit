from dataclasses import dataclass
from typing import List, Union
import numpy
import numpy.typing
from wpimath.geometry import *


@dataclass(frozen=True)
class FiducialImageObservation:
    tag_id: int
    corners: numpy.typing.NDArray[numpy.float64]


@dataclass(frozen=True)
class FiducialPoseObservation:
    tag_id: int
    tvec_0: numpy.typing.NDArray[numpy.float64]
    rvec_0: numpy.typing.NDArray[numpy.float64]
    error_0: float
    tvec_1: numpy.typing.NDArray[numpy.float64]
    rvec_1: numpy.typing.NDArray[numpy.float64]
    error_1: float


@dataclass(frozen=True)
class CameraPoseObservation:
    pose_0: Pose3d
    error_0: float
    pose_1: Union[Pose3d, None]
    error_1: Union[float, None]
    tag_ids: List[int]
