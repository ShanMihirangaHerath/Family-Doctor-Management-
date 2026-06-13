const express = require('express');
const mysql = require('mysql2/promise');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const cors = require('cors');
const crypto = require('crypto');
require('dotenv').config();

// 🔴 අලුත්ම Firebase Admin v12+ එකට ගැලපෙන විදිහට Import කිරීම
const { initializeApp, cert } = require('firebase-admin/app');
const { getMessaging } = require('firebase-admin/messaging');

try {
    const serviceAccount = require("./serviceAccountKey.json");
    initializeApp({
        credential: cert(serviceAccount)
    });
    console.log("🔥 Firebase Admin initialized successfully!");
} catch (error) {
    console.error("⚠️ Firebase initialization failed:", error.message);
}

const app = express();

app.use(cors());
app.use(express.json());

// Database Connection Pool
const pool = mysql.createPool({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME
});

const JWT_SECRET = process.env.JWT_SECRET;

// 1. LOGIN ENDPOINT
app.post('/api/staff/login', async (req, res) => {
    try {
        const { email, password } = req.body;
        if (!email || !password) return res.status(400).json({ message: "Email and password are required" });

        const [rows] = await pool.query('SELECT * FROM staff_members WHERE email = ?', [email]);
        if (rows.length === 0) return res.status(400).json({ message: "User not found!" });

        const staff = rows[0];
        const isMatch = await bcrypt.compare(password, staff.password);
        if (!isMatch) return res.status(400).json({ message: "Invalid password!" });

        const token = jwt.sign({ sub: staff.email, role: staff.role, name: staff.full_name }, JWT_SECRET, { expiresIn: '10h' });

        return res.json({ token, role: staff.role, name: staff.full_name, id: staff.id });
    } catch (error) {
        return res.status(500).json({ message: error.message });
    }
});

// ==========================================
// 2. ADD STAFF ENDPOINT (WITH CV URL)
// ==========================================
app.post('/api/staff/add', async (req, res) => {
    try {
        const data = req.body;
        const fullName = `${data.firstName || ''} ${data.lastName || ''}`.trim();
        const rawPassword = data.password || "fdhealth123";
        const hashedPassword = await bcrypt.hash(rawPassword, 10);
        const qrCode = "FD-" + crypto.randomUUID().substring(0, 8).toUpperCase();
        const cvUrl = data.cvUrl || null; // 🔴 CV URL එක ගන්නවා

        const query = `
            INSERT INTO staff_members (
                email, password, full_name, first_name, middle_name, last_name, 
                nic, phone, mobile_no, whatsapp_no, address, bank_name, branch_name, 
                account_name, account_number, role, qr_code_string, cv_url, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())`;
        
        const values = [
            data.email, hashedPassword, fullName, data.firstName, data.middleName, data.lastName, 
            data.nic, data.phone, data.mobileNo, data.whatsappNo, data.address, data.bankName, 
            data.branchName, data.accountName, data.accountNumber, data.role || 'STAFF', qrCode, cvUrl
        ];

        const [result] = await pool.query(query, values);
        return res.json({ id: result.insertId, message: "Staff registered successfully!" });
    } catch (error) {
        return res.status(400).json({ message: error.message });
    }
});

// ==========================================
// 3. UPDATE STAFF ENDPOINT (WITH CV URL)
// ==========================================
app.put('/api/staff/update/:id', async (req, res) => {
    try {
        const data = req.body;
        const fullName = `${data.firstName || ''} ${data.lastName || ''}`.trim();
        const cvUrl = data.cvUrl || null; // 🔴 CV URL එක ගන්නවා

        const query = `
            UPDATE staff_members SET 
                email=?, full_name=?, first_name=?, middle_name=?, last_name=?, 
                nic=?, phone=?, mobile_no=?, whatsapp_no=?, address=?, bank_name=?, 
                branch_name=?, account_name=?, account_number=?, role=?, cv_url=? 
            WHERE id=?`;
        
        const values = [
            data.email, fullName, data.firstName, data.middleName, data.lastName, 
            data.nic, data.phone, data.mobileNo, data.whatsappNo, data.address, 
            data.bankName, data.branchName, data.accountName, data.accountNumber, 
            data.role, cvUrl, req.params.id
        ];

        await pool.query(query, values);
        return res.json({ message: "Profile updated successfully!" });
    } catch (error) {
        return res.status(400).json({ message: error.message });
    }
});

