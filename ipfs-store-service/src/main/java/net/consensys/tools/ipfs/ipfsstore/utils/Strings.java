package net.consensys.tools.ipfs.ipfsstore.utils;

public interface Strings {

    static boolean isEmpty(CharSequence str) {
        return !hasLength(str);
    }

    static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }
}
