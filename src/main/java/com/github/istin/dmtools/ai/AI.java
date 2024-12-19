package com.github.istin.dmtools.ai;

import java.io.File;
import java.util.List;

public interface AI {

    String chat(String message) throws Exception;

    String chat(String model, String message, File imageFile) throws Exception;

    String chat(String model, String message, List<File> files) throws Exception;

}
