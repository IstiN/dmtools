package com.github.istin.dmtools.job;

import org.json.JSONObject;

import java.net.URLDecoder;

public class UrlEncodedJobTrigger {

    public static void main(String... args) throws Exception {
        if (args != null) {
            for (String arg : args) {
                System.out.println("arg: " + arg);
            }
            String decodedArgs = URLDecoder.decode(args[0]);
            System.out.println(new JSONObject(decodedArgs));
            String base64Encoded = JobRunner.encodeBase64(decodedArgs);
            JobRunner.main(new String[] {base64Encoded});
        } else {
            throw new IllegalStateException("arguments for Job Runner must be JSON urlEncoded.");
        }
    }

}
