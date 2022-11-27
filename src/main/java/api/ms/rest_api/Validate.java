package api.ms.rest_api;

import java.util.regex.Pattern;

public abstract class Validate {

  public static boolean isPasswordStrong(String password) {
    String passwordRegex = "^(?=.*[A-Z])(?=.*[!@#$&%^*()_+])(?=.*[0-9])(?=.*[a-z]).{6,20}$";
    //Regex meaning/  at least: 1 uppercase letter,  one special sign ( basically all numbers + shift ), 1 number,1 lowercase letter, 6-20 characters
    //Rubular link to check it: https://rubular.com/r/gEmHAEm9wKr1Tj    <- regex checker
    return Pattern.matches(passwordRegex, password);
  }

  public static boolean isLoginValid(String login) {
    String loginRegex = "^[^ ]{2,20}$";
    // all characters except space, 2-20 characters
    return Pattern.matches(loginRegex, login);
  }


}
