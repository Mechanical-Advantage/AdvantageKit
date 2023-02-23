import dataclasses
import sys
import time
from typing import Tuple

import cv2
import numpy
from config.config import ConfigStore


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

        return remote_a.camera_id != remote_b.camera_id or remote_a.camera_resolution_width != remote_b.camera_resolution_width or remote_a.camera_resolution_height != remote_b.camera_resolution_height or remote_a.camera_auto_exposure != remote_b.camera_auto_exposure or remote_a.camera_exposure != remote_b.camera_exposure or remote_a.camera_gain != remote_b.camera_gain


class DefaultCapture(Capture):
    """"Read from camera with default OpenCV config."""

    def __init__(self) -> None:
        pass

    _video = None
    _last_config: ConfigStore

    def get_frame(self, config_store: ConfigStore) -> Tuple[bool, cv2.Mat]:
        if self._video != None and self._config_changed(self._last_config, config_store):
            print("Restarting capture session")
            self._video.release()
            self._video = None

        if self._video == None:
            self._video = cv2.VideoCapture(config_store.remote_config.camera_id)
            self._video.set(cv2.CAP_PROP_FRAME_WIDTH, config_store.remote_config.camera_resolution_width)
            self._video.set(cv2.CAP_PROP_FRAME_HEIGHT, config_store.remote_config.camera_resolution_height)
            self._video.set(cv2.CAP_PROP_AUTO_EXPOSURE, config_store.remote_config.camera_auto_exposure)
            self._video.set(cv2.CAP_PROP_EXPOSURE, config_store.remote_config.camera_exposure)
            self._video.set(cv2.CAP_PROP_GAIN, config_store.remote_config.camera_gain)

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
            print("Config changed, stopping capture session")
            self._video.release()
            self._video = None
            time.sleep(2)

        if self._video == None:
            if config_store.remote_config.camera_id == -1:
                print("No camera ID, waiting to start capture session")
            else:
                print("Starting capture session")
                self._video = cv2.VideoCapture("v4l2src device=/dev/video" + str(config_store.remote_config.camera_id) + " extra_controls=\"c,exposure_auto=" + str(config_store.remote_config.camera_auto_exposure) + ",exposure_absolute=" + str(
                    config_store.remote_config.camera_exposure) + ",gain=" + str(config_store.remote_config.camera_gain) + ",sharpness=0,brightness=0\" ! image/jpeg,format=MJPG,width=" + str(config_store.remote_config.camera_resolution_width) + ",height=" + str(config_store.remote_config.camera_resolution_height) + " ! jpegdec ! video/x-raw ! appsink drop=1", cv2.CAP_GSTREAMER)
                print("Capture session ready")

        self._last_config = ConfigStore(dataclasses.replace(config_store.local_config),
                                        dataclasses.replace(config_store.remote_config))

        if self._video != None:
            retval, image = self._video.read()
            if not retval:
                print("Capture session failed, restarting")
                self._video.release()
                self._video = None  # Force reconnect
                sys.exit(1)
            return retval, image
        else:
            return False, cv2.Mat(numpy.ndarray([]))
