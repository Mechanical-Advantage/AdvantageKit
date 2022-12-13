from dataclasses import dataclass

@dataclass
class RemoteConfig:
    camera_auto_exposure: bool
    camera_exposure: float

@dataclass
class LocalConfig:
    device_id: str
    server_ip: str
    resolution_width: int
    resolution_height: int
    camera_path: str

@dataclass
class ConfigStore:
    remote_config: RemoteConfig
    local_config: LocalConfig