package dev.securemask;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class SecureMaskModuleTest {
    static class Customer {
        public String name = "Jane";
        @MaskField(revealTo = {"ADMIN"}) public String ssn = "123-45-6789";
        @MaskField(revealTo = {"ADMIN"}, type = MaskType.LAST_FOUR)
        public String account = "1234567890";
        @MaskField(revealTo = {"ADMIN"}, type = MaskType.EMAIL)
        public String email = "jane@example.com";
    }
    @Test void masksByDefaultAndRevealsOnlyToAuthorizedRole() throws Exception {
        AtomicReference<Set<String>> roles = new AtomicReference<>(Set.of("SUPPORT"));
        ObjectMapper mapper = new ObjectMapper().registerModule(new SecureMaskModule(roles::get));
        Customer customer = new Customer();
        String masked = mapper.writeValueAsString(customer);
        assertTrue(masked.contains("\"ssn\":\"********\""));
        assertTrue(masked.contains("\"account\":\"****7890\""));
        assertTrue(masked.contains("\"email\":\"***@example.com\""));
        assertFalse(masked.contains("123-45-6789"));
        roles.set(Set.of("ADMIN"));
        String visible = mapper.writeValueAsString(customer);
        assertTrue(visible.contains("123-45-6789"));
        assertTrue(visible.contains("1234567890"));
    }
    @Test void nullRolesFailClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new SecureMaskModule(() -> null));
        assertFalse(mapper.writeValueAsString(new Customer()).contains("123-45-6789"));
    }
    @Test void nestedObjectsAreMasked() throws Exception {
        record Envelope(Customer customer) {}
        ObjectMapper mapper = new ObjectMapper().registerModule(new SecureMaskModule(Set::of));
        assertFalse(mapper.writeValueAsString(new Envelope(new Customer())).contains("123-45-6789"));
    }
}
