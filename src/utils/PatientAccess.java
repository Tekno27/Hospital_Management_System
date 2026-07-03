package utils;

import database.NurseDAO;
import database.PatientDAO;
import models.Patient;
import models.User;

import java.util.Collections;
import java.util.List;

public final class PatientAccess {

    private PatientAccess() {
    }

    public static List<Patient> getVisiblePatients() {
        User user = Session.getCurrentUser();
        if (user == null) {
            return Collections.emptyList();
        }
        PatientDAO dao = new PatientDAO();
        if (user.isAdmin()) {
            return dao.getAll();
        }
        if (user.isDoctor() && user.getLinkedStaffId() != null) {
            return dao.getByDoctorId(user.getLinkedStaffId());
        }
        if (user.isNurse() && user.getLinkedStaffId() != null) {
            String ward = new NurseDAO().getWardById(user.getLinkedStaffId());
            if (ward != null) {
                return dao.getByWard(ward);
            }
        }
        return Collections.emptyList();
    }

    public static List<Patient> searchVisiblePatients(String keyword) {
        User user = Session.getCurrentUser();
        if (user == null) {
            return Collections.emptyList();
        }
        PatientDAO dao = new PatientDAO();
        if (user.isAdmin()) {
            return dao.search(keyword);
        }
        if (user.isDoctor() && user.getLinkedStaffId() != null) {
            return dao.searchForDoctor(keyword, user.getLinkedStaffId());
        }
        if (user.isNurse() && user.getLinkedStaffId() != null) {
            String ward = new NurseDAO().getWardById(user.getLinkedStaffId());
            if (ward != null) {
                return dao.searchForWard(keyword, ward);
            }
        }
        return Collections.emptyList();
    }

    public static int countVisiblePatients() {
        User user = Session.getCurrentUser();
        if (user == null) {
            return 0;
        }
        PatientDAO dao = new PatientDAO();
        if (user.isAdmin()) {
            return dao.countAll();
        }
        if (user.isDoctor() && user.getLinkedStaffId() != null) {
            return dao.countByDoctorId(user.getLinkedStaffId());
        }
        if (user.isNurse() && user.getLinkedStaffId() != null) {
            String ward = new NurseDAO().getWardById(user.getLinkedStaffId());
            if (ward != null) {
                return dao.countByWard(ward);
            }
        }
        return 0;
    }

    public static String nurseWard() {
        User user = Session.getCurrentUser();
        if (user != null && user.isNurse() && user.getLinkedStaffId() != null) {
            return new NurseDAO().getWardById(user.getLinkedStaffId());
        }
        return null;
    }
}
