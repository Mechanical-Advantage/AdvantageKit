import json

import cv2
import ntcore
import numpy

from config.config import ConfigStore, RemoteConfig


class ConfigSource:
    def update(self, config_store: ConfigStore) -> None:
        raise NotImplementedError


class FileConfigSource(ConfigSource):
    CONFIG_FILENAME = "config.json"
    CALIBRATION_FILENAME = "calibration.json"

    def __init__(self) -> None:
        pass

    def update(self, config_store: ConfigStore) -> None:
        # Get config
        with open(self.CONFIG_FILENAME, "r") as config_file:
            config_data = json.loads(config_file.read())
            config_store.local_config.device_id = config_data["device_id"]
            config_store.local_config.server_ip = config_data["server_ip"]
            config_store.local_config.stream_port = config_data["stream_port"]

        # Get calibration
        calibration_store = cv2.FileStorage(self.CALIBRATION_FILENAME, cv2.FILE_STORAGE_READ)
        camera_matrix = calibration_store.getNode("camera_matrix").mat()
        distortion_coefficients = calibration_store.getNode("distortion_coefficients").mat()
        calibration_store.release()
        if type(camera_matrix) == numpy.ndarray and type(distortion_coefficients) == numpy.ndarray:
            config_store.local_config.camera_matrix = camera_matrix
            config_store.local_config.distortion_coefficients = distortion_coefficients
            config_store.local_config.has_calibration = True


class NTConfigSource(ConfigSource):
    _init_complete: bool = False
    _camera_id_sub: ntcore.IntegerSubscriber
    _camera_resolution_width_sub: ntcore.IntegerSubscriber
    _camera_resolution_height_sub: ntcore.IntegerSubscriber
    _camera_auto_exposure_sub: ntcore.IntegerSubscriber
    _camera_exposure_sub: ntcore.IntegerSubscriber
    _camera_gain_sub: ntcore.IntegerSubscriber
    _fiducial_size_m_sub: ntcore.DoubleSubscriber
    _tag_layout_sub: ntcore.DoubleSubscriber

    def update(self, config_store: ConfigStore) -> None:
        # Initialize subscribers on first call
        if not self._init_complete:
            nt_table = ntcore.NetworkTableInstance.getDefault().getTable(
                "/" + config_store.local_config.device_id + "/config")
            self._camera_id_sub = nt_table.getIntegerTopic("camera_id").subscribe(RemoteConfig.camera_id)
            self._camera_resolution_width_sub = nt_table.getIntegerTopic(
                "camera_resolution_width").subscribe(RemoteConfig.camera_resolution_width)
            self._camera_resolution_height_sub = nt_table.getIntegerTopic(
                "camera_resolution_height").subscribe(RemoteConfig.camera_resolution_height)
            self._camera_auto_exposure_sub = nt_table.getIntegerTopic(
                "camera_auto_exposure").subscribe(RemoteConfig.camera_auto_exposure)
            self._camera_exposure_sub = nt_table.getIntegerTopic(
                "camera_exposure").subscribe(RemoteConfig.camera_exposure)
            self._camera_gain_sub = nt_table.getIntegerTopic(
                "camera_gain").subscribe(RemoteConfig.camera_gain)
            self._fiducial_size_m_sub = nt_table.getDoubleTopic(
                "fiducial_size_m").subscribe(RemoteConfig.fiducial_size_m)
            self._tag_layout_sub = nt_table.getStringTopic(
                "tag_layout").subscribe("")
            self._init_complete = True

        # Read config data
        config_store.remote_config.camera_id = self._camera_id_sub.get()
        config_store.remote_config.camera_resolution_width = self._camera_resolution_width_sub.get()
        config_store.remote_config.camera_resolution_height = self._camera_resolution_height_sub.get()
        config_store.remote_config.camera_auto_exposure = self._camera_auto_exposure_sub.get()
        config_store.remote_config.camera_exposure = self._camera_exposure_sub.get()
        config_store.remote_config.camera_gain = self._camera_gain_sub.get()
        config_store.remote_config.fiducial_size_m = self._fiducial_size_m_sub.get()
        try:
            config_store.remote_config.tag_layout = json.loads(self._tag_layout_sub.get())
        except:
            config_store.remote_config.tag_layout = None
            pass
