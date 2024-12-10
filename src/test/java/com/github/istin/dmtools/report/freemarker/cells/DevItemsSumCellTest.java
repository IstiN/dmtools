package com.github.istin.dmtools.report.freemarker.cells;

import org.junit.Test;

public class DevItemsSumCellTest {


    @Test
    public void testConstructorWithoutKeys() {
        // Arrange
        String basePath = "test/path";

        // Act
        DevItemsSumCell cell = new DevItemsSumCell(basePath);

        // Assert
        // No exception should be thrown, and the object should be created successfully
    }
}