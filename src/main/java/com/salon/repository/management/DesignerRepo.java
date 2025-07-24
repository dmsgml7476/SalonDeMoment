package com.salon.repository.management;

import com.salon.entity.management.Designer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignerRepo extends JpaRepository<Designer, Long> {

    // MemberId 로 디자이너 가져오기
    Designer findByMember_Id(Long memberId);

    // MemberId 로 디자이너 가져오기 222 옵셔널을 씀.
    Optional<Designer> findByMemberId(Long id);
  
    // 디자이너 이름 검색 결과
    List<Designer> findByMember_NameContainingIgnoreCase(String name);

    // 디자이너 전화번호 검색 결과
    List<Designer> findByMember_Tel(String tel);
  
}
