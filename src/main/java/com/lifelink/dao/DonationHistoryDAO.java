package com.lifelink.dao;

import com.lifelink.model.DonationHistory;
import java.util.List;

public interface DonationHistoryDAO {
    boolean create(DonationHistory history);
    DonationHistory readById(int id);
    boolean update(DonationHistory history);
    boolean delete(int id);
    List<DonationHistory> readAll();
}
