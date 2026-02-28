package fishtail.ecom.service;

import fishtail.ecom.dto.UserDTO;
import fishtail.ecom.entity.Role;
import fishtail.ecom.entity.User;
import fishtail.ecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserDTO registerUser(String email, String ipAddress) {
        String country = getCountryFromIp(ipAddress);

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("user123")) // Default password for new registrations
                .role(Role.ROLE_USER)
                .requirePasswordChange(false)
                .country(country)
                .build();

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDTO.class);
    }

    public void changePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRequirePasswordChange(false);
        userRepository.save(user);
    }

    private String getCountryFromIp(String ipAddress) {
        // Handle localhost for testing
        if (ipAddress == null || ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
            return "Localhost";
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://ip-api.com/json/" + ipAddress + "?fields=country"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                if (jsonNode != null && jsonNode.has("country")) {
                    return jsonNode.get("country").asText();
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching country for IP " + ipAddress + ": " + e.getMessage());
        }
        return "Unknown";
    }
}
