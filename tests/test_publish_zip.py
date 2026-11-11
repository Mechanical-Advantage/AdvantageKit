"""Offline regression tests for the Maven publish helper."""

import hashlib
import io
from contextlib import redirect_stderr, redirect_stdout
from pathlib import Path
import runpy
import sys
import tempfile
import unittest
from unittest.mock import Mock, patch

import requests

SCRIPT = Path(__file__).resolve().parents[1] / "publish_zip.py"


class PublishZipTest(unittest.TestCase):
    def setUp(self):
        self.tempdir = tempfile.TemporaryDirectory()
        self.addCleanup(self.tempdir.cleanup)
        self.archive = Path(self.tempdir.name) / "artifact.zip"
        self.archive.write_bytes(b"zip-test-bytes")
        self.url = "https://maven.test/repository"

    def run_publisher(self, responses, base_url=None):
        args = [
            str(SCRIPT),
            "--url", base_url or self.url,
            "--group_id", "org.example.frc",
            "--artifact_id", "test-lib",
            "--version", "1.2.3",
            "--classifier", "linuxx86-64",
            "--file_path", str(self.archive),
            "--username", "test-user",
            "--password", "test-pass",
        ]
        results = iter(responses)
        calls = []

        def fake_put(url, **kwargs):
            calls.append((url, kwargs))
            result = next(results)
            if isinstance(result, Exception):
                raise result
            return Mock(status_code=result, content=b"upload rejected")

        stdout, stderr = io.StringIO(), io.StringIO()
        with patch.object(sys, "argv", args), patch("requests.put", side_effect=fake_put), redirect_stdout(stdout), redirect_stderr(stderr):
            try:
                runpy.run_path(str(SCRIPT), run_name="__main__")
            except SystemExit as exc:
                exit_status = exc.code
            else:
                exit_status = 0
        return exit_status, calls, stdout.getvalue(), stderr.getvalue()

    def test_all_three_http_uploads_succeed_with_200_201_or_204(self):
        code, calls, stdout, stderr = self.run_publisher([200, 201, 204])
        self.assertEqual(code, 0)
        self.assertEqual(len(calls), 3)
        self.assertEqual([c[0].rsplit(".zip", 1)[-1] for c in calls], ["", ".sha256", ".md5"])
        for _, kwargs in calls:
            self.assertEqual(kwargs["timeout"], 30)
        self.assertEqual(stderr, "")
        self.assertIn("Zip file published", stdout)

    def test_failure_to_upload_archive_fails_and_skips_checksums(self):
        code, calls, _, stderr = self.run_publisher([403])
        self.assertEqual(code, 1)
        self.assertEqual(len(calls), 1)
        self.assertIn("403", stderr)

    def test_failure_to_upload_sha256_fails_and_skips_md5(self):
        code, calls, _, stderr = self.run_publisher([201, 500])
        self.assertEqual(code, 1)
        self.assertEqual(len(calls), 2)
        self.assertIn("SHA-256", stderr)

    def test_failure_to_upload_md5_fails(self):
        code, calls, _, stderr = self.run_publisher([201, 201, 409])
        self.assertEqual(code, 1)
        self.assertEqual(len(calls), 3)
        self.assertIn("MD5", stderr)

    def test_network_timeout_fails_without_uploading_checksums(self):
        code, calls, _, stderr = self.run_publisher([requests.Timeout("timed out")])
        self.assertEqual(code, 1)
        self.assertEqual(len(calls), 1)
        self.assertIn("timed out", stderr)

    def test_file_url_does_not_use_http_and_writes_checksums(self):
        path = Path(self.tempdir.name) / "maven"
        code, calls, _, _ = self.run_publisher([], base_url="file://" + str(path))
        self.assertEqual(code, 0)
        self.assertEqual(calls, [])
        output = path / "org/example/frc/test-lib/1.2.3/test-lib-1.2.3-linuxx86-64.zip"
        data = self.archive.read_bytes()
        self.assertEqual(output.read_bytes(), data)
        self.assertEqual((Path(str(output) + ".sha256")).read_text(), hashlib.sha256(data).hexdigest())
        self.assertEqual((Path(str(output) + ".md5")).read_text(), hashlib.md5(data).hexdigest())


if __name__ == "__main__":
    unittest.main()
