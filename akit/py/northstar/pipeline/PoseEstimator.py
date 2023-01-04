import cv2
import numpy
from config.config import ConfigStore
from vision_types import FiducialImageObservation, FiducialPoseObservation


class PoseEstimator:
    def __init__(self) -> None:
        raise NotImplementedError

    def solve_fiducial_pose(self, image_observation: FiducialImageObservation, config_store: ConfigStore) -> FiducialPoseObservation:
        raise NotImplementedError


class SquareTargetPoseEstimator(PoseEstimator):
    def __init__(self) -> None:
        pass

    def solve_fiducial_pose(self, image_observation: FiducialImageObservation, config_store: ConfigStore) -> FiducialPoseObservation:
        fid_size = config_store.remote_config.fiducial_size_m
        object_points = numpy.array([[-fid_size / 2.0, fid_size / 2.0, 0.0],
                                     [fid_size / 2.0, fid_size / 2.0, 0.0],
                                     [fid_size / 2.0, -fid_size / 2.0 , 0.0],
                                     [-fid_size / 2.0, -fid_size / 2.0 , 0.0]])

        _, rvec, tvec = cv2.solvePnP(object_points, image_observation.corners, config_store.local_config.camera_matrix, config_store.local_config.distortion_coefficients, flags=cv2.SOLVEPNP_IPPE_SQUARE)
        return FiducialPoseObservation(image_observation.tag_id, tvec, rvec)