// 4. GET ALL STAFF
app.get('/api/staff/all', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT id, full_name as fullName, first_name as firstName, last_name as lastName, email, role, nic, phone FROM staff_members');
        return res.json(rows);
    } catch (error) {
        return res.status(500).json({ message: error.message });
    }
});

// 5. GET SINGLE STAFF PROFILE
app.get('/api/staff/:id', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM staff_members WHERE id = ?', [req.params.id]);
        if (rows.length === 0) return res.status(404).json({ message: "Not found" });
        return res.json(rows[0]);
    } catch (error) {
        return res.status(500).json({ message: error.message });
    }
});

// ==========================================
// 6. GET TODAY'S ATTENDANCE STATUS
// ==========================================
app.get('/api/attendance/status/:staffId', async (req, res) => {
    try {
        const staffId = req.params.staffId;
        
        // අද දවසේ මේ Staff සාමාජිකයාට Record එකක් තියෙනවද බලනවා
        const query = `
            SELECT * FROM attendance_records 
            WHERE staff_id = ? AND DATE(check_in_time) = CURDATE()
            LIMIT 1
        `;
        const [rows] = await pool.query(query, [staffId]);

        if (rows.length === 0) {
            return res.json({ status: "NOT_CHECKED_IN", record: null });
        }

        const record = rows[0];
        if (record.check_out_time === null) {
            return res.json({ status: "CHECKED_IN", record: record });
        } else {
            return res.json({ status: "CHECKED_OUT", record: record });
        }
    } catch (error) {
        return res.status(500).json({ message: error.message });
    }
});

// ==========================================
// 7. CHECK IN ENDPOINT
// ==========================================
app.post('/api/attendance/check-in', async (req, res) => {
    try {
        const { staffId, latitude, longitude, isGeofenceUsed, isQrUsed } = req.body;

        // දැනටමත් අද දවසේ Check In වෙලාද කියලා double check කරනවා
        const [existing] = await pool.query(
            'SELECT id FROM attendance_records WHERE staff_id = ? AND DATE(check_in_time) = CURDATE()',
            [staffId]
        );
        if (existing.length > 0) {
            return res.status(400).json({ message: "Already checked in for today!" });
        }

        const query = `
            INSERT INTO attendance_records (
                staff_id, check_in_latitude, check_in_longitude, check_in_time,
                is_geofence_used, is_qr_used, status, approval_status
            ) VALUES (?, ?, ?, NOW(), ?, ?, 'PRESENT', 'APPROVED')
        `;

        // BIT(1) ෆීල්ඩ්ස් නිසා true/false වෙනුවට 1 හෝ 0 පාස් කරනවා
        await pool.query(query, [
            staffId, latitude, longitude, 
            isGeofenceUsed ? 1 : 0, isQrUsed ? 1 : 0
        ]);

        return res.json({ message: "Checked in successfully!" });
    } catch (error) {
        return res.status(400).json({ message: error.message });
    }
});

// ==========================================
// 8. CHECK OUT ENDPOINT
// ==========================================
app.put('/api/attendance/check-out', async (req, res) => {
    try {
        const { staffId, latitude, longitude } = req.body;

        // අද දවසේ Check In Record එක හොයනවා
        const [records] = await pool.query(
            'SELECT id FROM attendance_records WHERE staff_id = ? AND DATE(check_in_time) = CURDATE() AND check_out_time IS NULL',
            [staffId]
        );

        if (records.length === 0) {
            return res.status(400).json({ message: "No active check-in found for today!" });
        }

        const recordId = records[0].id;
        const query = `
            UPDATE attendance_records SET 
                check_out_latitude = ?, 
                check_out_longitude = ?, 
                check_out_time = NOW() 
            WHERE id = ?
        `;
        await pool.query(query, [latitude, longitude, recordId]);

        return res.json({ message: "Checked out successfully!" });
    } catch (error) {
        return res.status(400).json({ message: error.message });
    }
});

