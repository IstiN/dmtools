package com.github.istin.dmtools.ai;

import junit.framework.TestCase;

import java.io.IOException;

public class ChunkPreparationTest extends TestCase {

    private ChunkPreparation chunkPreparation;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        chunkPreparation = new ChunkPreparation();
    }

    public void testPrepareChunks() throws IOException {
        int tokens = new Claude35TokenCounter().countTokens("test");
        System.out.println(tokens);
    }

    public void testTestPrepareChunks() {

    }
}