from dataclasses import dataclass
import numpy
import numpy.typing


@dataclass(frozen=True)
class FiducialImageObservation:
    tag_id: int
    corners: numpy.typing.NDArray[numpy.float64]


@dataclass(frozen=True)
class FiducialPoseObservation:
    tag_id: int
    tvec: numpy.typing.NDArray[numpy.float64]
    rvec: numpy.typing.NDArray[numpy.float64]
