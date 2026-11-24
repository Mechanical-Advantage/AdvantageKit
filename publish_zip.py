# Copyright (c) 2021-2026 Littleton Robotics
# http://github.com/Mechanical-Advantage
#
# Use of this source code is governed by a BSD
# license that can be found in the LICENSE file
# at the root directory of this project.

import argparse
import requests
from requests.auth import HTTPBasicAuth
import hashlib
import os
import sys

# Parse command line arguments
parser = argparse.ArgumentParser()
parser.add_argument("--url", help="URL of maven repository")
parser.add_argument("--group_id", help="Group ID of the artifact")
parser.add_argument("--artifact_id", help="Artifact ID of the artifact")
parser.add_argument("--version", help="Version of the artifact")
parser.add_argument("--classifier", help="Classifier of the artifact")
parser.add_argument("--file_path", help="File path of the zip file to be published")
parser.add_argument("--username", help="Maven username")
parser.add_argument("--password", help="Maven password")
args = parser.parse_args()

# Upload URL
url = f"{args.url}/{args.group_id.replace('.', '/')}/{args.artifact_id}/{args.version}/{args.artifact_id}-{args.version}-{args.classifier}.zip"

# Read the contents of the zip file
with open(args.file_path, "rb") as f:
    zip_data = f.read()

# Generate the SHA-256 hash of the zip file
sha256_hash = hashlib.sha256(zip_data).hexdigest()

# Generate the MD5 hash of the zip file
md5_hash = hashlib.md5(zip_data).hexdigest()

# Create the contents of the sha256 file
sha256_content = sha256_hash.encode()

# Create the contents of the md5 file
md5_content = md5_hash.encode()

file_prefix = "file://"
if url.startswith(file_prefix):
    # Write zip file
    zip_path = url[len(file_prefix):]
    dest_dir = os.path.dirname(zip_path)
    os.makedirs(dest_dir, exist_ok=True)
    with open(url[len(file_prefix):], "wb") as f:
        f.write(zip_data)

    # Write sha256 file
    sha256_path = url[len(file_prefix):] + ".sha256"
    with open(sha256_path, "wb") as f:
        f.write(sha256_content)

    # Write md5 file
    md5_path = url[len(file_prefix):] + ".md5"
    with open(md5_path, "wb") as f:
        f.write(md5_content)

else:
    # Set up auth for the HTTP requests
    auth = HTTPBasicAuth(args.username, args.password)

    def publish_file(file_url, content, label):
        try:
            response = requests.put(file_url, auth=auth, data=content, timeout=30)
        except requests.RequestException as exc:
            print(f"Error uploading {label}: {exc}", file=sys.stderr)
            return False

        if response.status_code not in (200, 201, 204):
            message = response.content.decode(errors="replace")
            print(f"Error uploading {label}: {response.status_code} - {message}", file=sys.stderr)
            return False

        print(f"{label} published to Maven.")
        return True

    # Only publish checksums after the archive was successfully uploaded. Fail the
    # publishing process if any request is unsuccessful so CI cannot report success.
    if not publish_file(url, zip_data, "Zip file"):
        sys.exit(1)
    if not publish_file(f"{url}.sha256", sha256_content, "SHA-256 file"):
        sys.exit(1)
    if not publish_file(f"{url}.md5", md5_content, "MD5 file"):
        sys.exit(1)
