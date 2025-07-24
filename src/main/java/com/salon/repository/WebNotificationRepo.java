package com.salon.repository;


import com.salon.constant.WebTarget;
import com.salon.entity.admin.WebNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface WebNotificationRepo extends JpaRepository<WebNotification, Long> {
    Optional<WebNotification> findTopByWebTargetAndTargetIdOrderByCreateAtDesc(WebTarget webTarget, Long targetId);

    List<WebNotification> findAllByMemberIdAndIsReadFalse(Long memberId);

    // 해당 리뷰에 달린 댓글을 회원이 확인했는지 안했는지
    Optional<WebNotification> findTopByWebTargetAndTargetIdAndMemberId(WebTarget webTarget, Long id, Long memberId);

    // 미읽음 알림 개수
    long countByMemberIdAndIsReadFalse(Long memberId);

    @Query("""
       SELECT w FROM WebNotification w
       WHERE w.memberId = :memberId
         AND w.isRead   = false
       ORDER BY w.createAt DESC
       """)
    List<WebNotification> findTopNUnread(@Param("memberId") Long memberId,
                                         Pageable pageable);

    List<WebNotification> findTop3ByMemberIdAndIsReadFalseOrderByCreateAtDesc(Long memberId);
}
