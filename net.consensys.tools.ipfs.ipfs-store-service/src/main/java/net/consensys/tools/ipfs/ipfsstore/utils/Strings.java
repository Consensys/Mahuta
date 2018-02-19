package net.consensys.tools.ipfs.ipfsstore.utils;

public abstract class Strings {

    public static boolean isEmpty(CharSequence str) {
        return !hasLength(str);
    }
    
    public static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }
}
