package nl.esciencecenter.xenon.cli;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Subparser;

import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.credentials.CertificateCredential;
import nl.esciencecenter.xenon.credentials.KeytabCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.credentials.UserCredential;
import nl.esciencecenter.xenon.filesystems.CopyMode;

/**
 * Utilities to construct argument parser
 */
public class ParserHelpers {
    private static final String KEY_VAL = "KEY=VAL";

    private ParserHelpers() {
        throw new IllegalAccessError("Utility class");
    }

    static void addCredentialArguments(ArgumentParser parser, Set<Class> supportedCreds) {
        if (supportedCreds.stream().anyMatch(UserCredential.class::isAssignableFrom)) {
            parser.addArgument("--username").help("Username").setDefault(System.getProperty("user.name"));
        }
        if (supportedCreds.contains(PasswordCredential.class) || supportedCreds.contains(CertificateCredential.class)) {
            parser.addArgument("--password").help("Password or passphrase");
        }
        if (supportedCreds.contains(CertificateCredential.class)) {
            parser.addArgument("--certfile").help("Certificate private key file");
        }
        if (supportedCreds.contains(KeytabCredential.class)) {
            parser.addArgument("--keytabfile").help("Key tab file");
        }
    }

    public static void addTargetCredentialArguments(ArgumentGroup parser, Set<Class> supportedCreds) {
        if (supportedCreds.stream().anyMatch(UserCredential.class::isAssignableFrom)) {
            parser.addArgument("--target-username").help("Username for target location (default: --username value)");
        }
        if (supportedCreds.contains(PasswordCredential.class) || supportedCreds.contains(CertificateCredential.class)) {
            parser.addArgument("--target-password").help("Password or passphrase for target location (default: --password value)");
        }
        if (supportedCreds.contains(CertificateCredential.class)) {
            parser.addArgument("--target-certfile").help("Certificate private key file for target location (default: --certfile value)");
        }
        if (supportedCreds.contains(KeytabCredential.class)) {
            parser.addArgument("--target-keytabfile").help("Key tab file for target location (--default: --keytabfile value)");
        }
    }

    static void addViaCredentialArguments(ArgumentParser parser, Set<Class> supportedCreds) {
        if (supportedCreds.stream().anyMatch(UserCredential.class::isAssignableFrom)) {
            parser.addArgument("--via-username")
                    .action(Arguments.append())
                    .metavar(KEY_VAL)
                    .dest("via_usernames")
                    .help("Username for via host, format <via hostname>=<username> (default: --username value)");
        }
        if (supportedCreds.contains(PasswordCredential.class) || supportedCreds.contains(CertificateCredential.class)) {
            parser.addArgument("--via-password")
                    .action(Arguments.append())
                    .metavar(KEY_VAL)
                    .dest("via_passwords")
                    .help("Password or passphrase for via host, format <via hostname>=<password> (default: --password value)");
        }
        if (supportedCreds.contains(CertificateCredential.class)) {
            parser.addArgument("--via-certfile")
                    .action(Arguments.append())
                    .metavar(KEY_VAL)
                    .dest("via_certfiles")
                    .help("Certificate private key file for via host, format <via hostname>=<certfile> (default: --certfile value)");
        }
        if (supportedCreds.contains(KeytabCredential.class)) {
            parser.addArgument("--via-keytabfile")
                    .action(Arguments.append())
                    .metavar(KEY_VAL)
                    .dest("via_keytabfiles")
                    .help("Key tab file for via host, format <via hostname>=<keytabfilefile> (default: --keytabfile value)");
        }
    }

    public static MutuallyExclusiveGroup addCopyModeArguments(ArgumentParser parser) {
        MutuallyExclusiveGroup group = parser.addMutuallyExclusiveGroup("optional copy mode arguments");
        group.addArgument("--replace")
            .help("If a file already exists at the target location, replace that file with the file from the source location")
            .type(CopyMode.class)
            .action(Arguments.storeConst())
            .dest("copymode")
            .setConst(CopyMode.REPLACE)
            .setDefault(CopyMode.CREATE);
        group.addArgument("--ignore")
            .help("If a file already exists at the target location, skip copying of that file")
            .type(CopyMode.class)
            .action(Arguments.storeConst())
            .dest("copymode")
            .setConst(CopyMode.IGNORE)
            .setDefault(CopyMode.CREATE);
        return group;
    }

    public static String getSupportedLocationHelp(String[] supportedLocations) {
        List<String> helps = Arrays.stream(supportedLocations).map(location -> "- " + location).collect(Collectors.toList());
        helps.add(0, "supported locations:");
        String sep = System.getProperty("line.separator");
        return String.join(sep, helps);
    }

    private static String getAdaptorPropertyHelp(XenonPropertyDescription property) {
        return "- " + property.getName() + "=" + property.getDefaultValue() + " ("+ property.getDescription() + ", type:" + property.getType() + ") ";
    }

    public static void addRunArguments(Subparser subparser) {
        subparser.addArgument("executable").help("Executable to schedule for execution").required(true);
        subparser.addArgument("args")
            .help("Arguments for executable, prepend ' -- ' when arguments start with '-'")
            .nargs("*");

        subparser.addArgument("--queue").help("Schedule job in this queue");
        subparser.addArgument("--env")
            .help("Environment variable of the executable")
            .metavar(KEY_VAL)
            .action(Arguments.append())
            .dest("envs");
        subparser.addArgument("--inherit-env")
            .help("Use all local environment variables for the executable")
            .action(Arguments.storeTrue())
        ;
        subparser.addArgument("--max-run-time").help("Maximum job run time (in minutes)").type(Integer.class);
        subparser.addArgument("--tasks").type(Integer.class).help("Number of tasks in this job").setDefault(1);
        subparser.addArgument("--cores-per-task").type(Integer.class).help("Number of cores needed for each task").setDefault(1);
        subparser.addArgument("--tasks-per-node").type(Integer.class).help("Number of tasks allowed per node");
        subparser.addArgument("--start-per-task").action(Arguments.storeTrue()).help("Executable must be started for each task instead of once per job");
        subparser.addArgument("--working-directory")
            .help("Path at location where executable should be executed. If location is local system, default value is the current working directory. If location is remote, default value is remote system's entry path");
        subparser.addArgument("--max-memory").help("Maximum amount of memory needed for process (in MBytes)").type(Integer.class);
        subparser.addArgument("--scheduler-argument").dest("scheduler_arguments").action(Arguments.append()).help("Scheduler specific arguments for this job");
        subparser.addArgument("--temp-space").type(Integer.class).help("Amount of temp space needed per node/process (in MBytes)");
    }

    public static String getSupportedPropertiesHelp(XenonPropertyDescription[] descriptions) {
        String sep = System.getProperty("line.separator");
        List<String> helps = Arrays.stream(descriptions).map(ParserHelpers::getAdaptorPropertyHelp).collect(Collectors.toList());
        helps.add(0, "supported properties:");
        return String.join(sep, helps);
    }
}
