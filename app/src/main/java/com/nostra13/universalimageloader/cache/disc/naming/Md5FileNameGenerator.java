package com.nostra13.universalimageloader.cache.disc.naming;

import com.nostra13.universalimageloader.utils.C0197L;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5FileNameGenerator implements FileNameGenerator {
    public String generate(String imageUri) {
        return new BigInteger(getMD5(imageUri.getBytes())).abs().toString(36);
    }

    private byte[] getMD5(byte[] data) {
        byte[] hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(data);
            hash = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            C0197L.m2e(e);
        }
        return hash;
    }
}
