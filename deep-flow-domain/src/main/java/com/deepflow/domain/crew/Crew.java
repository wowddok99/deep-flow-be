package com.deepflow.domain.crew;

import com.deepflow.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "crew",
        indexes = {
                @Index(name = "idx_crew_invite_code", columnList = "invite_code"),
                @Index(name = "idx_crew_owner_id", columnList = "owner_user_id"),
                @Index(name = "idx_crew_visibility_name", columnList = "visibility, name"),
                @Index(name = "idx_crew_deleted_at", columnList = "deleted_at")
        }
)
public class Crew extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility;

    @Column(name = "max_members")
    private Integer maxMembers;

    @Column(name = "invite_code", length = 8, unique = true)
    private String inviteCode;

    @Column(name = "invite_code_expires_at")
    private LocalDateTime inviteCodeExpiresAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Crew create(String name, String description, Long ownerUserId,
                              Visibility visibility, Integer maxMembers) {
        return Crew.builder()
                .name(name)
                .description(description)
                .ownerUserId(ownerUserId)
                .visibility(visibility)
                .maxMembers(maxMembers)
                .build();
    }

    public void updateInfo(String name, String description, Integer maxMembers, Visibility visibility) {
        if (name != null) this.name = name;
        this.description = description;
        if (visibility != null) this.visibility = visibility;
        this.maxMembers = maxMembers;
    }

    public void issueInviteCode(String code, LocalDateTime expiresAt) {
        this.inviteCode = code;
        this.inviteCodeExpiresAt = expiresAt;
    }

    public void clearInviteCode() {
        this.inviteCode = null;
        this.inviteCodeExpiresAt = null;
    }

    public void softDelete() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("이미 삭제된 크루입니다.");
        }
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isInviteCodeValid(LocalDateTime now) {
        return inviteCode != null
                && inviteCodeExpiresAt != null
                && inviteCodeExpiresAt.isAfter(now);
    }

    public boolean isOwner(Long userId) {
        return ownerUserId.equals(userId);
    }
}
