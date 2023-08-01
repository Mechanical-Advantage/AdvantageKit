from typing import Union

import cv2
import numpy
from config.config import ConfigStore
from vision_types import FiducialImageObservation, FiducialPoseObservation

from pipeline.coordinate_systems import openCvPoseToWpilib


class PoseEstimator:
    def __init__(self) -> None:
        raise NotImplementedError

    def solve_fiducial_pose(self, image_observation: FiducialImageObservation, config_store: ConfigStore) -> Union[FiducialPoseObservation, None]:
        raise NotImplementedError


class SquareTargetPoseEstimator(PoseEstimator):
    def __init__(self) -> None:
        pass

    def solve_fiducial_pose(self, image_observation: FiducialImageObservation, config_store: ConfigStore) -> Union[FiducialPoseObservation, None]:
        fid_size = config_store.remote_config.fiducial_size_m
        object_points = numpy.array([[-fid_size / 2.0, fid_size / 2.0, 0.0],
                                     [fid_size / 2.0, fid_size / 2.0, 0.0],
                                     [fid_size / 2.0, -fid_size / 2.0, 0.0],
                                     [-fid_size / 2.0, -fid_size / 2.0, 0.0]])

        try:
            _, rvecs, tvecs, errors = cv2.solvePnPGeneric(object_points, image_observation.corners, config_store.local_config.camera_matrix,
                                                          config_store.local_config.distortion_coefficients, flags=cv2.SOLVEPNP_IPPE_SQUARE)
        except:
            return None
        return FiducialPoseObservation(
            image_observation.tag_id,
            openCvPoseToWpilib(tvecs[0], rvecs[0]),
            errors[0][0],
            openCvPoseToWpilib(tvecs[1], rvecs[1]),
            errors[1][0]
        )
