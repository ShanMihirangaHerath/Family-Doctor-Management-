const express = require('express');
const mysql = require('mysql2/promise');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const cors = require('cors');
const crypto = require('crypto');
require('dotenv').config();

const app = express();

app.use(cors());
app.use(express.json()); // 🔴 ඕනෑම JSON එකක් බාරගන්නවා (No more 415!)

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

// 2. ADD STAFF ENDPOINT
app.post('/api/staff/add', async (req, res) => {
    try {
        const data = req.body;
        const fullName = `${data.firstName || ''} ${data.lastName || ''}`.trim();
        const rawPassword = data.password || "fdhealth123";
        const hashedPassword = await bcrypt.hash(rawPassword, 10);
        const qrCode = "FD-" + crypto.randomUUID().substring(0, 8).toUpperCase();

        const query = `INSERT INTO staff_members (email, password, full_name, first_name, middle_name, last_name, nic, phone, mobile_no, whatsapp_no, address, bank_name, branch_name, account_name, account_number, role, qr_code_string, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())`;
        const [result] = await pool.query(query, [data.email, hashedPassword, fullName, data.firstName, data.middleName, data.lastName, data.nic, data.phone, data.mobileNo, data.whatsappNo, data.address, data.bankName, data.branchName, data.accountName, data.accountNumber, data.role || 'STAFF', qrCode]);

        return res.json({ id: result.insertId, message: "Staff registered successfully!" });
    } catch (error) {
        return res.status(400).json({ message: error.message });
    }
});

// 3. UPDATE STAFF ENDPOINT
app.put('/api/staff/update/:id', async (req, res) => {
    try {
        const data = req.body;
        const fullName = `${data.firstName || ''} ${data.lastName || ''}`.trim();
        const query = `UPDATE staff_members SET email=?, full_name=?, first_name=?, middle_name=?, last_name=?, nic=?, phone=?, mobile_no=?, whatsapp_no=?, address=?, bank_name=?, branch_name=?, account_name=?, account_number=?, role=? WHERE id=?`;
        await pool.query(query, [data.email, fullName, data.firstName, data.middleName, data.lastName, data.nic, data.phone, data.mobileNo, data.whatsappNo, data.address, data.bankName, data.branchName, data.accountName, data.accountNumber, data.role, req.params.id]);
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

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => console.log(`🚀 Node.js Backend running on port ${PORT}`));