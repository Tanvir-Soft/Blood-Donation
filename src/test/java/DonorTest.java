import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.lifelink.database.DBConnection;
import com.lifelink.model.Donor;
import com.lifelink.model.User;
import com.lifelink.dao.DonorDAO;
import com.lifelink.dao.UserDAO;
import com.lifelink.daoimpl.DonorDAOImpl;
import com.lifelink.daoimpl.UserDAOImpl;
import com.lifelink.service.EligibilityService;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

public class DonorTest {
    private static UserDAO userDAO;
    private static DonorDAO donorDAO;
    private static EligibilityService eligibilityService;

    @BeforeAll
    public static void setUpSuite() {
        DBConnection.testConnection();
        userDAO = new UserDAOImpl();
        donorDAO = new DonorDAOImpl();
        eligibilityService = new EligibilityService();
    }

    @BeforeEach
    public void cleanUp() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM donors");
            stmt.execute("DELETE FROM users WHERE username != 'admin'");
        }
    }

    @Test
    public void testDonorCreationAndProfileUpdate() {
        User user = new User("donoruser", "pass123", "donor@test.com", "DONOR");
        userDAO.create(user);

        Donor donor = new Donor(user.getId(), "Alice", 30, 55.0, "A+", "New York", null, true);
        boolean success = donorDAO.create(donor);

        assertTrue(success);
        assertTrue(donor.getId() > 0);

        donor.setName("Alice Cooper");
        donor.setAge(31);
        boolean updateSuccess = donorDAO.update(donor);
        assertTrue(updateSuccess);

        Donor updated = donorDAO.readById(donor.getId());
        assertEquals("Alice Cooper", updated.getName());
        assertEquals(31, updated.getAge());
    }

    @Test
    public void testEligibilityRules() {
        // Test age limits
        Donor youngDonor = new Donor(1, "Young", 17, 60.0, "B+", "New York", null, false);
        assertFalse(eligibilityService.isEligible(youngDonor));

        Donor oldDonor = new Donor(2, "Old", 66, 60.0, "B+", "New York", null, false);
        assertFalse(eligibilityService.isEligible(oldDonor));

        Donor goodAgeDonor = new Donor(3, "Adult", 25, 60.0, "B+", "New York", null, true);
        assertTrue(eligibilityService.isEligible(goodAgeDonor));

        // Test donation date gaps (90 days limit)
        goodAgeDonor.setLastDonationDate(LocalDate.now().minusDays(30));
        assertFalse(eligibilityService.isEligible(goodAgeDonor));

        goodAgeDonor.setLastDonationDate(LocalDate.now().minusDays(95));
        assertTrue(eligibilityService.isEligible(goodAgeDonor));
    }
}
