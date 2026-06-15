import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.lifelink.database.DBConnection;
import com.lifelink.model.*;
import com.lifelink.dao.*;
import com.lifelink.daoimpl.*;
import com.lifelink.service.AvailabilityService;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

public class RequestTest {
    private static UserDAO userDAO;
    private static HospitalDAO hospitalDAO;
    private static DonorDAO donorDAO;
    private static RequestDAO requestDAO;
    private static DonationHistoryDAO donationHistoryDAO;
    private static AvailabilityService availabilityService;

    @BeforeAll
    public static void setUpSuite() {
        DBConnection.testConnection();
        userDAO = new UserDAOImpl();
        hospitalDAO = new HospitalDAOImpl();
        donorDAO = new DonorDAOImpl();
        requestDAO = new RequestDAOImpl();
        donationHistoryDAO = new DonationHistoryDAOImpl();
        availabilityService = new AvailabilityService();
    }

    @BeforeEach
    public void cleanUp() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM blood_requests");
            stmt.execute("DELETE FROM donation_history");
            stmt.execute("DELETE FROM hospitals");
            stmt.execute("DELETE FROM donors");
            stmt.execute("DELETE FROM users WHERE username != 'admin'");
        }
    }

    @Test
    public void testBloodRequestCreationAndStatusChange() {
        User user = new User("hospitaluser", "pass123", "hosp@test.com", "HOSPITAL");
        userDAO.create(user);

        Hospital hosp = new Hospital("City General", "123 Health Ave", "555-0199", "hosp@test.com");
        hospitalDAO.create(hosp);

        BloodRequest req = new BloodRequest(null, hosp.getId(), "O+", 5, "Normal", LocalDate.now(), "PENDING");
        boolean success = requestDAO.create(req);

        assertTrue(success);
        assertTrue(req.getId() > 0);

        req.setStatus("APPROVED");
        boolean updateSuccess = requestDAO.update(req);
        assertTrue(updateSuccess);

        BloodRequest updated = requestDAO.readById(req.getId());
        assertEquals("APPROVED", updated.getStatus());
    }

    @Test
    public void testAvailabilityStockAggregation() {
        // 1. Create a Donor
        User u1 = new User("donor1", "pass123", "d1@test.com", "DONOR");
        userDAO.create(u1);
        Donor d1 = new Donor(u1.getId(), "John", 25, 65.0, "O+", "Chicago", null, true);
        donorDAO.create(d1);

        // 2. Add two donations of O+ (10 units total)
        DonationHistory don1 = new DonationHistory(d1.getId(), LocalDate.now().minusDays(10), 6, "Location A");
        DonationHistory don2 = new DonationHistory(d1.getId(), LocalDate.now().minusDays(5), 4, "Location B");
        donationHistoryDAO.create(don1);
        donationHistoryDAO.create(don2);

        // Check stock (should be 10)
        assertEquals(10, availabilityService.getBloodStock("O+"));

        // 3. Create a Hospital
        User u2 = new User("hospital1", "pass123", "h1@test.com", "HOSPITAL");
        userDAO.create(u2);
        Hospital hosp = new Hospital("Health Care", "City Rd", "555-0188", "h1@test.com");
        hospitalDAO.create(hosp);

        // 4. Create an APPROVED blood request of O+ (3 units)
        BloodRequest req1 = new BloodRequest(null, hosp.getId(), "O+", 3, "Normal", LocalDate.now(), "APPROVED");
        requestDAO.create(req1);

        // Check stock (should be 10 - 3 = 7)
        assertEquals(7, availabilityService.getBloodStock("O+"));

        // 5. Create a PENDING blood request of O+ (2 units) - should NOT affect stock count
        BloodRequest req2 = new BloodRequest(null, hosp.getId(), "O+", 2, "Normal", LocalDate.now(), "PENDING");
        requestDAO.create(req2);

        assertEquals(7, availabilityService.getBloodStock("O+"));

        // 6. Complete the pending request (Approved)
        req2.setStatus("APPROVED");
        requestDAO.update(req2);
        assertEquals(5, availabilityService.getBloodStock("O+"));
    }
}
