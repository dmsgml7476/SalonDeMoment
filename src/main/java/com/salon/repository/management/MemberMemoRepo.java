package com.salon.repository.management;

import com.salon.entity.management.MemberMemo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberMemoRepo extends JpaRepository<MemberMemo, Long> {

    // 디자이너의 회원 개인메모 가져오기
    Optional<MemberMemo> findByMemberIdAndShopDesignerId(Long memberId, Long shopDesignerId);

}
