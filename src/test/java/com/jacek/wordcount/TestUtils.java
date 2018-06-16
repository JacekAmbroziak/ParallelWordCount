package com.jacek.wordcount;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.google.common.io.Resources.getResource;
import static com.jacek.wordcount.Utils.filesInDirWithExtension;

final class TestUtils {
    static File resourceFile(final String fileName) throws URISyntaxException {
        return new File(getResource(fileName).toURI());
    }

    static void zipTextFilesForTesting(final String dirName, final String extension, final String zipFileName) throws IOException {
        final List<File> fileList = filesInDirWithExtension(dirName, extension);
        zipFiles(fileList, zipFileName);
    }

    /**
     * Creates a FLATTENED zip archive of a list of files
     * (file separator chars are replaced with _)
     *
     * @param files       list of files to archive, without duplicates
     * @param zipFileName
     * @throws IOException
     */
    static void zipFiles(final List<File> files, final String zipFileName) throws IOException {
        final FileOutputStream fos = new FileOutputStream(zipFileName);
        final ZipOutputStream zipOut = new ZipOutputStream(fos);
        final byte[] bytes = new byte[4096];
        for (final File file : files) {
            final String canonicalPath = file.getCanonicalPath();
            final String flattenedName = canonicalPath.replace(File.separatorChar, '_');
            zipOut.putNextEntry(new ZipEntry(flattenedName));
            {
                final FileInputStream fis = new FileInputStream(file);
                int nBytesRead;
                while ((nBytesRead = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, nBytesRead);
                }
                fis.close();
            }
        }
        zipOut.close();
        fos.close();
    }

    static List<File> unzipToDir(final File zipFile, final File targetDir) throws IOException {
        final ArrayList<File> fileList = new ArrayList<>();
        final ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        final byte[] buffer = new byte[4096];
        for (ZipEntry zipEntry = zis.getNextEntry(); zipEntry != null; zipEntry = zis.getNextEntry()) {
            final String fileName = zipEntry.getName();
            final File newFile = new File(targetDir, fileName);
            {
                final FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) >= 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            fileList.add(newFile);
        }
        zis.closeEntry();
        zis.close();
        return fileList;
    }

    static List<String> unzipToLines(final File zipFile) throws IOException {
        final ArrayList<String> lines = new ArrayList<>();
        final ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        for (ZipEntry zipEntry = zis.getNextEntry(); zipEntry != null; zipEntry = zis.getNextEntry()) {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zis));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        }
        zis.closeEntry();
        zis.close();
        return lines;
    }
}
