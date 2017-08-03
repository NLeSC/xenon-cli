package nl.esciencecenter.xenon.cli;

import static nl.esciencecenter.xenon.cli.Main.buildXenonProperties;
import static nl.esciencecenter.xenon.cli.ParserHelpers.getAllowedFileSystemPropertyKeys;
import static nl.esciencecenter.xenon.cli.ParserHelpers.getAllowedSchedulerPropertyKeys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.CertificateCredential;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.Scheduler;

import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Helpers for Xenon.jobs based commands
 */
public class Utils {
    /** The default buffer size to use for copy operations */
    private static final int DEFAULT_BUFFER_SIZE = 16 * 1024;

    Utils() {
        throw new IllegalAccessError("Utility class");
    }

    static Map<String, String> parseArgumentListAsMap(List<String> input) {
        Map<String, String> output = new HashMap<>();
        if (input != null) {
            for (String prop : input) {
                String[] keyval = prop.split("=", 2);
                output.put(keyval[0], keyval[1]);
            }
        }
        return output;
    }

    public static JobDescription getJobDescription(Namespace res) {
        JobDescription description = new JobDescription();

        String executable = res.get("executable");
        description.setExecutable(executable);

        List<String> args = res.getList("args");
        if (args != null && !args.isEmpty()) {
            description.setArguments(args.toArray(new String[0]));
        }

        String queue = res.getString("queue");
        if (queue != null) {
            description.setQueueName(queue);
        }

        Map<String, String> envs = parseArgumentListAsMap(res.getList("envs"));
        if (!envs.isEmpty()) {
            description.setEnvironment(envs);
        }

        Map<String, String> options = parseArgumentListAsMap(res.getList("options"));
        if (!options.isEmpty()) {
            description.setJobOptions(options);
        }

        int maxTime = res.getInt("max_time");
        description.setMaxTime(maxTime);

        int nodeCount = res.getInt("node_count");
        description.setNodeCount(nodeCount);

        int procsPerNode = res.getInt("procs_per_node");
        description.setProcessesPerNode(procsPerNode);

        String workingDirectory = res.getString("working_directory");
        if (workingDirectory != null) {
            description.setWorkingDirectory(workingDirectory);
        }

        String stdin = res.getString("stdin");
        if (stdin != null) {
            description.setStdin(stdin);
        }

        String stdout = res.getString("stdout");
        if (stdout != null) {
            description.setStdout(stdout);
        }

        String stderr = res.getString("stderr");
        if (stderr != null) {
            description.setStderr(stderr);
        }
        return description;
    }

    public static long pipe(InputStream in, OutputStream out) throws IOException {
        long bytes = 0;

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int len = in.read(buffer);
        while (len != -1) {
            bytes += len;
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
        return bytes;
    }

    public static Scheduler createScheduler(Namespace res) throws XenonException {
        String adaptor = res.getString("adaptor");
        String location = res.getString("location");
        Credential credential = createCredential(res);

        Set<String> allowedKeys = getAllowedSchedulerPropertyKeys(adaptor);
        Map<String, String> props = buildXenonProperties(res, allowedKeys);
        return Scheduler.create(adaptor, location, credential, props);
    }

    public static FileSystem createFileSystem(Namespace res) throws XenonException {
        String adaptor = res.getString("adaptor");
        String location = res.getString("location");
        Credential credential = createCredential(res);

        Set<String> allowedKeys = getAllowedFileSystemPropertyKeys(adaptor);
        Map<String, String> props = buildXenonProperties(res, allowedKeys);
        return FileSystem.create(adaptor, location, credential, props);
    }

    static Credential createCredential(Namespace res) {
        return createCredential(res, "");
    }

    static Credential createCredential(Namespace res, String prefix) {
        String username = res.getString(prefix + "username");
        String passwordAsString = res.getString(prefix + "password");
        String certfile = res.getString(prefix + "certfile");
        char[] password = null;
        if (passwordAsString != null) {
            password = passwordAsString.toCharArray();
        }
        if (certfile != null) {
            return new CertificateCredential(username, certfile, password);
        } else if (password != null) {
            return new PasswordCredential(username, password);
        } else if (username != null) {
            return new DefaultCredential(username);
        } else {
            return new DefaultCredential();
        }
    }
}
