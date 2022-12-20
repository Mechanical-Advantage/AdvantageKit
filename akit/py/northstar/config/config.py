from dataclasses import dataclass
import numpy
import numpy.typing

@dataclass
class RemoteConfig:
    camera_id: int
    camera_resolution_width: int
    camera_resolution_height: int
    camera_auto_exposure: int
    camera_exposure: int
    fiducial_size_m : float


@dataclass
class LocalConfig:
    device_id: str
    server_ip: str
    camera_matrix: numpy.typing.NDArray[numpy.float64]
    distortion_coefficients: numpy.typing.NDArray[numpy.float64]


@dataclass
class ConfigStore:
    remote_config: RemoteConfig
    local_config: LocalConfig