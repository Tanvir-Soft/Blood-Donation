import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.lifelink.database.DBConnection;
import com.lifelink.model.Hospital;
import com.lifelink.model.User;
import com.lifelink.dao.HospitalDAO;
import com.lifelink.dao.UserDAO;
import com.lifelink.daoimpl.HospitalDAOImpl;
import com.lifelink.daoimpl.UserDAOImpl;

import java.sql.Connection;
import java.sql.Statement;

public class HospitalTest {
    private static UserDAO userDAO;
    private static HospitalDAO hospitalDAO;

    @BeforeAll
    public static void setUpSuite() {
        DBConnection.testConnection();
        userDAO = new UserDAOImpl();
        hospitalDAO = new HospitalDAOImpl();
    }

    @BeforeEach
    public void cleanUp() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM hospitals");
            stmt.execute("DELETE FROM users WHERE username != 'admin'");
        }
    }

    @Test
    public void testHospitalCreationAndUpdates() {
        User user = new User("hospitaluser", "pass123", "hosp@test.com", "HOSPITAL");
        userDAO.create(user);

        Hospital hosp = new Hospital("City General", "123 Health Ave", "555-0199", "hosp@test.com");
        boolean success = hospitalDAO.create(hosp);

        assertTrue(success);
        assertTrue(hosp.getId() > 0);

        hosp.setName("City General Hospital");
        hosp.setContactNo("555-0200");
        boolean updateSuccess = hospitalDAO.update(hosp);
        assertTrue(updateSuccess);

        Hospital updated = hospitalDAO.readById(hosp.getId());
        assertEquals("City General Hospital", updated.getName());
        assertEquals("555-0200", updated.getContactNo());
    }
}
