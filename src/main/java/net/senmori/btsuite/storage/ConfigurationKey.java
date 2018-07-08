package net.senmori.btsuite.storage;

public interface ConfigurationKey {

    final class Name {

        /* *********************************
         *          URLS
         * *********************************
         */

        public static final String SPIGOT_VERSION_LINK = "spigot_versions_link";

        public static final String GIT_INSTALLER_LINK = "git_installer_link";

        public static final String MAVEN_INSTALLER_LINK = "maven_installer_link";

        public static final String STASH_REPO_LINK = "stash_repo_link";

        public static final String S3_DOWNLOAD_LINK = "s3_download_link";

        public static final String MC_JAR_DOWNLOAD_LINK = "mc_jar_download_link";

        /* *********************************
         *          VERSIONS
         * *********************************
         */

        public static final String PORTABLE_GIT_VERSION = "portable_git_version";

        public static final String DEFAULT_SPIGOT_VERSION = "default_spigot_version";

        public static final String MAVEN_VERSION = "maven_version";

        /* *********************************
         *          DIRECTORIES
         * *********************************
         */
        public static final String WORK_DIR = "work_dir";

        public static final String TEMP_DIR = "tmp_dir";

        public static final String VERSIONS_DIR = "versions_dir";

        public static final String JAR_DIR = "jar_dir";

        public static final String MAVEN_DIR = "maven_dir";

        public static final String PORTABLE_GIT_DIR = "portable_git_dir";

        /* *********************************
         *       INTERNAL USE ONLY
         * *********************************
         */

        @Deprecated
        public static final String DIR_ID = "id";

        @Deprecated
        public static final String DIR_PARENT = "parent";

        @Deprecated
        public static final String DIR_PATH = "path";

    }

    final class Value {
        /* *********************************
         *    DEFAULT UN-FORMATTED LINKS
         * *********************************
         */
        public static final String DEFAULT_MAVEN_INSTALLER_LINK = "https://static.spigotmc.org/maven/apache-maven-{0}-bin.zip";

        public static final String DEFAULT_S3_DOWNLOAD_LINK = "https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/minecraft_server.%1$s.jar";
    }
}
