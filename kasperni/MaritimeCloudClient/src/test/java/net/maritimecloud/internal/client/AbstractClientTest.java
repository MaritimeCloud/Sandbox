/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.maritimecloud.internal.client;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 *
 * @author Kasper Nielsen
 */
public class AbstractClientTest {
    /** The output of these tests will be kept in the temporary folder */
    Set<Description> keepThisTest = new CopyOnWriteArraySet<>();

    @Rule
    public DeleteOnTestSuccess rule = new DeleteOnTestSuccess();

    protected Path testroot;

    // perhaps we should move failed tests into user.tmp/cake/failedtests/TestName

    public String getTestName() {
        return rule.description.getMethodName();
    }

    /** Call this method to keep the tests contents. */
    protected void keepTestContents() {
        keepThisTest.add(rule.description);
    }

    Path fail;

    Path broadcastOut;

    Path broadcastIn;

    Path endpointOut;

    Path endpointIn;

    @Before
    public final void setupDirectory() throws Exception {
        testroot = Files.createTempDirectory("mc-client-" + getTestName() + "-test");
        testroot = Paths.get("/Users/kasperni/tmp/dd");
        // testroot = rootPath.resolve(getTestName());
        Files.createDirectories(testroot);
        Files.createDirectories(testroot.resolve("fileio"));
        Files.createDirectories(fail = testroot.resolve("fileio/fail"));
        Files.createDirectories(broadcastIn = testroot.resolve("fileio/in/broadcast"));
        Files.createDirectories(endpointIn = testroot.resolve("fileio/in/service"));
        Files.createDirectories(broadcastOut = testroot.resolve("fileio/out/broadcast"));
        Files.createDirectories(endpointOut = testroot.resolve("fileio/out/service"));
    }

    class DeleteOnTestSuccess extends TestWatcher {

        Description description;

        @Override
        public void starting(Description description) {
            this.description = description;
        }

        /** Delete test directory on successful test. */
        @Override
        public void succeeded(Description description) {
            if (false && !keepThisTest.contains(description)) {
                Path root = testroot; // .resolve(description.getMethodName());
                try {
                    deleteDirectoryRecursively(root);
                } catch (IOException e) {
                    String file = e instanceof FileSystemException ? ((FileSystemException) e).getFile() : "unknown";
                    throw new Error("Could not cleanup directory, maybe the file " + file + " is open in an editor", e);
                }
            }
        }


    }

    /**
     * Delete the directory contents recursively, including the specified path
     */
    static void deleteDirectoryRecursively(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