// ==========================================
// 9. OFFICE LOCATIONS මේසය (Table) සෑදීම (නැත්නම් විතරක්)
// ==========================================
const initOfficeTable = async () => {
    await pool.query(`
        CREATE TABLE IF NOT EXISTS office_locations (
            id INT PRIMARY KEY,
            latitude DOUBLE NOT NULL,
            longitude DOUBLE NOT NULL,
            radius_meters DOUBLE NOT NULL
        )
    `);
};
initOfficeTable();

// ==========================================
// 10. GET OFFICE LOCATION (Staff එකටයි Admin එකටයි දෙකටම)
// ==========================================
app.get('/api/office/get-location', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT latitude, longitude, radius_meters as radiusMeters FROM office_locations WHERE id = 1');
        if (rows.length === 0) {
            // මුකුත්ම නැත්නම් Default Colombo ලොකේෂන් එක දෙනවා
            return res.json({ latitude: 6.927079, longitude: 79.861244, radiusMeters: 50.0 });
        }
        return res.json(rows[0]);
    } catch (error) {
        return res.status(500).json({ message: error.message });
    }
});

// ==========================================
// 11. SET/UPDATE OFFICE LOCATION (ADMIN)
// ==========================================
app.post('/api/office/set-location', async (req, res) => {
    try {
        const { latitude, longitude, radiusMeters } = req.body;
        const [rows] = await pool.query('SELECT id FROM office_locations WHERE id = 1');
        
        if (rows.length === 0) {
            await pool.query('INSERT INTO office_locations (id, latitude, longitude, radius_meters) VALUES (1, ?, ?, ?)', [latitude, longitude, radiusMeters]);
        } else {
            await pool.query('UPDATE office_locations SET latitude = ?, longitude = ?, radius_meters = ? WHERE id = 1', [latitude, longitude, radiusMeters]);
        }
        return res.json({ message: "Office location configuration updated!" });
    } catch (error) {
        return res.status(400).json({ message: error.message });
    }
});

// ==========================================
// 12. GET ALL ATTENDANCE LOGS BY DATE (ADMIN)
// ==========================================
app.get('/api/attendance/admin/logs', async (req, res) => {
    try {
        const { date } = req.query; // YYYY-MM-DD ෆෝමැට් එකෙන් එන්නේ
        const query = `
            SELECT a.*, s.full_name as staffName, s.role as staffRole 
            FROM attendance_records a
            JOIN staff_members s ON a.staff_id = s.id
            WHERE DATE(a.check_in_time) = ?
            ORDER BY a.check_in_time DESC
        `;
        const [rows] = await pool.query(query, [date]);
        return res.json(rows);
    } catch (error) {
        return res.status(500).json({ message: error.message });
    }
});

// ==========================================
// 13. APPROVE / REJECT ATTENDANCE RECORD (ADMIN)
// ==========================================
app.put('/api/attendance/admin/status/:id', async (req, res) => {
    try {
        const { approvalStatus } = req.body; // 'APPROVED' හෝ 'REJECTED'
        await pool.query('UPDATE attendance_records SET approval_status = ? WHERE id = ?', [approvalStatus, req.params.id]);
        return res.json({ message: `Attendance successfully ${approvalStatus}!` });
    } catch (error) {
        return res.status(400).json({ message: error.message });
    }
});
// ==========================================
// 14. GET ALL LEAVE REQUESTS FOR ADMIN 
// ==========================================
app.get('/api/leave/admin/all', async (req, res) => {
    try {
        const query = `
            SELECT l.*, s.full_name as staffName, s.role as staffRole 
            FROM leave_requests l 
            JOIN staff_members s ON l.staff_id = s.id
            ORDER BY FIELD(l.status, 'PENDING', 'APPROVED', 'REJECTED'), l.applied_on DESC
        `;
        const [rows] = await pool.query(query);
        return res.json(rows);
    } catch (error) {
        return res.status(500).json({ message: error.message });
    }
});

