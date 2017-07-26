package nl.esciencecenter.xenon.cli;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import nl.esciencecenter.xenon.cli.listfiles.ListFilesOutput;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class FileTest {
    @Rule
    public TemporaryFolder myfolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void copy_file() throws IOException {
        File sourceFile = myfolder.newFile("source.txt");
        sourceFile.createNewFile();
        File targetFile = new File(myfolder.getRoot(), "target.txt");

        String[] args = {"file", "copy", sourceFile.getAbsolutePath(), targetFile.getAbsolutePath()};
        Main main = new Main();
        main.run(args);

        assertTrue(targetFile.isFile());
    }

    @Test
    public void copy_recursiveFile() throws IOException {
        File sourceFile = myfolder.newFile("source.txt");
        sourceFile.createNewFile();
        File targetFile = new File(myfolder.getRoot(), "target.txt");

        String[] args = {"file", "copy", "--recursive", sourceFile.getAbsolutePath(), targetFile.getAbsolutePath()};
        Main main = new Main();
        main.run(args);

        assertTrue(targetFile.isFile());
    }

    @Test
    public void copy_dir() throws IOException {
        File sourceDir = myfolder.newFolder("source");
        File targetDir = new File(myfolder.getRoot(), "target");

        String[] args = {"file", "copy", "--recursive", sourceDir.getAbsolutePath(), targetDir.getAbsolutePath()};
        Main main = new Main();
        main.run(args);

        assertTrue(targetDir.isDirectory());
    }

    @Test
    public void copy_recursiveDir() throws IOException {
        File sourceDir = myfolder.newFolder("source");
        new File(sourceDir, "file1").createNewFile();
        File sourceDirDir = myfolder.newFolder("source", "dep1");
        new File(sourceDirDir, "file2").createNewFile();
        File targetDir = new File(myfolder.getRoot(), "target");

        String[] args = {"file", "copy", "--recursive", sourceDir.getAbsolutePath(), targetDir.getAbsolutePath()};
        Main main = new Main();
        main.run(args);

        assertTrue(targetDir.isDirectory());
        assertTrue(new File(targetDir, "file1").isFile());
        File targetDirDir = new File(targetDir, "dep1");
        assertTrue(targetDirDir.isDirectory());
        assertTrue(new File(targetDirDir, "file2").isFile());
    }

    @Test
    public void copy_targetExists_throwsExecption() throws IOException {
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            String expected = "file adaptor: Destination path already exists";
            assertTrue(expected, systemErrRule.getLog().contains(expected));
        });

        File sourceFile = myfolder.newFile("source.txt");
        sourceFile.createNewFile();
        File targetFile = myfolder.newFile("target.txt");
        targetFile.createNewFile();

        String[] args = {"file", "copy", sourceFile.getAbsolutePath(), targetFile.getAbsolutePath()};
        Main main = new Main();
        main.run(args);
    }

    @Test
    public void copy_fromStdin() throws IOException {
        Path targetFile = Paths.get(myfolder.getRoot().getAbsolutePath(), "target.txt");
        InputStream oldIn = System.in;
        String sourceContent = "my content";
        InputStream sourceIn = new ByteArrayInputStream(sourceContent.getBytes());
        System.setIn(sourceIn);

        try {
            String[] args = {"file", "copy", "-", targetFile.toString()};
            Main main = new Main();
            main.run(args);

            String targetContent = String.join("", Files.lines(targetFile).collect(Collectors.toList()));
            assertEquals(sourceContent, targetContent);
        } finally {
            System.setIn(oldIn);
        }
    }

    @Test
    public void copy_RecursiveStdin_throwsException() {
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            String expected = "file adaptor: Unable to do recursive copy from stdin";
            assertTrue(expected, systemErrRule.getLog().contains(expected));
        });
        String[] args = {"file", "copy", "--recursive", "-", myfolder.getRoot().getAbsolutePath()};
        Main main = new Main();
        main.run(args);
    }

    @Test
    public void copy_toStdout() throws IOException {
        File sourceFile = myfolder.newFile("source.txt");
        String message = "Hello World!\n";
        Files.write(sourceFile.toPath(), message.getBytes());

        String[] args = {"file", "copy", sourceFile.getAbsolutePath(), "-"};
        Main main = new Main();
        main.run(args);

        assertEquals(message, systemOutRule.getLogWithNormalizedLineSeparator());
    }

    @Test
    public void copyFile_RecursiveStdout_throwsException() {
        exit.checkAssertionAfterwards(() -> {
            String expected = "file adaptor: Unable to do recursive copy to stdout";
            assertTrue(expected, systemErrRule.getLog().contains(expected));
        });
        exit.expectSystemExitWithStatus(1);
        String[] args = {"file", "copy", "--recursive", myfolder.getRoot().getAbsolutePath(), "-"};
        Main main = new Main();
        main.run(args);
    }

    @Test
    public void list_aDirectoryWithHiddenFiles_onlyListNonHiddenObjects() throws IOException {
        myfolder.newFile("file1").createNewFile();
        myfolder.newFile(".hidden1").createNewFile();
        File dir1 = myfolder.newFolder("dir1");
        dir1.mkdirs();
        new File(dir1, "file3").createNewFile();
        File hdir2 = myfolder.newFolder(".hidden2");
        hdir2.mkdirs();

        String path = myfolder.getRoot().getCanonicalPath();
        String[] args = {"file", "list", path};
        Main main = new Main();
        ListFilesOutput output = (ListFilesOutput) main.run(args);

        Set<String> result = new HashSet<>(output.getObjects());
        Set<String> expected = new HashSet<>(Arrays.asList("dir1", "file1"));
        assertEquals(expected, result);
    }

    @Test
    public void list_aFile_exit1() throws IOException {
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            String expected = "file adaptor: Failed to list directory";
            String log = systemErrRule.getLog();
            assertTrue(expected, log.contains(expected));
        });

        File file1 = myfolder.newFile("file1");
        file1.createNewFile();

        String path = file1.getAbsolutePath();
        String[] args = {"file", "list", path};
        Main main = new Main();
        main.run(args);

    }

    @Test
    public void removeFile_touchedFile_fileShouldNotExist() throws IOException {
        File file1 = myfolder.newFile("file1");
        file1.createNewFile();

        String[] args = {"file", "remove", file1.getAbsolutePath()};
        Main main = new Main();
        main.run(args);

        assertFalse(file1.exists());
    }

    @Test
    public void list_nonExistingPath_exit1() throws IOException {
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            String expected = "Failed to list";
            assertTrue(expected, systemErrRule.getLog().contains(expected));
        });
        File file1 = myfolder.newFile("idontexist");

        String[] args = {"file", "list", file1.getAbsolutePath()};
        Main main = new Main();
        main.run(args);
    }

    @Test
    public void list_nonExistingPathWithStacktrace_exit1() throws IOException {
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            String expected = "Caused by:";
            assertTrue(expected, systemErrRule.getLog().contains(expected));
        });
        File file1 = myfolder.newFile("idontexist");

        String[] args = {"--stacktrace", "file", "list", file1.getAbsolutePath()};
        Main main = new Main();
        main.run(args);
    }

}
