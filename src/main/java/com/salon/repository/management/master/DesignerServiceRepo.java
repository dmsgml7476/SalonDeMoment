package com.salon.repository.management.master;

import com.salon.entity.management.master.DesignerService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignerServiceRepo extends JpaRepository<DesignerService, Long> {

    // 디자이너 담당 시술찾기
   Optional<DesignerService> findByShopDesignerId(Long designerId);

}
