from calibration.CalibrationSession import CalibrationSession
import os
import cv2

FOLDER_NAME = "manual_images"


if __name__ == "__main__":
    calibration_session = CalibrationSession()
    for filename in [x for x in os.listdir(FOLDER_NAME) if not x.startswith(".")]:
        print("Calibrating with \"" + filename + "\"")
        image = cv2.imread(os.path.join(FOLDER_NAME, filename))
        calibration_session.process_frame(image, True)
    calibration_session.finish()
    print("Finished calibration")
