package com.salon.repository.admin;

import com.salon.constant.ApplyStatus;
import com.salon.constant.ApplyType;
import com.salon.entity.Member;
import com.salon.entity.admin.Apply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplyRepo extends JpaRepository<Apply, Long> {

    List<Apply> findByStatus(ApplyStatus applyStatus);

    boolean existsByMemberAndApplyType(Member member, ApplyType applyType);

    List<Apply> findByApplyTypeAndStatus(ApplyType applyType, ApplyStatus applyStatus);
}
