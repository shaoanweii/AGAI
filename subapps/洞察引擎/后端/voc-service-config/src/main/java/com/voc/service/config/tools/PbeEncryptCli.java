package com.voc.service.config.tools;

import com.voc.service.config.PBEStringEncryptor;

public class PbeEncryptCli {
    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "encrypt";
        String text = args.length > 1 ? args[1] : "Passw0rd@!";
        if ("decrypt".equalsIgnoreCase(mode)) {
            System.out.println(PBEStringEncryptor.getInstance().decrypt(text));
            return;
        }
        System.out.println(PBEStringEncryptor.getInstance().encrypt(text));
    }
}
