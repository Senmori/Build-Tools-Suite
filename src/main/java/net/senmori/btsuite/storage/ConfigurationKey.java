/*
 * Copyright (c) 2018, Senmori. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

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

        public static final String DEFAULT_GIT_INSTALLER_LINK = "https://static.spigotmc.org/git/PortableGit-{0}-{1}-bit.7z.exe";

        //public static final String DEFAULT_S3_DOWNLOAD_LINK = "https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/minecraft_server.%1$s.jar";
    }
}
