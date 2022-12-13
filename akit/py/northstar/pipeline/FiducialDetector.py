from typing import List

import cv2
from northstar.config.config import ConfigStore
from northstar.types import FiducialImageObservation


class FiducialDetector:
    def detect_fiducials(self, image: cv2.Mat, config_store: ConfigStore) -> List[FiducialImageObservation]:
        return []
