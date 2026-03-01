package fishtail.ecom.controller;

import fishtail.ecom.dto.AuthRequest;
import fishtail.ecom.dto.AuthResponse;
import fishtail.ecom.dto.UserDTO;
import fishtail.ecom.service.AuthService;
import fishtail.ecom.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request,
            java.security.Principal principal) {
        String email = principal.getName(); // Get email from authenticated user
        String newPassword = request.get("newPassword");
        try {
            userService.changePassword(email, newPassword);
            return ResponseEntity.ok("Password changed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<?> getAdminDashboard() {
        return ResponseEntity.ok("Welcome to the Admin Dashboard! Only admins can see this.");
    }

    @PostMapping("/admin/change-email")
    public ResponseEntity<?> changeEmail(@RequestBody Map<String, String> request, java.security.Principal principal) {
        String currentEmail = principal.getName();
        String newEmail = request.get("newEmail");
        String password = request.get("password");

        if (newEmail == null || password == null) {
            return ResponseEntity.badRequest().body("newEmail and password are required");
        }

        try {
            userService.changeEmail(currentEmail, newEmail, password);
            return ResponseEntity.ok("Email successfully changed. Please log in again with your new email.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/admin/change-password")
    public ResponseEntity<?> changePasswordAdmin(@RequestBody Map<String, String> request,
            java.security.Principal principal) {
        String email = principal.getName();
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("currentPassword and newPassword are required");
        }

        try {
            userService.changePasswordSecurely(email, currentPassword, newPassword);
            return ResponseEntity.ok("Password updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/admin/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody fishtail.ecom.dto.ForgotPasswordRequestDTO request) {
        try {
            userService.generatePasswordResetOtp(request.getEmail());
            return ResponseEntity.ok("If an admin account exists with that email, an OTP has been sent.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/admin/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody fishtail.ecom.dto.ResetPasswordRequestDTO request) {
        try {
            userService.resetPasswordWithOtp(request.getEmail(), request.getOtp(), request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully. You can now login with your new password.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request, HttpServletRequest servletRequest) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        // Allow the frontend to explicitly send the IP (like the tracking endpoint)
        String ipAddress = request.get("ipAddress");
        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = getClientIp(servletRequest);
        }
        try {
            UserDTO userDto = userService.registerUser(email, ipAddress);
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error registering user: " + e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";

        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }

        return remoteAddr;
    }
}
