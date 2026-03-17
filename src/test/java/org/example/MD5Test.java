package org.example;

import org.springframework.util.DigestUtils;

public class MD5Test {
    public static void main(String[] args) {
        String password = "D5a!rX&8upsQ*x";
        String md5Hash = DigestUtils.md5DigestAsHex(password.getBytes());
        System.out.println("Password: " + password);
        System.out.println("MD5 Hash: " + md5Hash);
    }
}
