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
    tvec_0: numpy.typing.NDArray[numpy.float64]
    rvec_0: numpy.typing.NDArray[numpy.float64]
    error_0: float
    tvec_1: numpy.typing.NDArray[numpy.float64]
    rvec_1: numpy.typing.NDArray[numpy.float64]
    error_1: float
