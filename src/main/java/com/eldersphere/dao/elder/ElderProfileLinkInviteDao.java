package com.eldersphere.dao.elder;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.ElderProfileLinkInvite;
import com.eldersphere.enums.LinkInviteStatusEnum;
import com.eldersphere.repositories.ElderProfileLinkInviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ElderProfileLinkInviteDao implements IDao<ElderProfileLinkInvite, Long> {

    private final ElderProfileLinkInviteRepository elderProfileLinkInviteRepository;

    @Override
    public JpaRepository<ElderProfileLinkInvite, Long> getRepository() {
        return elderProfileLinkInviteRepository;
    }

    public ElderProfileLinkInvite save(ElderProfileLinkInvite invite) {
        return elderProfileLinkInviteRepository.save(invite);
    }

    public List<ElderProfileLinkInvite> findByElderProfileId(Long elderProfileId) {
        return elderProfileLinkInviteRepository.findByElderProfileIdOrderByCreatedAtDesc(elderProfileId);
    }

    public List<ElderProfileLinkInvite> findPendingByInvitedUserId(Long invitedUserId) {
        return elderProfileLinkInviteRepository.findByInvitedUserIdAndStatusOrderByCreatedAtDesc(invitedUserId, LinkInviteStatusEnum.PENDING);
    }

    public boolean existsPendingForProfileAndUser(Long elderProfileId, Long invitedUserId) {
        return elderProfileLinkInviteRepository.existsByElderProfileIdAndInvitedUserIdAndStatus(
                elderProfileId, invitedUserId, LinkInviteStatusEnum.PENDING);
    }

    public long countPendingByInvitedUserId(Long invitedUserId) {
        return elderProfileLinkInviteRepository.countByInvitedUserIdAndStatus(invitedUserId, LinkInviteStatusEnum.PENDING);
    }
}
