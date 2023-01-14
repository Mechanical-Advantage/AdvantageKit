import cv2
import numpy
from config.config import ConfigStore
from vision_types import FiducialImageObservation, FiducialPoseObservation


def overlay_image_observation(image: cv2.Mat, observation: FiducialImageObservation) -> None:
    cv2.aruco.drawDetectedMarkers(image, numpy.array([observation.corners]), numpy.array([observation.tag_id]))


def overlay_pose_observation(image: cv2.Mat, config_store: ConfigStore, observation: FiducialPoseObservation) -> None:
    cv2.drawFrameAxes(image, config_store.local_config.camera_matrix,
                      config_store.local_config.distortion_coefficients, observation.rvec_0, observation.tvec_0, config_store.remote_config.fiducial_size_m / 2)
    cv2.drawFrameAxes(image, config_store.local_config.camera_matrix,
                      config_store.local_config.distortion_coefficients, observation.rvec_1, observation.tvec_1, config_store.remote_config.fiducial_size_m / 2)