// ==========================================
// 15. APPROVE / REJECT LEAVE REQUEST (ADMIN)
// ==========================================
app.put('/api/leave/admin/status/:id', async (req, res) => {
    try {
        await pool.query('UPDATE leave_requests SET status = ? WHERE id = ?', [req.body.status, req.params.id]);
        return res.json({ message: `Leave application status updated!` });
    } catch (error) {
        return res.status(400).json({ message: error.message });
    }
});

// ==========================================
// 17. STAFF SIDE: APPLY LEAVE APPLICATION
// ==========================================
app.post('/api/leave/apply/:staffId', async (req, res) => {
    try {
        const query = `
            INSERT INTO leave_requests (staff_id, start_date, end_date, reason, status, applied_on)
            VALUES (?, ?, ?, ?, 'PENDING', NOW())
        `;
        await pool.query(query, [req.params.staffId, req.body.startDate, req.body.endDate, req.body.reason]);
        return res.json({ message: "Leave application submitted successfully!" });
    } catch (error) {
        return res.status(400).json({ message: error.message });
    }
});

// ==========================================
// 18. STAFF SIDE: GET LEAVE HISTORY
// ==========================================
app.get('/api/leave/history/:staffId', async (req, res) => {
    try {
        const query = `
            SELECT id, start_date as startDate, end_date as endDate, reason, status 
            FROM leave_requests WHERE staff_id = ? ORDER BY applied_on DESC
        `;
        const [rows] = await pool.query(query, [req.params.staffId]);
        return res.json(rows);
    } catch (error) {
        return res.status(500).json({ message: error.message });
    }
});

// ==========================================
// 19. ADMIN SIDE: GET ALL LIVE LOCATIONS
// ==========================================
app.get('/api/location/all', async (req, res) => {
    try {
        // ලොකේෂන් එකක් තියෙන (Update වෙච්ච) අය විතරක් ගන්නවා
        const query = `
            SELECT id, full_name as staffName, role as staffRole, 
                   assigned_latitude as latitude, assigned_longitude as longitude
            FROM staff_members
            WHERE assigned_latitude IS NOT NULL AND assigned_longitude IS NOT NULL
        `;
        const [rows] = await pool.query(query);
        return res.json(rows);
    } catch (error) {
        return res.status(500).json({ message: error.message });
    }
});

// ==========================================
// 20. LIVE LOCATION TRACKING RECEIVER (10s Interval) - UPDATE KELA
// ==========================================
app.post('/api/location/update/:staffId', async (req, res) => {
    try {
        const staffId = req.params.staffId;
        const { latitude, longitude } = req.body;

        // 1. Current location staff table madhe update kara
        const updateQuery = `UPDATE staff_members SET assigned_latitude = ?, assigned_longitude = ? WHERE id = ?`;
        await pool.query(updateQuery, [latitude, longitude, staffId]);

        // 2. 🔴 Navin code: Location history save kara (Path draw karaylasathi)
        const historyQuery = `INSERT INTO location_history (staff_id, latitude, longitude, recorded_at) VALUES (?, ?, ?, NOW())`;
        await pool.query(historyQuery, [staffId, latitude, longitude]);

        return res.json({ message: "Live coordinates synced and history saved!" });
    } catch (error) {
        return res.status(400).json({ message: error.message });
    }
});

