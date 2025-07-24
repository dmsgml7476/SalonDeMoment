package com.salon.repository.admin;

import com.salon.entity.admin.CsFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsCustomerFileRepo extends JpaRepository<CsFile, Long> {
}
