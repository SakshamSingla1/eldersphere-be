package com.eldersphere.repositories;

import com.eldersphere.entities.ElderProfileLinkInvite;
import com.eldersphere.enums.LinkInviteStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElderProfileLinkInviteRepository extends JpaRepository<ElderProfileLinkInvite, Long> {

    List<ElderProfileLinkInvite> findByElderProfileIdOrderByCreatedAtDesc(Long elderProfileId);

    List<ElderProfileLinkInvite> findByInvitedUserIdAndStatusOrderByCreatedAtDesc(Long invitedUserId, LinkInviteStatusEnum status);

    boolean existsByElderProfileIdAndInvitedUserIdAndStatus(Long elderProfileId, Long invitedUserId, LinkInviteStatusEnum status);

    long countByInvitedUserIdAndStatus(Long invitedUserId, LinkInviteStatusEnum status);
}
