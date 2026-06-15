package com.lifelink.dao;

import com.lifelink.model.BloodRequest;
import java.util.List;

public interface RequestDAO {
    boolean create(BloodRequest request);
    BloodRequest readById(int id);
    boolean update(BloodRequest request);
    boolean delete(int id);
    List<BloodRequest> readAll();
}
