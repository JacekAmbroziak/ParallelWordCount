package com.jacek.wordcount;

import java.io.IOException;

final class PrepareTestData {
    public static void main(String[] args) {
        try {
            TestUtils.zipTextFilesForTesting("/usr/src/linux-4.9.95-gentoo/", ".txt", "/tmp/linux-4.9.95-docs.zip");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
