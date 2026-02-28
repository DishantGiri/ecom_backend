package fishtail.ecom.service;

import fishtail.ecom.dto.AuthRequest;
import fishtail.ecom.dto.AuthResponse;
import fishtail.ecom.entity.User;
import fishtail.ecom.repository.UserRepository;
import fishtail.ecom.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final JwtUtils jwtUtils;
        private final AuthenticationManager authenticationManager;
        private final UserDetailsService userDetailsService;

        public AuthResponse login(AuthRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                String token = jwtUtils.generateToken(userDetails);

                return AuthResponse.builder()
                                .token(token)
                                .email(user.getEmail())
                                .role(user.getRole().name())
                                .mustChangePassword(user.isRequirePasswordChange())
                                .build();
        }
}
