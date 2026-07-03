PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     username TEXT NOT NULL UNIQUE,
                                     password TEXT NOT NULL,
                                     role TEXT NOT NULL CHECK (role IN ('ADMIN', 'DOCTOR', 'NURSE')),
    full_name TEXT NOT NULL,
    linked_staff_id INTEGER,
    created_at TEXT DEFAULT (datetime('now'))
    );

CREATE TABLE IF NOT EXISTS doctors (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       full_name TEXT NOT NULL,
                                       specialization TEXT,
                                       phone TEXT,
                                       email TEXT,
                                       department TEXT,
                                       created_at TEXT DEFAULT (datetime('now'))
    );

CREATE TABLE IF NOT EXISTS nurses (
                                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                                      full_name TEXT NOT NULL,
                                      phone TEXT,
                                      email TEXT,
                                      ward TEXT,
                                      shift TEXT CHECK (shift IN ('MORNING', 'AFTERNOON', 'NIGHT')),
    created_at TEXT DEFAULT (datetime('now'))
    );

CREATE TABLE IF NOT EXISTS patients (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        full_name TEXT NOT NULL,
                                        date_of_birth TEXT,
                                        gender TEXT CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    phone TEXT,
    address TEXT,
    blood_group TEXT,
    assigned_doctor_id INTEGER,
    ward TEXT,
    status TEXT NOT NULL DEFAULT 'ADMITTED' CHECK (status IN ('ADMITTED', 'DISCHARGED', 'OUTPATIENT')),
    admitted_date TEXT DEFAULT (datetime('now')),
    discharged_date TEXT,
    notes TEXT,
    FOREIGN KEY (assigned_doctor_id) REFERENCES doctors(id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS billing (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       patient_id INTEGER NOT NULL,
                                       description TEXT NOT NULL,
                                       amount REAL NOT NULL,
                                       status TEXT NOT NULL DEFAULT 'UNPAID' CHECK (status IN ('UNPAID', 'PAID', 'PARTIAL')),
    amount_paid REAL NOT NULL DEFAULT 0,
    bill_date TEXT DEFAULT (datetime('now')),
    due_date TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
    );

INSERT OR IGNORE INTO users (id, username, password, role, full_name)
VALUES (1, 'admin', 'admin123', 'ADMIN', 'System Administrator');

INSERT OR IGNORE INTO doctors (id, full_name, specialization, phone, email, department)
VALUES
    (1, 'Dr. Kwame Mensah', 'Cardiology', '0244123456', 'kmensah@hospital.com', 'Cardiology'),
    (2, 'Dr. Ama Owusu', 'Pediatrics', '0244987654', 'aowusu@hospital.com', 'Pediatrics');

INSERT OR IGNORE INTO nurses (id, full_name, phone, email, ward, shift)
VALUES
    (1, 'Nurse Akosua Boateng', '0204556677', 'aboateng@hospital.com', 'Ward A', 'MORNING'),
    (2, 'Nurse Yaw Asante', '0204998877', 'yasante@hospital.com', 'Ward B', 'NIGHT');

INSERT OR IGNORE INTO users (id, username, password, role, full_name, linked_staff_id)
VALUES
    (2, 'kmensah', 'password', 'DOCTOR', 'Dr. Kwame Mensah', 1),
    (3, 'aboateng', 'password', 'NURSE', 'Nurse Akosua Boateng', 1);

CREATE TABLE IF NOT EXISTS medicines (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category TEXT,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    unit_price REAL NOT NULL DEFAULT 0,
    reorder_level INTEGER DEFAULT 10,
    expiry_date TEXT,
    created_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS prescriptions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    doctor_id INTEGER,
    medicine_id INTEGER NOT NULL,
    dosage TEXT NOT NULL,
    frequency TEXT,
    duration TEXT,
    quantity INTEGER NOT NULL DEFAULT 1,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'DISPENSED', 'CANCELLED')),
    prescribed_date TEXT DEFAULT (datetime('now')),
    dispensed_date TEXT,
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE SET NULL,
    FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS lab_tests (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category TEXT,
    price REAL DEFAULT 0,
    normal_range TEXT,
    created_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS lab_orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    doctor_id INTEGER,
    test_id INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'ORDERED' CHECK (status IN ('ORDERED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    result_value TEXT,
    result_notes TEXT,
    ordered_date TEXT DEFAULT (datetime('now')),
    completed_date TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE SET NULL,
    FOREIGN KEY (test_id) REFERENCES lab_tests(id) ON DELETE RESTRICT
);

INSERT OR IGNORE INTO patients (id, full_name, date_of_birth, gender, phone, address, blood_group, assigned_doctor_id, ward, status, notes)
VALUES
    (1, 'Kofi Asante', '1985-03-12', 'MALE', '0244111222', 'Accra', 'O+', 1, 'Ward A', 'ADMITTED', 'Cardiology follow-up'),
    (2, 'Abena Mensah', '1992-07-22', 'FEMALE', '0244333444', 'Kumasi', 'A+', 2, 'Ward B', 'ADMITTED', 'Pediatric care'),
    (3, 'James Osei', '1978-11-05', 'MALE', '0244555666', 'Tema', 'B+', 1, 'Ward A', 'OUTPATIENT', 'Emergency referral');

INSERT OR IGNORE INTO medicines (id, name, category, stock_quantity, unit_price, reorder_level, expiry_date)
VALUES
    (1, 'Paracetamol 500mg', 'Analgesic', 500, 0.50, 100, '2027-06-30'),
    (2, 'Amoxicillin 250mg', 'Antibiotic', 200, 2.50, 50, '2026-12-31'),
    (3, 'Metformin 500mg', 'Antidiabetic', 150, 1.20, 40, '2027-03-15'),
    (4, 'Ibuprofen 400mg', 'Analgesic', 80, 0.80, 30, '2026-09-20');

INSERT OR IGNORE INTO lab_tests (id, name, category, price, normal_range)
VALUES
    (1, 'Full Blood Count', 'Hematology', 45.00, 'See report'),
    (2, 'Blood Glucose (Fasting)', 'Biochemistry', 25.00, '3.9-5.5 mmol/L'),
    (3, 'Urinalysis', 'Microbiology', 30.00, 'Normal'),
    (4, 'Liver Function Test', 'Biochemistry', 80.00, 'See report');

INSERT OR IGNORE INTO prescriptions (id, patient_id, doctor_id, medicine_id, dosage, frequency, duration, quantity, status, notes)
VALUES
    (1, 1, 1, 1, '500mg', 'Twice daily', '5 days', 10, 'PENDING', 'For headache'),
    (2, 2, 2, 2, '250mg', 'Three times daily', '7 days', 21, 'PENDING', 'Infection treatment');

INSERT OR IGNORE INTO lab_orders (id, patient_id, doctor_id, test_id, status, result_notes)
VALUES
    (1, 1, 1, 1, 'ORDERED', 'Routine check'),
    (2, 3, 1, 2, 'IN_PROGRESS', 'Fasting required');

CREATE TABLE IF NOT EXISTS departments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    floor TEXT,
    head_doctor_id INTEGER,
    bed_capacity INTEGER DEFAULT 20,
    phone TEXT,
    description TEXT,
    FOREIGN KEY (head_doctor_id) REFERENCES doctors(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS wards (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    department TEXT,
    bed_capacity INTEGER NOT NULL DEFAULT 10,
    floor TEXT,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS appointments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    doctor_id INTEGER NOT NULL,
    appointment_date TEXT NOT NULL,
    appointment_time TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    reason TEXT,
    notes TEXT,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);

INSERT OR IGNORE INTO departments (id, name, floor, head_doctor_id, bed_capacity, phone, description)
VALUES
    (1, 'Cardiology', 'Floor 2', 1, 30, '030-200-1001', 'Heart and vascular care'),
    (2, 'Pediatrics', 'Floor 1', 2, 25, '030-200-1002', 'Child and adolescent health'),
    (3, 'Emergency', 'Ground', NULL, 40, '030-200-1003', '24/7 emergency services');

INSERT OR IGNORE INTO wards (id, name, department, bed_capacity, floor, notes)
VALUES
    (1, 'Ward A', 'Cardiology', 15, 'Floor 2', 'Cardiac monitoring unit'),
    (2, 'Ward B', 'Pediatrics', 12, 'Floor 1', 'Pediatric inpatient beds'),
    (3, 'Ward C', 'Emergency', 20, 'Ground', 'Acute care overflow');

INSERT OR IGNORE INTO appointments (id, patient_id, doctor_id, appointment_date, appointment_time, status, reason, notes)
VALUES
    (1, 1, 1, date('now', '+1 day'), '09:00', 'SCHEDULED', 'Follow-up consultation', 'Post-admission review'),
    (2, 2, 2, date('now', '+2 day'), '11:30', 'SCHEDULED', 'Pediatric check-up', 'Routine visit'),
    (3, 3, 1, date('now'), '14:00', 'COMPLETED', 'Blood pressure review', 'Completed today');