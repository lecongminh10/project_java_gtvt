package com.example.project.controller.teacher;

import com.example.project.entity.PasswordResetToken;
import com.example.project.entity.User;
import com.example.project.entity.UserStatus;
import com.example.project.repository.PasswordResetTokenRepository;
import com.example.project.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class TeacherPasswordController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:no-reply@gtvt.edu.vn}")
    private String mailFrom;

    public TeacherPasswordController(UserRepository userRepository,
                                     PasswordResetTokenRepository tokenRepository,
                                     PasswordEncoder passwordEncoder,
                                     JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @PostMapping("/forgot-password")
    public String sendResetEmail(@RequestParam("email") String email, HttpServletRequest request) {
        if (email == null || email.isBlank()) {
            return "redirect:/login?reset=invalid";
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            return "redirect:/login?reset=notfound";
        }

        User user = userOpt.get();
        if (user.getStatus() != UserStatus.ACTIVE) {
            return "redirect:/login?reset=notfound";
        }

        Optional<PasswordResetToken> existingToken = tokenRepository
                .findTopByUserAndUsedAtIsNullOrderByCreatedAtDesc(user);
        if (existingToken.isPresent() && !existingToken.get().isExpired()) {
            return "redirect:/login?reset=sent-retry";
        }

        tokenRepository.deleteByUser(user);
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(token);

        String resetUrl = ServletUriComponentsBuilder.fromRequest(request)
                .replacePath("/reset-password")
                .replaceQuery("token=" + tokenValue)
                .build()
                .toUriString();

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setFrom(mailFrom);
            helper.setSubject("Reset mật khẩu tài khoản");
            helper.setText(buildResetEmailHtml(user.getUsername(), resetUrl), true);
            mailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            return "redirect:/login?reset=error";
        }

        return "redirect:/login?reset=sent";
    }

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam(value = "token", required = false) String token,
                                Model model,
                                java.security.Principal principal) {
        if (token != null && !token.isBlank()) {
            Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
            if (tokenOpt.isEmpty() || tokenOpt.get().isExpired() || tokenOpt.get().isUsed()) {
                model.addAttribute("invalidToken", true);
                return "teacher/reset-password";
            }
            model.addAttribute("token", token);
            return "teacher/reset-password";
        }

        if (principal == null || principal.getName() == null) {
            model.addAttribute("invalidToken", true);
            return "teacher/reset-password";
        }

        Optional<User> userOpt = userRepository.findByUsername(principal.getName());
        if (userOpt.isEmpty()) {
            model.addAttribute("invalidToken", true);
            return "teacher/reset-password";
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches("12345678", user.getPassword())) {
            model.addAttribute("invalidToken", true);
            return "teacher/reset-password";
        }

        model.addAttribute("tokenless", true);
        return "teacher/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleReset(@RequestParam(value = "token", required = false) String token,
                              @RequestParam("newPassword") String newPassword,
                              @RequestParam("confirmPassword") String confirmPassword,
                              RedirectAttributes redirectAttributes,
                              java.security.Principal principal) {
        if (token == null || token.isBlank()) {
            return handleTeacherFirstLoginReset(newPassword, confirmPassword, redirectAttributes, principal);
        }

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired() || tokenOpt.get().isUsed()) {
            redirectAttributes.addFlashAttribute("resetError", "Link không hợp lệ hoặc đã hết hạn.");
            return "redirect:/reset-password?token=" + token;
        }

        if (newPassword == null || newPassword.isBlank()) {
            redirectAttributes.addFlashAttribute("resetError", "Mật khẩu mới không được để trống.");
            return "redirect:/reset-password?token=" + token;
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("resetError", "Mật khẩu xác nhận không khớp.");
            return "redirect:/reset-password?token=" + token;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        return "redirect:/login?reset=changed";
    }

    private String handleTeacherFirstLoginReset(String newPassword,
                                                String confirmPassword,
                                                RedirectAttributes redirectAttributes,
                                                java.security.Principal principal) {
        if (principal == null || principal.getName() == null) {
            redirectAttributes.addFlashAttribute("resetError", "Bạn cần đăng nhập để đổi mật khẩu.");
            return "redirect:/reset-password";
        }

        Optional<User> userOpt = userRepository.findByUsername(principal.getName());
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("resetError", "Không tìm thấy tài khoản đăng nhập.");
            return "redirect:/reset-password";
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches("12345678", user.getPassword())) {
            redirectAttributes.addFlashAttribute("resetError", "Khong du dieu kien de doi mat khau.");
            return "redirect:/reset-password";
        }

        if (newPassword == null || newPassword.isBlank()) {
            redirectAttributes.addFlashAttribute("resetError", "Mật khẩu mới không được để trống.");
            return "redirect:/reset-password";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("resetError", "Mật khẩu xác nhận không khớp.");
            return "redirect:/reset-password";
        }
        if ("12345678".equals(newPassword)) {
            redirectAttributes.addFlashAttribute("resetError", "Vui lòng không dùng lại mật khẩu mặc định.");
            return "redirect:/reset-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        if (user.getRole() == com.example.project.entity.UserRole.ADMIN) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/teacher/dashboard";
    }

    private String buildResetEmailHtml(String username, String resetUrl) {
        return String.format("""
                <div style=\"font-family: 'Inter', Arial, sans-serif; background:#f8fafc; padding:32px;\">
                    <div style=\"max-width:560px; margin:0 auto; background:#ffffff; border-radius:16px; padding:28px; box-shadow:0 10px 30px rgba(15,23,42,0.08);\">
                        <div style=\"font-size:18px; font-weight:700; color:#0f172a; margin-bottom:8px;\">Yêu cầu reset mật khẩu</div>
                        <div style=\"font-size:14px; color:#475569; margin-bottom:16px;\">Chào %s,</div>
                        <div style=\"font-size:14px; color:#475569; margin-bottom:20px;\">
                            Chúng tôi nhận được yêu cầu đổi mật khẩu. Hãy nhấn vào nút bên dưới để tạo mật khẩu mới.
                            Link sẽ hết hạn sau 15 phút.
                        </div>
                        <a href=\"%s\" style=\"display:inline-block; padding:12px 20px; background:#2563eb; color:#ffffff; text-decoration:none; border-radius:10px; font-weight:600;\">Reset mật khẩu</a>
                        <div style=\"font-size:12px; color:#94a3b8; margin-top:20px;\">
                            Nếu bạn không yêu cầu reset, vui lòng bỏ qua email này.
                        </div>
                        <div style=\"font-size:12px; color:#94a3b8; margin-top:12px;\">
                            Hoặc mở link này: <a href=\"%s\" style=\"color:#2563eb;\">%s</a>
                        </div>
                    </div>
                </div>
                """, username, resetUrl, resetUrl, resetUrl);
    }
}
