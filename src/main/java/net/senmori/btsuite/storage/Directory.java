package net.senmori.btsuite.storage;

import lombok.Getter;

import java.io.File;

/**
 * A {@link Directory} can be either a file or actual directory.
 * The type is determined by the {@link #getPath()} attribute.
 */
public class Directory {

    /**
     * Get the parent of this directory
     */
    @Getter
    private final String parent;

    /**
     * Get the actual name of this directory, or file.
     */
    @Getter
    private final String path;

    public Directory(String parent, String path) {
        this.parent = parent;
        this.path = path;
    }

    public Directory(Directory parent, String path) {
        this.parent = parent.getFile().getAbsolutePath();
        this.path = path;
    }

    /**
     * Get this {@link Directory} as a {@link File}
     *
     * @return this directory as a {@link File}
     */
    public File getFile() {
        return new File( parent, path );
    }
}
