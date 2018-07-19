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

import lombok.AccessLevel;
import lombok.Getter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.regex.Pattern;

/**
 * A {@link Directory} can be either a file or actual directory.
 * The type is determined by the {@link #getPath()} attribute.
 */
@Getter
public class Directory {
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile( ".*[/\\\\].*" );

    /**
     * Get the parent of this directory
     */
    private final String parent;

    /**
     * Get the actual name of this directory, or file.
     */
    private final String path;

    @Getter(AccessLevel.NONE)
    private WeakReference<File> file;

    /**
     * Create a new Directory instance with the given path.
     *
     * @param parent the parent folder of the directory
     * @param path   Either the file, or subfolder of the parent
     */
    public Directory(String parent, String path) {
        this.parent = parent;
        this.path = path;
        file = new WeakReference<>( new File( this.parent, this.path ) );
    }

    public Directory(File parent, String child) {
        this( parent.getPath(), child );
    }

    public Directory(Directory parent, String path) {
        this( parent.getFile().getPath(), path );
    }

    /**
     * Get this Directory as a {@link File}
     *
     * @return this directory as a {@link File}
     */
    public File getFile() {
        if ( file.get() != null ) {
            return file.get();
        }
        file = new WeakReference<>( new File( parent, path ) );
        return file.get();
    }

    public void clearReference() {
        file.clear();
    }

    public boolean isFile() {
        return getFile().isFile();
    }

    public boolean isDirectory() {
        return getFile().isDirectory();
    }

    public boolean exists() {
        return getFile().exists();
    }

    public boolean isRepository() {
        return getFile().exists(); // TODO: Implement a way to tell if it's actually a repo
    }

    public String toString() {
        return parent + File.separator + path;
    }
}
