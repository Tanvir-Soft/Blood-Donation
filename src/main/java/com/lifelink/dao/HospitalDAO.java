package com.lifelink.dao;

import com.lifelink.model.Hospital;
import java.util.List;

public interface HospitalDAO {
    boolean create(Hospital hospital);
    Hospital readById(int id);
    boolean update(Hospital hospital);
    boolean delete(int id);
    List<Hospital> readAll();
}
