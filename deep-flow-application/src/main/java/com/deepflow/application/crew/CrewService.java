package com.deepflow.application.crew;

import com.deepflow.application.common.SliceResult;
import com.deepflow.application.crew.dto.*;
import com.deepflow.application.exception.ResourceNotFoundException;
import com.deepflow.application.exception.crew.*;
import com.deepflow.application.lock.DistributedLock;
import com.deepflow.application.port.out.persistence.*;
import com.deepflow.domain.crew.Crew;
import com.deepflow.domain.crew.CrewMember;
import com.deepflow.domain.crew.CrewRole;
import com.deepflow.domain.crew.Visibility;
import com.deepflow.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrewService {

    private static final int MIN_MAX_MEMBERS = 2;
    private static final int HARD_LIMIT_MAX_MEMBERS = 500;
    private static final int MAX_CODE_RETRY = 5;
    private static final Set<Integer> VALID_TTL_MINUTES = Set.of(5, 30, 60, 1440);

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final StatsRepository statsRepository;
    private final InviteCodeGenerator inviteCodeGenerator;
    private final CrewJoinLocker crewJoinLocker;

    // ---------- 생성 ----------

    @Transactional
    public CrewSummaryInfo create(Long userId, CrewCreateCommand cmd) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String name = normalizeName(cmd.name());
        String description = normalizeDescription(cmd.description());
        Visibility visibility = cmd.visibility() != null ? cmd.visibility() : Visibility.PRIVATE;
        Integer maxMembers = normalizeMaxMembers(cmd.maxMembers());

        Crew crew = Crew.create(name, description, userId, visibility, maxMembers);
        Crew saved = crewRepository.save(crew);

        crewMemberRepository.save(CrewMember.newOwner(saved.getId(), userId));

        log.info("크루 생성: crewId={}, ownerUserId={}", saved.getId(), userId);

        return CrewSummaryInfo.of(saved, 1L, 0, CrewRole.OWNER);
    }

    // ---------- 조회 ----------

    public List<CrewSummaryInfo> listMyCrews(Long userId) {
        List<CrewMember> myMemberships = crewMemberRepository.findAllByUserId(userId);
        if (myMemberships.isEmpty()) return List.of();

        List<Long> crewIds = myMemberships.stream().map(CrewMember::getCrewId).toList();
        List<Crew> crews = crewRepository.findAllByIds(crewIds);

        Map<Long, CrewRole> roleByCrew = myMemberships.stream()
                .collect(Collectors.toMap(CrewMember::getCrewId, CrewMember::getRole));

        // 모든 멤버 한 번에 로드 → crewId 별로 userId 묶기
        List<CrewMember> allMembers = crewMemberRepository.findAllByCrewIds(crewIds);
        Map<Long, List<Long>> userIdsByCrew = allMembers.stream()
                .collect(Collectors.groupingBy(
                        CrewMember::getCrewId,
                        Collectors.mapping(CrewMember::getUserId, Collectors.toList())));

        // 모든 ONGOING user 한 번에 로드 → Set 캐싱
        List<Long> distinctMemberIds = allMembers.stream()
                .map(CrewMember::getUserId).distinct().toList();
        Set<Long> ongoingSet = new HashSet<>(
                sessionRepository.findOngoingUserIdsByUserIds(distinctMemberIds));

        return crews.stream()
                .sorted(Comparator.comparing(Crew::getId).reversed())
                .map(c -> {
                    List<Long> members = userIdsByCrew.getOrDefault(c.getId(), List.of());
                    long memberCount = members.size();
                    int activeNow = (int) members.stream().filter(ongoingSet::contains).count();
                    return CrewSummaryInfo.of(c, memberCount, activeNow, roleByCrew.get(c.getId()));
                })
                .toList();
    }

    public CrewDetailInfo getDetail(Long userId, Long crewId) {
        Crew crew = getCrew(crewId);

        CrewMember myMembership = crewMemberRepository.findByCrewIdAndUserId(crewId, userId)
                .orElseThrow(NotCrewMemberException::new);

        List<CrewMember> members = crewMemberRepository.findAllByCrewId(crewId);
        List<Long> memberUserIds = members.stream().map(CrewMember::getUserId).toList();

        Map<Long, String> nameByUserId = loadUserNames(memberUserIds);
        Set<Long> activeUserIds = new HashSet<>(sessionRepository.findOngoingUserIdsByUserIds(memberUserIds));

        List<CrewMemberInfo> memberInfos = members.stream()
                .map(cm -> new CrewMemberInfo(
                        cm.getUserId(),
                        nameByUserId.getOrDefault(cm.getUserId(), "알수없음"),
                        cm.getRole(),
                        cm.getCreatedAt(),
                        activeUserIds.contains(cm.getUserId())
                ))
                .sorted(Comparator.comparing(CrewMemberInfo::role).thenComparing(CrewMemberInfo::joinedAt))
                .toList();

        LocalDateTime now = LocalDateTime.now();
        boolean inviteValid = crew.isInviteCodeValid(now);

        return new CrewDetailInfo(
                crew.getId(),
                crew.getName(),
                crew.getDescription(),
                crew.getVisibility(),
                crew.getMaxMembers(),
                members.size(),
                activeUserIds.size(),
                myMembership.getRole(),
                inviteValid ? crew.getInviteCode() : null,
                inviteValid ? crew.getInviteCodeExpiresAt() : null,
                crew.getCreatedAt(),
                memberInfos
        );
    }

    public SliceResult<CrewSummaryInfo> searchPublic(Long userId, String q, Long cursorId, int size) {
        String keyword = q == null ? "" : q.trim();
        SliceResult<Crew> result = crewRepository.searchPublic(keyword, cursorId, size);

        if (result.content().isEmpty()) {
            return new SliceResult<>(List.of(), result.nextCursorId(), result.hasNext());
        }

        List<Long> resultCrewIds = result.content().stream().map(Crew::getId).toList();

        // 검색 결과 크루의 모든 멤버 한 번에 로드
        List<CrewMember> allMembers = crewMemberRepository.findAllByCrewIds(resultCrewIds);
        Map<Long, List<Long>> userIdsByCrew = allMembers.stream()
                .collect(Collectors.groupingBy(
                        CrewMember::getCrewId,
                        Collectors.mapping(CrewMember::getUserId, Collectors.toList())));

        // 모든 ONGOING user 한 번에 로드
        List<Long> distinctMemberIds = allMembers.stream()
                .map(CrewMember::getUserId).distinct().toList();
        Set<Long> ongoingSet = new HashSet<>(
                sessionRepository.findOngoingUserIdsByUserIds(distinctMemberIds));

        // 내 역할 매핑
        Map<Long, CrewRole> myRoleByCrew = allMembers.stream()
                .filter(cm -> cm.getUserId().equals(userId))
                .collect(Collectors.toMap(CrewMember::getCrewId, CrewMember::getRole));

        List<CrewSummaryInfo> content = result.content().stream()
                .map(c -> {
                    List<Long> members = userIdsByCrew.getOrDefault(c.getId(), List.of());
                    long memberCount = members.size();
                    int activeNow = (int) members.stream().filter(ongoingSet::contains).count();
                    return CrewSummaryInfo.of(c, memberCount, activeNow, myRoleByCrew.get(c.getId()));
                })
                .toList();

        return new SliceResult<>(content, result.nextCursorId(), result.hasNext());
    }

    // ---------- 수정 ----------

    @Transactional
    public CrewSummaryInfo update(Long userId, Long crewId, CrewUpdateCommand cmd) {
        Crew crew = getCrew(crewId);
        if (!crew.isOwner(userId)) throw new CrewAccessDeniedException();

        // PUT 시맨틱: name/visibility 는 필수, description/maxMembers 는 null = 명시적 비움/무제한.
        String name = normalizeName(cmd.name());
        String description = normalizeDescription(cmd.description());
        Visibility visibility = cmd.visibility() != null ? cmd.visibility() : crew.getVisibility();
        Integer newMaxMembers = normalizeMaxMembers(cmd.maxMembers());

        long currentCount = crewMemberRepository.countByCrewId(crewId);
        if (newMaxMembers != null && currentCount > newMaxMembers) {
            throw new CrewMaxMembersBelowCurrentException();
        }

        crew.updateInfo(name, description, newMaxMembers, visibility);
        Crew saved = crewRepository.save(crew);

        int activeNow = countActiveNowForCrew(crewId);
        return CrewSummaryInfo.of(saved, currentCount, activeNow, CrewRole.OWNER);
    }

    // ---------- 해체 ----------

    @Transactional
    public void disband(Long userId, Long crewId) {
        Crew crew = getCrew(crewId);
        if (!crew.isOwner(userId)) throw new CrewAccessDeniedException();

        crewMemberRepository.deleteAllByCrewId(crewId);
        crew.softDelete();
        crewRepository.save(crew);

        log.info("크루 해체: crewId={}, ownerUserId={}", crewId, userId);
    }

    // ---------- 초대 코드 ----------

    @Transactional
    @DistributedLock(key = "'crew_invite:' + #crewId")
    public InviteCodeIssuedInfo issueInviteCode(Long userId, Long crewId, int ttlMinutes) {
        if (!VALID_TTL_MINUTES.contains(ttlMinutes)) throw new InvalidInviteTtlException();

        Crew crew = getCrew(crewId);
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new NotCrewMemberException();
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(ttlMinutes);
        String code = null;

        for (int i = 0; i < MAX_CODE_RETRY; i++) {
            String candidate = inviteCodeGenerator.generate();
            crew.issueInviteCode(candidate, expiresAt);
            try {
                crewRepository.save(crew);
                code = candidate;
                break;
            } catch (DataIntegrityViolationException e) {
                log.warn("초대 코드 충돌 재시도: attempt={}", i + 1);
            }
        }

        if (code == null) {
            throw new IllegalStateException("초대 코드 생성 실패 (충돌 반복)");
        }

        log.info("초대 코드 발급: crewId={}, ttlMinutes={}", crewId, ttlMinutes);
        return new InviteCodeIssuedInfo(code, expiresAt);
    }

    // ---------- 가입 ----------
    //
    // joinByCode / joinPublic 은 외부 진입점이며 @Transactional 을 두지 않는다.
    // CrewJoinLocker.join 이 @DistributedLock 을 통해 락 안에서 REQUIRES_NEW TX 를 시작·커밋한 뒤
    // 락을 해제하기 때문에, 여기서 외부 TX 를 열면 락 순서가 깨진다.

    public CrewSummaryInfo joinByCode(Long userId, String code) {
        if (code == null || code.isBlank()) throw new InvalidInviteCodeException();

        Crew crew = crewRepository.findByInviteCode(code.trim().toUpperCase())
                .filter(c -> c.isInviteCodeValid(LocalDateTime.now()))
                .orElseThrow(InvalidInviteCodeException::new);

        return crewJoinLocker.join(userId, crew.getId(), false);
    }

    public CrewSummaryInfo joinPublic(Long userId, Long crewId) {
        return crewJoinLocker.join(userId, crewId, true);
    }

    /**
     * CrewJoinLocker 가 분산 락을 획득한 뒤에만 호출되는 가입 본문.
     * 외부에서 직접 호출하지 말 것 (락 우회 위험).
     */
    @Transactional
    public CrewSummaryInfo joinCrewLockedInternal(Long userId, Long crewId, boolean requirePublic) {
        Crew crew = getCrew(crewId);

        if (requirePublic && crew.getVisibility() != Visibility.PUBLIC) {
            throw new CrewNotPublicException();
        }

        if (crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new AlreadyCrewMemberException();
        }

        long currentCount = crewMemberRepository.countByCrewId(crewId);
        int effectiveLimit = crew.getMaxMembers() != null
                ? crew.getMaxMembers()
                : HARD_LIMIT_MAX_MEMBERS;
        if (currentCount >= effectiveLimit) {
            throw new CrewMemberLimitExceededException();
        }

        crewMemberRepository.save(CrewMember.newMember(crewId, userId));
        long newCount = currentCount + 1;

        int activeNow = countActiveNowForCrew(crewId);
        log.info("크루 가입: crewId={}, userId={}", crewId, userId);

        return CrewSummaryInfo.of(crew, newCount, activeNow, CrewRole.MEMBER);
    }

    // ---------- 탈퇴 ----------

    @Transactional
    public void leave(Long userId, Long crewId) {
        CrewMember membership = crewMemberRepository.findByCrewIdAndUserId(crewId, userId)
                .orElseThrow(NotCrewMemberException::new);

        if (membership.isOwner()) throw new CrewOwnerCannotLeaveException();

        crewMemberRepository.delete(membership);
        log.info("크루 탈퇴: crewId={}, userId={}", crewId, userId);
    }

    @Transactional
    public void kick(Long ownerUserId, Long crewId, Long targetUserId) {
        Crew crew = getCrew(crewId);
        if (!crew.isOwner(ownerUserId)) throw new CrewAccessDeniedException();
        if (Objects.equals(ownerUserId, targetUserId)) {
            throw new CrewAccessDeniedException();
        }

        CrewMember target = crewMemberRepository.findByCrewIdAndUserId(crewId, targetUserId)
                .orElseThrow(NotCrewMemberException::new);

        crewMemberRepository.delete(target);
        log.info("크루 추방: crewId={}, targetUserId={}", crewId, targetUserId);
    }

    // ---------- 활동 지표 ----------

    public CrewActivityInfo getActivity(Long userId, Long crewId) {
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new NotCrewMemberException();
        }

        List<CrewMember> members = crewMemberRepository.findAllByCrewId(crewId);
        List<Long> memberIds = members.stream().map(CrewMember::getUserId).toList();

        Set<Long> activeSet = new HashSet<>(sessionRepository.findOngoingUserIdsByUserIds(memberIds));
        LocalDate today = LocalDate.now();
        Set<Long> todaySet = new HashSet<>(statsRepository.findUserIdsWithActivityOnDate(memberIds, today));
        todaySet.addAll(activeSet);

        long todayTotal = statsRepository.sumDurationByUserIdsOnDate(memberIds, today);

        List<CrewActivityInfo.WeeklyTrendPoint> weekly = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            long duration = statsRepository.sumDurationByUserIdsOnDate(memberIds, d);
            weekly.add(new CrewActivityInfo.WeeklyTrendPoint(d.toString(), duration));
        }

        List<Object[]> rankingRows = statsRepository.findMemberRankingByUserIdsOnDate(memberIds, today, 5);
        Map<Long, String> namesById = loadUserNames(memberIds);
        List<CrewActivityInfo.MemberRankingEntry> ranking = rankingRows.stream()
                .map(row -> new CrewActivityInfo.MemberRankingEntry(
                        (Long) row[0],
                        namesById.getOrDefault((Long) row[0], "알수없음"),
                        ((Number) row[1]).longValue()
                ))
                .toList();

        return new CrewActivityInfo(activeSet.size(), todaySet.size(), todayTotal, weekly, ranking);
    }

    // ---------- 내부 유틸 ----------

    private Crew getCrew(Long crewId) {
        return crewRepository.findById(crewId).orElseThrow(CrewNotFoundException::new);
    }

    private CrewRole lookupRole(Long crewId, Long userId) {
        return crewMemberRepository.findByCrewIdAndUserId(crewId, userId)
                .map(CrewMember::getRole)
                .orElse(null);
    }

    private int countActiveNowForCrew(Long crewId) {
        List<Long> memberIds = crewMemberRepository.findAllByCrewId(crewId).stream()
                .map(CrewMember::getUserId).toList();
        if (memberIds.isEmpty()) return 0;
        return sessionRepository.findOngoingUserIdsByUserIds(memberIds).size();
    }

    private Map<Long, String> loadUserNames(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        User::getName,
                        (a, b) -> a));   // 중복 키 발생 시 IllegalStateException 방지
    }

    private String normalizeName(String name) {
        if (name == null) throw new IllegalArgumentException("name is required");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("name must not be blank");
        if (trimmed.length() > 30) throw new IllegalArgumentException("name must be <= 30 chars");
        return trimmed;
    }

    private String normalizeDescription(String description) {
        if (description == null) return null;
        String trimmed = description.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.length() > 200) throw new IllegalArgumentException("description must be <= 200 chars");
        return trimmed;
    }

    /**
     * null = 무제한 (create / update 공통). 0 sentinel 같은 마법값을 두지 않는다.
     * 양수면 2 ~ 500 범위 검증.
     */
    private Integer normalizeMaxMembers(Integer maxMembers) {
        if (maxMembers == null) return null;
        if (maxMembers < MIN_MAX_MEMBERS || maxMembers > HARD_LIMIT_MAX_MEMBERS) {
            throw new IllegalArgumentException(
                    "maxMembers must be between " + MIN_MAX_MEMBERS + " and " + HARD_LIMIT_MAX_MEMBERS);
        }
        return maxMembers;
    }
}
