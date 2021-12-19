
// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MINUTES;

public class MavenZipPublisher {
    private static final int COPY_BUFFER_SIZE = 1024 * 100;

    public static byte[] toByteArray(InputStream source) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            copy(source, bos);
            return bos.toByteArray();
        }
    }

    public static void copy(InputStream source, OutputStream sink) throws IOException {
        byte[] buffer = new byte[COPY_BUFFER_SIZE];

        for (int read = source.read(buffer); read != -1; read = source.read(buffer)) {
            sink.write(buffer, 0, read);
        }
    }

    private static final Logger LOG = Logger.getLogger(MavenZipPublisher.class.getName());
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);
    private static final String[] SUPPORTED_SCHEMES = { "file:/", "https://" };

    public static void main(String[] args)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {
        String repo = args[0];
        if (!isSchemeSupported(repo)) {
            throw new IllegalArgumentException("Repository must be accessed via the supported schemes: "
                    + Arrays.toString(SUPPORTED_SCHEMES));
        }

        Credentials credentials = new Credentials(args[1], args[2]);

        List<String> parts = Arrays.asList(args[3].split(":"));
        if (parts.size() != 3) {
            throw new IllegalArgumentException("Coordinates must be a triplet: " + Arrays.toString(args));
        }

        // In our case this is the architecture the zip is built for
        String classifier = args[4];

        Coordinates coords = new Coordinates(parts.get(0), parts.get(1), parts.get(2), classifier);

        // Calculate md5 and sha1 for each of the inputs
        Path zipFile = Paths.get(args[5]);

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            // futures.add(upload(repo, credentials, coords, ".pom", pom));
            futures.add(upload(repo, credentials, coords, ".zip", zipFile));

            CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            all.get(30, MINUTES);
        } finally {
            EXECUTOR.shutdown();
        }
    }

    private static boolean isSchemeSupported(String repo) {
        for (String scheme : SUPPORTED_SCHEMES) {
            if (repo.startsWith(scheme)) {
                return true;
            }
        }
        return false;
    }

    private static CompletableFuture<Void> upload(
            String repo,
            Credentials credentials,
            Coordinates coords,
            String append,
            Path item) throws IOException, InterruptedException {

        String base = String.format(
                "%s/%s/%s/%s/%s-%s-%s",
                repo.replaceAll("/$", ""),
                coords.groupId.replace('.', '/'),
                coords.artifactId,
                coords.version,
                coords.artifactId,
                coords.version,
                coords.classifier);

        byte[] toHash = Files.readAllBytes(item);
        Path md5 = Files.createTempFile(item.getFileName().toString(), ".md5");
        Files.write(md5, toMd5(toHash).getBytes(UTF_8));

        Path sha1 = Files.createTempFile(item.getFileName().toString(), ".sha1");
        Files.write(sha1, toSha1(toHash).getBytes(UTF_8));

        List<CompletableFuture<?>> uploads = new ArrayList<>();
        uploads.add(upload(String.format("%s%s", base, append), credentials, item));
        uploads.add(upload(String.format("%s%s.md5", base, append), credentials, md5));
        uploads.add(upload(String.format("%s%s.sha1", base, append), credentials, sha1));

        return CompletableFuture.allOf(uploads.toArray(new CompletableFuture<?>[0]));
    }

    private static String toSha1(byte[] toHash) {
        return toHexS("%040x", "SHA-1", toHash);
    }

    private static String toMd5(byte[] toHash) {
        return toHexS("%032x", "MD5", toHash);
    }

    private static String toHexS(String fmt, String algorithm, byte[] toHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.update(toHash);
            return String.format(fmt, new BigInteger(1, digest.digest()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static CompletableFuture<Void> upload(String targetUrl, Credentials credentials, Path toUpload) {
        Callable<Void> callable;
        if (targetUrl.startsWith("http://") || targetUrl.startsWith("https://")) {
            callable = httpUpload(targetUrl, credentials, toUpload);
        } else {
            callable = writeFile(targetUrl, toUpload);
        }

        CompletableFuture<Void> toReturn = new CompletableFuture<>();
        EXECUTOR.submit(() -> {
            try {
                callable.call();
                toReturn.complete(null);
            } catch (Exception e) {
                toReturn.completeExceptionally(e);
            }
        });
        return toReturn;
    }

    private static Callable<Void> httpUpload(String targetUrl, Credentials credentials, Path toUpload) {
        return () -> {
            LOG.info(String.format("Uploading to %s", targetUrl));
            URL url = new URL(targetUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            if (credentials.getUser() != null) {
                String basicAuth = Base64.getEncoder().encodeToString(
                        String.format("%s:%s", credentials.getUser(), credentials.getPassword()).getBytes(US_ASCII));
                connection.setRequestProperty("Authorization", "Basic " + basicAuth);
            }
            connection.setRequestProperty("Content-Length", "" + Files.size(toUpload));

            try (OutputStream out = connection.getOutputStream()) {
                try (InputStream is = Files.newInputStream(toUpload)) {
                    copy(is, out);
                }

                int code = connection.getResponseCode();

                if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new RuntimeException(connection.getHeaderField("WWW-Authenticate"));
                }

                if (code < 200 || code > 299) {
                    try (InputStream in = connection.getErrorStream()) {
                        String message = new String(toByteArray(in));
                        throw new IOException(String.format("Unable to upload %s (%s) %s", targetUrl, code, message));
                    }
                }
            }
            LOG.info(String.format("Upload to %s complete.", targetUrl));
            return null;
        };
    }

    private static Callable<Void> writeFile(String targetUrl, Path toUpload) {
        return () -> {
            LOG.info(String.format("Copying %s to %s", toUpload, targetUrl));
            Path path = Paths.get(new URL(targetUrl).toURI());
            Files.createDirectories(path.getParent());
            Files.deleteIfExists(path);
            Files.copy(toUpload, path);

            return null;
        };
    }

    private static class Coordinates {
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String classifier;

        public Coordinates(String groupId, String artifactId, String version, String classifier) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.classifier = classifier;
        }
    }

    private static class Credentials {
        private final String user;
        private final String password;

        public Credentials(String user, String password) {
            this.user = user == null || user.isEmpty() ? null : user;
            this.password = password == null || password.isEmpty() ? null : password;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }
    }
}
