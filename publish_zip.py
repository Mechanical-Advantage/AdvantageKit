import argparse
import requests
import hashlib
import os

# Parse command line arguments
parser = argparse.ArgumentParser()
parser.add_argument("--url", help="URL of maven repository")
parser.add_argument("--group_id", help="GroupID of the artifact")
parser.add_argument("--artifact_id", help="Artifact ID of the artifact")
parser.add_argument("--version", help="Version of the artifact")
parser.add_argument("--classifier", help="Classifier of the artifact")
parser.add_argument("--file_path", help="File path of the zip file to be published")
parser.add_argument("--username", help="GitHub username")
parser.add_argument("--access_token", help="GitHub personal access token")
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
    # Set the headers for the HTTP requests
    headers = {
        "Authorization": f"token {args.access_token}",
        "Content-Type": "application/octet-stream",
        "Accept": "application/vnd.github.v3+json"
    }

    # Make the HTTP request to publish the zip file
    response_zip = requests.put(url, headers=headers, data=zip_data)

    # Check the status code of the response
    if response_zip.status_code == 200 or response_zip.status_code == 201:
        print("Zip file published to GitHub Packages Maven.")
    else:
        print(f"Error: {response_zip.status_code} - {response_zip.content.decode()}")

    # Make the HTTP request to publish the sha256 file
    response_sha256 = requests.put(f"{url}.sha256", headers=headers, data=sha256_content)

    # Check the status code of the response
    if response_sha256.status_code == 200 or response_sha256.status_code == 201:
        print("SHA-256 file published to GitHub Packages Maven.")
    else:
        print(f"Error: {response_sha256.status_code} - {response_sha256.content.decode()}")

    # Make the HTTP request to publish the md5 file
    response_md5 = requests.put(f"{url}.md5", headers=headers, data=md5_content)

    # Check the status code of the response
    if response_md5.status_code == 200 or response_md5.status_code == 201:
        print("MD5 file published to GitHub Packages Maven.")
    else:
        print(f"Error: {response_md5.status_code} - {response_md5.content.decode()}")
