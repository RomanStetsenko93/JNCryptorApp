package com.rst;

import org.cryptonode.jncryptor.AES256JNCryptorInputStream;
import org.cryptonode.jncryptor.AES256JNCryptorOutputStream;
import org.cryptonode.jncryptor.CryptorException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static final String VERSION_NAME = "1.0.0";

    private static final String ENCRYPT = "-e";
    private static final String DECRYPT = "-d";
    private static final String VERSION = "-v";

    private static final String HELP_DESCRIPTION = "Help: \n" + VERSION + " to get app version;\nUsage:" +
            "\nFirst parameter: " + ENCRYPT + " (encrypt) or " + DECRYPT +
            " (decrypt);\nSecond parameter: your password;\nThird parameter: path to file." +
            "\n\nExample: \n>java -jar JNCryptorApp.jar -e MyPassword path/to/file/example.txt" +
            "\n\nResulted file will be created at the same directory as the source file";

    private static String userPassword;

    public static void main(String[] args) {
        try {
            if (args.length == 1 && args[0].equals(VERSION)) {
                System.out.println("Current version: " + VERSION_NAME);
            } else if (args.length == 3) {
                File file = getFileFromPath(args[2]);
                userPassword = args[1];
                if (userPassword == null || userPassword.length() < 1) {
                    System.out.println("Password is empty!");
                    return;
                }
                if (file != null) {
                    if (args[0].equals(ENCRYPT)) {
                        encryptFile(file);
                        System.out.println("Success, file encrypted!");
                    } else if (args[0].equals(DECRYPT)) {
                        decryptFile(file);
                        System.out.println("Success, file decrypted!");
                    }
                }
            } else {
                System.out.println(HELP_DESCRIPTION);
            }
        } catch (Exception e) {
            printException(e);
        }
    }

    private static void encryptFile(File file) throws IOException, CryptorException {
        File encryptedFile = createEncryptedFile(file);
        byte[] bytes = getBytesFromFile(file);

        AES256JNCryptorOutputStream jnCryptorOutputStream = null;
        try {
            jnCryptorOutputStream = new AES256JNCryptorOutputStream(
                    new FileOutputStream(encryptedFile), userPassword.toCharArray());
            jnCryptorOutputStream.write(bytes, 0, bytes.length);
        } finally {
            safeClose(jnCryptorOutputStream);
        }
    }

    private static void decryptFile(File encryptedFile) throws IOException {
        File decryptedFile = createDecryptedFile(encryptedFile);

        AES256JNCryptorInputStream jnCryptorInputStream  = null;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[8192];
        try {
            jnCryptorInputStream = new AES256JNCryptorInputStream(
                    new FileInputStream(encryptedFile), userPassword.toCharArray());
            while ((nRead = jnCryptorInputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            bytesToFile(buffer.toByteArray(), decryptedFile);
        } finally {
            safeClose(jnCryptorInputStream);
        }
    }

    private static File getFileFromPath(String pathToFile) {
        File file = new File(pathToFile);
        if (file.exists()) {
            if (file.isDirectory()) {
                System.out.println("This is a directory, provide path to file");
            } else {
                return file;
            }
        } else {
            System.out.println("File does not exist");
        }
        return null;
    }

    private static byte[] getBytesFromFile(File file) throws IOException {
        Path path = Paths.get(file.getPath());
        return Files.readAllBytes(path);
    }

    private static void bytesToFile(byte[] bytes, File file) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bytes);
        } finally {
            safeClose(fos);
        }
    }

    private static File createEncryptedFile(File sourceFile) {
        String name = sourceFile.getName();
        String dir = sourceFile.getParent();
        File encryptedFile = new File(dir, name + "ENC");
        encryptedFile.delete();
        return encryptedFile;
    }

    private static File createDecryptedFile(File file) {
        String name = file.getName();
        name = name.replaceAll("ENC$", "");
        String dir = file.getParent();
        File decryptedFile = new File(dir, name);
        decryptedFile.delete();
        return decryptedFile;
    }

    private static void safeClose(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
                printException(e);
            }
        }
    }

    private static void printException(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        System.out.println(sw.toString());
    }
}