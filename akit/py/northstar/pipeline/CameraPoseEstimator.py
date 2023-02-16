from typing import List
import cv2
import numpy
from config.config import ConfigStore
from pipeline.convert_poses import openCvPoseToWpilib, wpilibTranslationToOpenCv
from vision_types import CameraPoseObservation, FiducialImageObservation, FiducialPoseObservation
from wpimath.geometry import *


class CameraPoseEstimator:
    def __init__(self) -> None:
        raise NotImplementedError

    def solve_camera_pose(self, image_observations: List[FiducialImageObservation], config_store: ConfigStore) -> CameraPoseObservation:
        raise NotImplementedError


class MultiTargetCameraPoseEstimator(CameraPoseEstimator):
    def __init__(self) -> None:
        pass

    def solve_camera_pose(self, image_observations: List[FiducialImageObservation], config_store: ConfigStore) -> CameraPoseObservation:
        if config_store.remote_config.tag_layout == None:
            return CameraPoseObservation(Pose3d(), 0, Pose3d(), 0)

        fid_size = config_store.remote_config.fiducial_size_m
        object_points = []
        image_points = []
        for observation in image_observations:
            tag_pose = None
            for tag_data in config_store.remote_config.tag_layout["tags"]:
                if tag_data["ID"] == observation.tag_id:
                    tag_pose = Pose3d(
                        Translation3d(
                            tag_data["pose"]["translation"]["x"],
                            tag_data["pose"]["translation"]["y"],
                            tag_data["pose"]["translation"]["z"]
                        ),
                        Rotation3d(Quaternion(
                            tag_data["pose"]["rotation"]["quaternion"]["W"],
                            tag_data["pose"]["rotation"]["quaternion"]["X"],
                            tag_data["pose"]["rotation"]["quaternion"]["Y"],
                            tag_data["pose"]["rotation"]["quaternion"]["Z"]
                        )))
            if tag_pose != None:
                corner_0 = tag_pose + Transform3d(Translation3d(0, fid_size / 2.0, -fid_size / 2.0), Rotation3d())
                corner_1 = tag_pose + Transform3d(Translation3d(0, -fid_size / 2.0, -fid_size / 2.0), Rotation3d())
                corner_2 = tag_pose + Transform3d(Translation3d(0, -fid_size / 2.0, fid_size / 2.0), Rotation3d())
                corner_3 = tag_pose + Transform3d(Translation3d(0, fid_size / 2.0, fid_size / 2.0), Rotation3d())
                object_points += [
                    wpilibTranslationToOpenCv(corner_0.translation()),
                    wpilibTranslationToOpenCv(corner_1.translation()),
                    wpilibTranslationToOpenCv(corner_2.translation()),
                    wpilibTranslationToOpenCv(corner_3.translation())
                ]

                image_points += [
                    [observation.corners[0][0][0], observation.corners[0][0][1]],
                    [observation.corners[0][1][0], observation.corners[0][1][1]],
                    [observation.corners[0][2][0], observation.corners[0][2][1]],
                    [observation.corners[0][3][0], observation.corners[0][3][1]]
                ]

        _, rvecs, tvecs, errors = cv2.solvePnPGeneric(numpy.array(object_points), numpy.array(image_points),
                                                      config_store.local_config.camera_matrix, config_store.local_config.distortion_coefficients, flags=cv2.SOLVEPNP_SQPNP)
        print()
        print(numpy.array(object_points))
        print(numpy.array(image_points))
        print(tvecs[0])
        # print(openCvPoseToWpilib(tvecs[0], rvecs[0]))

        # object_points = numpy.array([[-fid_size / 2.0, fid_size / 2.0, 0.0],
        #                              [fid_size / 2.0, fid_size / 2.0, 0.0],
        #                              [fid_size / 2.0, -fid_size / 2.0, 0.0],
        #                              [-fid_size / 2.0, -fid_size / 2.0, 0.0]])

        # _, rvecs, tvecs, errors = cv2.solvePnPGeneric(object_points, image_observation.corners, config_store.local_config.camera_matrix,
        #                                               config_store.local_config.distortion_coefficients, flags=cv2.SOLVEPNP_IPPE_SQUARE)
        # return FiducialPoseObservation(image_observation.tag_id, tvecs[0], rvecs[0], errors[0][0], tvecs[1], rvecs[1], errors[1][0])

        return CameraPoseObservation(Pose3d(), 0, Pose3d(), 0)
