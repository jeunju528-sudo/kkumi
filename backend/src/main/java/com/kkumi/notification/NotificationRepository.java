package com.kkumi.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 그날 보낸 알림 키 (서버 시작 시 받은 목록 로드용)
    @Query("select new com.kkumi.notification.AlertKey(n.member.id, n.stock.code, n.alertDate) "
            + "from Notification n where n.alertDate = :alertDate")
    List<AlertKey> findAlertKeysByAlertDate(@Param("alertDate") LocalDate alertDate);

    // 안 읽은 알림 수 (종 배지, 토스트 푸시)
    long countByMemberIdAndReadAtIsNull(Long memberId);

    // 내 알림 목록, 최신순 (종목명 표시용 join fetch로 N+1 방지)
    @Query("""
            select n from Notification n join fetch n.stock
            where n.member.id = :memberId
            order by n.createdAt desc, n.id desc
            """)
    List<Notification> findAllByMemberIdWithStock(@Param("memberId") Long memberId);

    // 내 알림 1건 (남의 알림이면 빈 값)
    Optional<Notification> findByIdAndMemberId(Long id, Long memberId);

    // 안 읽은 알림 모두 읽음 (엔티티 로딩 없이 update 1번), 변경 행 수 반환
    // flushAutomatically: 앞서 바꾼 값 먼저 DB 반영, clearAutomatically: 영속성 컨텍스트의 옛 값(readAt null) 비움
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Notification n set n.readAt = :now where n.member.id = :memberId and n.readAt is null")
    int markAllAsRead(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);

    // before 이전 날짜 알림 일괄 삭제, 삭제 행 수 반환
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Notification n where n.alertDate < :before")
    int deleteAlertedBefore(@Param("before") LocalDate before);
}
