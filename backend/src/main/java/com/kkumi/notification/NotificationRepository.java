package com.kkumi.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 그날 보낸 알림 키 (서버 시작 시 받은 목록 로드용)
    @Query("select new com.kkumi.notification.AlertKey(n.member.id, n.stock.code, n.alertDate) "
            + "from Notification n where n.alertDate = :alertDate")
    List<AlertKey> findAlertKeysByAlertDate(@Param("alertDate") LocalDate alertDate);
}
