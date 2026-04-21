package com.deepflow.domain.crew;

import com.deepflow.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "crew_member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_crew_member_crew_user", columnNames = {"crew_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_crew_member_user_id", columnList = "user_id"),
                @Index(name = "idx_crew_member_crew_id", columnList = "crew_id")
        }
)
public class CrewMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crew_id", nullable = false)
    private Long crewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CrewRole role;

    public static CrewMember newOwner(Long crewId, Long userId) {
        return CrewMember.builder()
                .crewId(crewId)
                .userId(userId)
                .role(CrewRole.OWNER)
                .build();
    }

    public static CrewMember newMember(Long crewId, Long userId) {
        return CrewMember.builder()
                .crewId(crewId)
                .userId(userId)
                .role(CrewRole.MEMBER)
                .build();
    }

    public boolean isOwner() {
        return role == CrewRole.OWNER;
    }
}
