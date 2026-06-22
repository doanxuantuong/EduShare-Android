package com.example.edushare.common.helper;

import android.widget.EditText;

public class RegisterValidator {
    public static boolean validate(EditText teFullname, EditText teEmail, EditText teStudentCode ,EditText tePhone,
                                   EditText tePassword, EditText teRewritePassword) {

        String fullname = teFullname.getText().toString().trim();
        String email = teEmail.getText().toString().trim();
        String studentCode = teStudentCode.getText().toString().trim();
        String phone = tePhone.getText().toString().trim();
        String password = tePassword.getText().toString().trim();
        String rewritePassword = teRewritePassword.getText().toString().trim();


        // 1. Kiểm tra Fullname
        if (fullname.isEmpty()) {
            teFullname.setError("Không được để trống");
            teFullname.requestFocus();
            return false;
        }
        if (fullname.length() < 2) {
            teFullname.setError("Tên quá ngắn");
            teFullname.requestFocus();
            return false;
        }

        // 2. Kiểm tra Email
        if (email.isEmpty()) {
            teEmail.setError("Email không được để trống");
            teEmail.requestFocus();
            return false;
        }

        String emailRegex = "^[0-9]{8}@st\\.hcmuaf\\.edu\\.vn$";
        if (!email.matches(emailRegex)) {
            teEmail.setError("Email phải có dạng MSSV@st.hcmuaf.edu.vn (VD: 23130192@st.hcmuaf.edu.vn)");
            teEmail.requestFocus();
            return false;
        }

        // 1. Kiểm tra Fullname
        if (studentCode.isEmpty()) {
            teStudentCode.setError("Không được để trống");
            teStudentCode.requestFocus();
            return false;
        }

        // 3. Kiểm tra Phone
        if (phone.isEmpty()) {
            tePhone.setError("Không được để trống");
            tePhone.requestFocus();
            return false;
        }
        if (!phone.matches("\\d{10}")) {
            tePhone.setError("Số điện thoại phải 10 số");
            tePhone.requestFocus();
            return false;
        }

        // 4. Kiểm tra Password
        if (password.isEmpty()) {
            tePassword.setError("Không được để trống");
            tePassword.requestFocus();
            return false;
        }

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_\\-])[A-Za-z\\d@$!%*?&.#_\\-]{8,25}$";
        if (!password.matches(passwordRegex)) {
            tePassword.setError("Mật khẩu phải từ 8 đến 25 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
            tePassword.requestFocus();
            return false;
        }

        // 5. Kiểm tra Rewrite Password
        if (!rewritePassword.equals(password)) {
            teRewritePassword.setError("Mật khẩu không khớp");
            teRewritePassword.requestFocus();
            return false;
        }

        return true; // Tất cả đều ổn
    }
}
