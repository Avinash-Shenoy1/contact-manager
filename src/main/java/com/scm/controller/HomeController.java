package com.scm.controller;

import com.scm.dao.UserRepository;
import com.scm.entities.User;
import com.scm.helper.Message;
import com.scm.model.EmailRequest;
import com.scm.services.EmailService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private HttpSession session;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Smart Contact Manager");
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "Contact - Smart Contact Manager");
        return "contact";
    }


    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title", "signup - Smart Contact Manager");
        model.addAttribute("user", new User());
        return "signup";
    }

    //handler for registering user
    @PostMapping("/do_register")
    public String register(@Valid @ModelAttribute("user") User user, BindingResult bResult,
                           @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
                           Model model) {
        try {
            if (!agreement) {
                throw new Exception("You haven't agreed the terms and conditions");
            }

            if (bResult.hasErrors()) {
                System.out.println("Error: " + bResult.getAllErrors());
                return "signup";
            }

            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("image/smart.png");

            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

            User result = this.userRepository.save(user);

            model.addAttribute("user", new User());
            session.setAttribute("message", new Message("Successfully registered", "alert-success"));
            return "signup";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", user);
            session.setAttribute("message", new Message(e.getMessage(), "alert-danger"));
            return "signup";
        }
    }

    //handler for custom login
    @GetMapping("/signin")
    public String customLogin(Model model) {
        model.addAttribute("title", "Login - Smart Contact Manager");
        return "login";
    }

    // Step 1: Show Forgot Password Page
    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("title", "Forgot Password - Smart Contact Manager");
        return "forgot-password";
    }

    // Step 2: Process Email Submission & Generate OTP
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email) {
        try {
            User user = userRepository.getUserByUsername(email);

            if (user == null) {
                session.setAttribute("message", new Message("No account found with this email.", "alert-danger"));
                return "redirect:/forgot-password";
            }

            // Generate a 6-digit OTP
            int otp = new Random().nextInt(900000) + 100000; // Generates 6-digit OTP
            session.setAttribute("otp", otp);
            session.setAttribute("email", email); // Store email for verification

            String subject = "Password Reset OTP - Smart Contact Manager";
            String message = "Your OTP for password reset is: " + otp + ". This OTP is valid for 5 minutes.";

            // Send email
            boolean result = emailService.sendEmail(message, subject, email);

            session.setAttribute("message", new Message("OTP sent to " + email, "alert-success"));
        } catch (Exception e) {
            session.setAttribute("message", new Message("Error in resetting the password " + e.getMessage(), "alert-danger"));
        }
        return "redirect:/forgot-password";
    }

    // Step 3: Verify OTP
    @PostMapping("/verify-otp")
    public String verifyOTP(@RequestParam("otp") String otp) {
        Object sessionOtp = session.getAttribute("otp");

        if (sessionOtp != null && otp.equals(sessionOtp.toString())) {
            session.setAttribute("message", new Message("OTP Verified! Set a new password.", "alert-success"));
            return "redirect:/reset-password";
        } else {
            session.setAttribute("message", new Message("Invalid OTP. Try again!", "alert-danger"));
            return "redirect:/forgot-password";
        }
    }

    // Step 4: Show Reset Password Page
    @GetMapping("/reset-password")
    public String showResetPasswordPage(Model model) {
        model.addAttribute("title", "Reset Password");
        return "reset-password";
    }

    // Step 5: Update Password
    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword) {

        String email = (String) session.getAttribute("email");
        User user = userRepository.getUserByUsername(email);

        if (user == null) {
            session.setAttribute("message", new Message("No user found!", "alert-danger"));
            return "redirect:/forgot-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("message", new Message("Passwords do not match!", "alert-danger"));
            return "redirect:/reset-password";
        }

        // Encrypt new password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        // Clear session attributes after password reset
        session.removeAttribute("otp");
        session.removeAttribute("email");

        session.setAttribute("message", new Message("Password successfully updated!", "alert-success"));
        return "redirect:/signin";
    }
}
