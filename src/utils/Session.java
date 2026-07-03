package utils;

import models.User;

/**
 * Holds the currently logged-in user for the lifetime of the application run.
 * Set once on successful login, read by any screen that needs to know who is
 * logged in or what role they have, and cleared on logout.
 */
public class Session {

    private static User currentUser;
    private static String searchKeyword;
    private static String wardFilter;
    private static java.time.LocalDateTime loginTime;

    private Session() {
        // utility class - no instances
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
        loginTime = java.time.LocalDateTime.now();
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
        searchKeyword = null;
        wardFilter = null;
        loginTime = null;
    }

    public static void setWardFilter(String ward) {
        wardFilter = ward;
    }

    public static String consumeWardFilter() {
        String ward = wardFilter;
        wardFilter = null;
        return ward;
    }

    public static java.time.LocalDateTime getLoginTime() {
        return loginTime;
    }

    public static void setSearchKeyword(String keyword) {
        searchKeyword = keyword;
    }

    public static String consumeSearchKeyword() {
        String keyword = searchKeyword;
        searchKeyword = null;
        return keyword;
    }
}