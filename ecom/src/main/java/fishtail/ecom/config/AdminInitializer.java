package fishtail.ecom.config;

import fishtail.ecom.entity.Role;
import fishtail.ecom.entity.User;
import fishtail.ecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("dishantgiri22@gmail.com").isEmpty()) {
            User admin = User.builder()
                    .email("dishantgiri22@gmail.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .requirePasswordChange(true)
                    .country("System")
                    .build();
            userRepository.save(admin);
            System.out.println("Default admin user created: admin@ecom.com / admin123");
        }
    }
}