// ==========================================
// 21. ADMIN SIDE: GET STAFF LOCATION HISTORY (ROUTE/PATH) BY DATE
// ==========================================
app.get('/api/location/history/:staffId', async (req, res) => {
    try {
        const staffId = req.params.staffId;
        const { date } = req.query; // YYYY-MM-DD format

        const query = `
            SELECT latitude, longitude, recorded_at 
            FROM location_history 
            WHERE staff_id = ? AND DATE(recorded_at) = ?
            ORDER BY recorded_at ASC
        `;
        const [rows] = await pool.query(query, [staffId, date]);
        return res.json(rows);
    } catch (error) {
        return res.status(500).json({ message: error.message });
    }
});
// ==========================================
// 22. ADMIN SIDE: SEND FIREBASE PUSH NOTIFICATIONS
// ==========================================
app.post('/api/notifications/send', async (req, res) => {
    try {
        const { targetStaffId, title, message } = req.body;

        if (targetStaffId === 'ALL') {
            // Bulk Messaging
            const [staff] = await pool.query('SELECT id, fcm_token FROM staff_members');
            if(staff.length === 0) return res.status(400).json({message: "No staff members found!"});
            
            const values = staff.map(s => [s.id, title, message]);
            await pool.query('INSERT INTO notifications (staff_id, title, message) VALUES ?', [values]);
            
            // 🔴 අලුත් getMessaging() ක්‍රමය 
            const tokens = staff.filter(s => s.fcm_token).map(s => s.fcm_token);
            if (tokens.length > 0) {
                const payload = {
                    notification: { title: title, body: message },
                    tokens: tokens 
                };
                await getMessaging().sendEachForMulticast(payload);
            }
            
            return res.json({ message: "Bulk notification sent via Firebase!" });

        } else {
            // Single Messaging
            await pool.query(
                'INSERT INTO notifications (staff_id, title, message) VALUES (?, ?, ?)', 
                [targetStaffId, title, message]
            );

            const [staffRows] = await pool.query('SELECT fcm_token FROM staff_members WHERE id = ?', [targetStaffId]);
            if (staffRows.length > 0 && staffRows[0].fcm_token) {
                const payload = {
                    notification: { title: title, body: message },
                    token: staffRows[0].fcm_token
                };
                // 🔴 අලුත් getMessaging() ක්‍රමය
                await getMessaging().send(payload);
            }

            return res.json({ message: "Push Notification sent successfully!" });
        }
    } catch (error) {
        console.error("Firebase Error: ", error);
        return res.status(500).json({ message: error.message });
    }
});

// ==========================================
// 23. STAFF SIDE: GET REAL NOTIFICATIONS (OVERWRITE OLD ONE)
// ==========================================
app.get('/api/notifications/:staffId', async (req, res) => {
    try {
        // අලුත්ම මැසේජ් එක උඩින්ම එන්න ORDER BY DESC දාලා ගන්නවා
        const query = `SELECT * FROM notifications WHERE staff_id = ? ORDER BY created_at DESC`;
        const [rows] = await pool.query(query, [req.params.staffId]);
        return res.json(rows);
    } catch (error) {
        return res.status(500).json({ message: error.message });
    }
});

// ==========================================
// 24. ADMIN DASHBOARD STATS
// ==========================================
app.get('/api/dashboard/stats', async (req, res) => {
    try {
        // 1. Total Staff
        const [staff] = await pool.query('SELECT COUNT(*) as count FROM staff_members');
        const totalStaff = staff[0].count;

        // 2. Active Branches (office_locations theke)
        const [branches] = await pool.query('SELECT COUNT(*) as count FROM office_locations');
        const activeBranches = branches[0].count;

        // 3. Present Today (Ajke koto jon check-in koreche)
        const [present] = await pool.query('SELECT COUNT(DISTINCT staff_id) as count FROM attendance_records WHERE DATE(check_in_time) = CURDATE()');
        const presentToday = present[0].count;

        // 4. On Leave Today (Ajke koto jon chutite ache)
        const [leave] = await pool.query("SELECT COUNT(DISTINCT staff_id) as count FROM leave_requests WHERE CURDATE() BETWEEN start_date AND end_date AND status = 'APPROVED'");
        const onLeaveToday = leave[0].count;

        return res.json({
            totalStaff: totalStaff || 0,
            activeBranches: activeBranches || 0,
            presentToday: presentToday || 0,
            onLeaveToday: onLeaveToday || 0
        });
    } catch (error) {
        console.error("Dashboard Error: ", error);
        return res.status(500).json({ message: error.message });
    }
});

// ==========================================
// 25. STAFF SIDE: GET ATTENDANCE HISTORY
// ==========================================
app.get('/api/attendance/history/:staffId', async (req, res) => {
    try {
        const query = `
            SELECT check_in_time as checkInTime, check_out_time as checkOutTime 
            FROM attendance_records 
            WHERE staff_id = ? 
            ORDER BY check_in_time DESC
        `;
        const [rows] = await pool.query(query, [req.params.staffId]);
        return res.json(rows);
    } catch (error) {
        return res.status(500).json({ message: error.message });
    }
});

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => console.log(`🚀 Node.js Backend running on port ${PORT}`));