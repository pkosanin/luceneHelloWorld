package com.petar.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    private static final int EX_USAGE = 64;
    private static final int EX_IOERR = 74;
    private static final int EX_DATAERR = 65;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: path_to_root_directory");
            System.exit(EX_USAGE);
        } else {
            List<String> fileNames = readFileNames(args[0]);
            createIndexes(fileNames);
            searchFileNames();
        }
    }

    private static void searchFileNames() {
        try {
            runPrompt();
        } catch (IOException e) {
            System.err.println("failed to read query");
            e.printStackTrace();
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        FSDirectory indexDirectory = FSDirectory.open(Paths.get("index"));
        DirectoryReader directoryReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);


        System.out.println("type in query");
        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            searchFileName(indexSearcher, line);
        }
    }

    private static void searchFileName(final IndexSearcher indexSearcher,
                                       final String userQuery) {
        StringBuilder queryBuilder = new StringBuilder();
        userQuery.chars().forEach(c -> queryBuilder.append("*").append((char) c));
        queryBuilder.append("*");

        String wildcardQuery = queryBuilder.toString();
        WildcardQuery query = new WildcardQuery(new Term("filepath",
                wildcardQuery));
        try {
            ScoreDoc[] docIds = indexSearcher.search(query, 10).scoreDocs;
            for (ScoreDoc docId  : docIds) {
                Document document = indexSearcher.doc(docId.doc);
                System.out.println(document.getField("filepath").stringValue());
            }
        } catch (IOException e) {
            System.err.println("failed to search for query");
            e.printStackTrace();
            System.exit(EX_DATAERR);
        }
    }

    private static void createIndexes(final List<String> fileNames) {
        if (Files.exists(Paths.get("index"))) {
            return;
        }

        IndexWriter indexWriter = null;
        try {
            Directory directory = FSDirectory.open(
                    Files.createDirectory(Paths.get("index")));
            indexWriter = new IndexWriter(directory, new IndexWriterConfig());
            for (String fileName : fileNames) {
                Document document = new Document();
                document.add(new StringField(
                        "filepath",
                        fileName,
                        Field.Store.YES));
                indexWriter.addDocument(document);
            }
            System.out.println("successfully indexed filenames");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(EX_IOERR);
        } finally {
            if (indexWriter != null) {
                try {
                    indexWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.err.println("failed to close index writer");
                    System.exit(EX_IOERR);
                }
            }
        }
    }

    private static List<String> readFileNames(final String rootDirectory) {
        FileNameVisitor fileNameVisitor = new FileNameVisitor();
        try {
            Files.walkFileTree(Paths.get(rootDirectory), fileNameVisitor);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(EX_IOERR);
        }
        return fileNameVisitor.getVisitedFileNames();
    }
}
