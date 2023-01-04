

import cv2

from config.config import ConfigStore, LocalConfig, RemoteConfig
from pipeline.Capture import DefaultCapture
from pipeline.FiducialDetector import ArucoFiducialDetector
from pipeline.PoseEstimator import SquareTargetPoseEstimator

if __name__ == "__main__":
    filename = "calibration.json"
    fs = cv2.FileStorage(filename, cv2.FILE_STORAGE_READ)
    camera_matrix = fs.getNode("camera_matrix").mat()
    dist_coeffs = fs.getNode("dist_coeffs").mat()
    fs.release()

    config = ConfigStore(RemoteConfig(1, 1600, 1200, 1, 10, 6 * 0.0254), LocalConfig("northstar", "127.0.0.1", camera_matrix, dist_coeffs))

    capture = DefaultCapture()
    fiducial_detector = ArucoFiducialDetector(cv2.aruco.DICT_APRILTAG_16h5)
    pose_estimator = SquareTargetPoseEstimator()

    while True:
        success, image = capture.get_frame(config)
        if success:
            observations = fiducial_detector.detect_fiducials(image, config)
            pose_observations = [pose_estimator.solve_fiducial_pose(x, config) for x in observations]
            print(pose_observations)
        else:
            print("Failed")
