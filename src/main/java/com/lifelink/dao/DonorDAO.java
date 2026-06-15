package com.lifelink.dao;

import com.lifelink.model.Donor;
import java.util.List;

public interface DonorDAO {
    boolean create(Donor donor);
    Donor readById(int id);
    Donor readByUserId(int userId);
    boolean update(Donor donor);
    boolean delete(int id);
    List<Donor> readAll();
}
