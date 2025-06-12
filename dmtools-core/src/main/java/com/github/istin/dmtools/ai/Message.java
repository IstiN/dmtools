package com.github.istin.dmtools.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Message {
    private String role;
    private String text;
    private List<File> files;
}
