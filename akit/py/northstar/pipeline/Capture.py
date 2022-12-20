from typing import Tuple

import cv2
import numpy
from northstar.config.config import ConfigStore


class Capture:
    """Interface for receiving camera frames."""

    def __init__(self) -> None:
        raise NotImplementedError

    def get_frame(self, config_store: ConfigStore) -> Tuple[bool, cv2.Mat]:
        """Return the next frame from the camera."""
        raise NotImplementedError

    @classmethod
    def _config_changed(cls, config_a: ConfigStore, config_b: ConfigStore) -> bool:
        if config_a == None and config_b == None:
            return False
        if config_a == None or config_b == None:
            return True

        remote_a = config_a.remote_config
        remote_b = config_b.remote_config

        return remote_a.camera_id != remote_b.camera_id or remote_a.camera_resolution_width != remote_b.camera_resolution_width or remote_a.camera_resolution_height != remote_b.camera_resolution_height or remote_a.camera_auto_exposure != remote_b.camera_auto_exposure or remote_a.camera_exposure != remote_b.camera_exposure


class DefaultCapture(Capture):
    """"Read from camera with default OpenCV config."""

    def __init__(self) -> None:
        pass

    _video = None
    _last_config: ConfigStore

    def get_frame(self, config_store: ConfigStore) -> Tuple[bool, cv2.Mat]:
        if self._video != None and self._config_changed(self._last_config, config_store):
            self._video.release()
            self._video = None

        if self._video == None:
            self._video = cv2.VideoCapture(config_store.remote_config.camera_id)
            self._video.set(cv2.CAP_PROP_FRAME_WIDTH, config_store.remote_config.camera_resolution_width)
            self._video.set(cv2.CAP_PROP_FRAME_HEIGHT, config_store.remote_config.camera_resolution_height)
            self._video.set(cv2.CAP_PROP_AUTO_EXPOSURE, config_store.remote_config.camera_auto_exposure)
            self._video.set(cv2.CAP_PROP_EXPOSURE, config_store.remote_config.camera_exposure)

        self._last_config = config_store
      
        retval, image = self._video.read()
        return retval, image


class GStreamerCapture(Capture):
    """"Read from camera with GStreamer."""

    def __init__(self) -> None:
        pass

    _video = None
    _last_config: ConfigStore
   
    def get_frame(self, config_store: ConfigStore) -> Tuple[bool, cv2.Mat]:
        if self._video != None and self._config_changed(self._last_config, config_store):
            self._video.release()
            self._video = None

        if self._video == None:
            self._video = cv2.VideoCapture("v4l2src device=/dev/video" + str(config_store.remote_config.camera_id) + " extra_controls=\"c,auto_exposure=" + str(config_store.remote_config.camera_auto_exposure) + ",exposure_time_absolute=" + str(config_store.remote_config.camera_exposure) + "\" ! image/jpeg, format=MJPG ! jpegdec ! video/x-raw ! appsink drop=1", cv2.CAP_GSTREAMER)

        self._last_config = config_store
      
        retval, image = self._video.read()
        return retval, image
