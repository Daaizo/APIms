package api.ms.rest_api;

import org.jasypt.util.password.StrongPasswordEncryptor;

public abstract class Password {
  public static String encryptPassword(String inputPassword) {
    StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
    return encryptor.encryptPassword(inputPassword);
  }

  public static boolean verifyPassword(String inputPassword, String encryptedStoredPassword) {
    StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
    return encryptor.checkPassword(inputPassword, encryptedStoredPassword);
  }
}
