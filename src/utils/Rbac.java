package utils;

import models.User;

/**
 * Role-based access control for MedCore HMS.
 * ADMIN has full access; DOCTOR sees assigned patients and can order labs/prescriptions;
 * NURSE sees ward patients and can dispense meds / update lab orders.
 */
public final class Rbac {

    public enum Module {
        DASHBOARD, PATIENTS, DOCTORS, NURSES, BILLING, PHARMACY, LABORATORY,
        APPOINTMENTS, DEPARTMENTS, WARD, REPORTS, SETTINGS, PROFILE
    }

    private Rbac() {
    }

    public static boolean canAccess(Module module) {
        if (module == Module.DASHBOARD) {
            return Session.isLoggedIn();
        }
        User user = Session.getCurrentUser();
        if (user == null) {
            return false;
        }
        switch (module) {
            case DASHBOARD:
            case PATIENTS:
            case PHARMACY:
            case LABORATORY:
                return true;
            case DOCTORS:
            case NURSES:
            case BILLING:
            case REPORTS:
            case SETTINGS:
                return user.isAdmin();
            case APPOINTMENTS:
                return user.isAdmin() || user.isDoctor();
            case DEPARTMENTS:
                return user.isAdmin();
            case WARD:
                return user.isAdmin() || user.isNurse();
            case PROFILE:
                return true;
            default:
                return false;
        }
    }

    public static boolean canCreatePatients() {
        User user = Session.getCurrentUser();
        return user != null && (user.isAdmin() || user.isNurse());
    }

    public static boolean canDeletePatients() {
        User user = Session.getCurrentUser();
        return user != null && user.isAdmin();
    }

    public static boolean canManageDoctors() {
        User user = Session.getCurrentUser();
        return user != null && user.isAdmin();
    }

    public static boolean canManageBilling() {
        User user = Session.getCurrentUser();
        return user != null && user.isAdmin();
    }

    public static boolean canManageMedicineInventory() {
        User user = Session.getCurrentUser();
        return user != null && user.isAdmin();
    }

    public static boolean canCreatePrescription() {
        User user = Session.getCurrentUser();
        return user != null && (user.isAdmin() || user.isDoctor());
    }

    public static boolean canDispensePrescription() {
        User user = Session.getCurrentUser();
        return user != null && (user.isAdmin() || user.isNurse());
    }

    public static boolean canManageLabCatalog() {
        User user = Session.getCurrentUser();
        return user != null && user.isAdmin();
    }

    public static boolean canOrderLabTest() {
        User user = Session.getCurrentUser();
        return user != null && (user.isAdmin() || user.isDoctor());
    }

    public static boolean canUpdateLabOrder() {
        User user = Session.getCurrentUser();
        return user != null;
    }

    public static String portalLabel() {
        User user = Session.getCurrentUser();
        if (user == null) {
            return "Portal";
        }
        if (user.isAdmin()) {
            return "Admin Portal";
        }
        if (user.isDoctor()) {
            return "Doctor Portal";
        }
        if (user.isNurse()) {
            return "Nurse Portal";
        }
        return "Portal";
    }

    public static String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
