import numpy
from northstar.config.config import ConfigStore
from northstar.types import FiducialImageObservation, FiducialPoseObservation


class PoseEstimator:
    def solve_fiducial_pose(self, image_observation: FiducialImageObservation, config_store: ConfigStore) -> FiducialPoseObservation:
        return FiducialPoseObservation(0, numpy.array([]), numpy.array([]))


 