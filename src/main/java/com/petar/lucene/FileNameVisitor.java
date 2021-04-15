package com.petar.lucene;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileNameVisitor extends SimpleFileVisitor<Path> {

    private final List<String> visitedFileNames = new ArrayList<>();

    @Override
    public FileVisitResult visitFile(
            final Path file,
            final BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile()) {
            visitedFileNames.add(file.toRealPath().toString());
        }
        return FileVisitResult.CONTINUE;
    }

    public List<String> getVisitedFileNames() {
        return visitedFileNames;
    }
}
