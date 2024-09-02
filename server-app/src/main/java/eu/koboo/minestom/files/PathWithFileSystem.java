package eu.koboo.minestom.files;

import java.nio.file.FileSystem;
import java.nio.file.Path;

public class PathWithFileSystem {
    private final Path path;
    private final FileSystem fileSystem;

    public PathWithFileSystem(Path path, FileSystem fileSystem) {
        this.path = path;
        this.fileSystem = fileSystem;
    }

    public Path getPath() {
        return path;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }
}