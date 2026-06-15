import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.lifelink.database.DBConnection;
import com.lifelink.model.User;
import com.lifelink.dao.UserDAO;
import com.lifelink.daoimpl.UserDAOImpl;
import com.lifelink.service.AuthService;

import java.sql.Connection;
import java.sql.Statement;

public class UserTest {
    private static UserDAO userDAO;
    private static AuthService authService;

    @BeforeAll
    public static void setUpSuite() {
        DBConnection.testConnection();
        userDAO = new UserDAOImpl();
        authService = new AuthService();
    }

    @BeforeEach
    public void cleanUp() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users WHERE username != 'admin'");
        }
    }

    @Test
    public void testUserCreationAndRetrieval() {
        User user = new User("testuser", "hashedpassword", "test@test.com", "DONOR");
        boolean success = userDAO.create(user);
        
        assertTrue(success);
        assertTrue(user.getId() > 0);

        User retrieved = userDAO.readByUsername("testuser");
        assertNotNull(retrieved);
        assertEquals("testuser", retrieved.getUsername());
        assertEquals("test@test.com", retrieved.getEmail());
    }

    @Test
    public void testAuthServiceRegistrationAndLogin() {
        User registered = authService.registerDonor("donor123", "secret123", "donor1@test.com", "John Donor", 25, 70.0, "O+", "Chicago");
        assertNotNull(registered);

        // Test login success
        User loggedIn = authService.login("donor123", "secret123");
        assertNotNull(loggedIn);
        assertEquals("donor123", loggedIn.getUsername());

        // Test login failure
        User badLogin = authService.login("donor123", "wrongpass");
        assertNull(badLogin);
    }
}
