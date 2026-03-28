-- Achievement 초기 데이터 (INSERT IGNORE로 중복 방지)

-- 카테고리 1: 첫 경험 (FIRST_STEP)
INSERT IGNORE INTO achievement (code, name, description, category, grade, hidden, created_at, updated_at) VALUES
('F-01', '첫 발걸음', '모든 여정의 시작', 'FIRST_STEP', 1, false, NOW(), NOW()),
('F-02', '기록의 시작', '이름 없는 세션은 없다', 'FIRST_STEP', 1, false, NOW(), NOW()),
('F-03', '생각의 파편', '첫 번째 생각을 기록하다', 'FIRST_STEP', 1, false, NOW(), NOW()),
('F-04', '비주얼 씽커', '글보다 강한 한 장', 'FIRST_STEP', 1, false, NOW(), NOW()),
('F-05', '요약의 기술', '핵심을 꿰뚫다', 'FIRST_STEP', 1, false, NOW(), NOW());

-- 카테고리 2: 단일 세션 몰입 (DEEP_DIVE)
INSERT IGNORE INTO achievement (code, name, description, category, grade, hidden, created_at, updated_at) VALUES
('D-01', '발끝 담그기', '물에 발을 담그다', 'DEEP_DIVE', 1, false, NOW(), NOW()),
('D-02', '무릎까지', '조금 더 깊이', 'DEEP_DIVE', 1, false, NOW(), NOW()),
('D-03', '잔잔한 호수', '고요한 집중의 시작', 'DEEP_DIVE', 2, false, NOW(), NOW()),
('D-04', '깊은 우물', '흐름에 빠지다', 'DEEP_DIVE', 2, false, NOW(), NOW()),
('D-05', '심해의 다이버', '되돌아올 수 없는 깊이', 'DEEP_DIVE', 3, false, NOW(), NOW()),
('D-06', '무아지경', '시간의 감각을 잃다', 'DEEP_DIVE', 4, false, NOW(), NOW()),
('D-07', '시간의 바깥', '현실과 단절된 몰입', 'DEEP_DIVE', 4, false, NOW(), NOW()),
('D-08', '마라토너', '끝이 보이지 않는 길', 'DEEP_DIVE', 5, false, NOW(), NOW());

-- 카테고리 3: 누적 몰입 시간 (GROWTH_RING)
INSERT IGNORE INTO achievement (code, name, description, category, grade, hidden, created_at, updated_at) VALUES
('G-01', '씨앗', '흙 속에서 시작되다', 'GROWTH_RING', 1, false, NOW(), NOW()),
('G-02', '새싹', '땅 위로 고개를 내밀다', 'GROWTH_RING', 1, false, NOW(), NOW()),
('G-03', '줄기', '방향을 잡다', 'GROWTH_RING', 2, false, NOW(), NOW()),
('G-04', '가지', '뻗어나가다', 'GROWTH_RING', 2, false, NOW(), NOW()),
('G-05', '묘목', '뿌리가 단단해지다', 'GROWTH_RING', 3, false, NOW(), NOW()),
('G-06', '고목', '세월을 품다', 'GROWTH_RING', 3, false, NOW(), NOW()),
('G-07', '숲의 주인', '나만의 숲을 이루다', 'GROWTH_RING', 4, false, NOW(), NOW()),
('G-08', '고대의 나무', '전설이 되다', 'GROWTH_RING', 5, false, NOW(), NOW()),
('G-09', '세계수', '모든 것의 근원', 'GROWTH_RING', 5, false, NOW(), NOW());

-- 카테고리 4: 누적 세션 수 (SESSION_COUNT)
INSERT IGNORE INTO achievement (code, name, description, category, grade, hidden, created_at, updated_at) VALUES
('S-01', '입문자', '문을 두드리다', 'SESSION_COUNT', 1, false, NOW(), NOW()),
('S-02', '수련생', '반복의 시작', 'SESSION_COUNT', 1, false, NOW(), NOW()),
('S-03', '숙련자', '몸이 기억하다', 'SESSION_COUNT', 2, false, NOW(), NOW()),
('S-04', '장인', '손끝에 감각이 남다', 'SESSION_COUNT', 2, false, NOW(), NOW()),
('S-05', '달인', '경지에 이르다', 'SESSION_COUNT', 3, false, NOW(), NOW()),
('S-06', '대가', '이름만으로 증명하다', 'SESSION_COUNT', 4, false, NOW(), NOW()),
('S-07', '전설', '기록이 곧 역사', 'SESSION_COUNT', 5, false, NOW(), NOW()),
('S-08', '신화', '시대를 초월하다', 'SESSION_COUNT', 5, false, NOW(), NOW());

-- 카테고리 5: 연속 기록 (STREAK)
INSERT IGNORE INTO achievement (code, name, description, category, grade, hidden, created_at, updated_at) VALUES
('K-01', '이틀째', '우연이 아니다', 'STREAK', 1, false, NOW(), NOW()),
('K-02', '단단한 뿌리', '습관의 씨앗', 'STREAK', 1, false, NOW(), NOW()),
('K-03', '일주일의 파도', '리듬을 타다', 'STREAK', 2, false, NOW(), NOW()),
('K-04', '2주의 관성', '멈출 수 없는 흐름', 'STREAK', 2, false, NOW(), NOW()),
('K-05', '흐름의 지배자', '습관이 본능이 되다', 'STREAK', 3, false, NOW(), NOW()),
('K-06', '한 달의 무게', '매일이 증거', 'STREAK', 3, false, NOW(), NOW()),
('K-07', '살아있는 루틴', '삶의 일부가 되다', 'STREAK', 4, false, NOW(), NOW()),
('K-08', '100일의 증명', '살아있는 전설', 'STREAK', 5, false, NOW(), NOW()),
('K-09', '불멸의 흐름', '1년, 단 하루도 빠짐없이', 'STREAK', 5, false, NOW(), NOW());

-- 카테고리 6: 하루 집중 (DAILY_INTENSITY)
INSERT IGNORE INTO achievement (code, name, description, category, grade, hidden, created_at, updated_at) VALUES
('I-01', '오늘의 시작', '한 번으로 끝내지 않다', 'DAILY_INTENSITY', 1, false, NOW(), NOW()),
('I-02', '삼세번', '세 번은 진심이다', 'DAILY_INTENSITY', 1, false, NOW(), NOW()),
('I-03', '다섯 손가락', '쉬지 않는 하루', 'DAILY_INTENSITY', 2, false, NOW(), NOW()),
('I-04', '풀타임 플로우', '반나절을 몰입에 바치다', 'DAILY_INTENSITY', 3, false, NOW(), NOW()),
('I-05', '하루의 절반', '하루를 온전히', 'DAILY_INTENSITY', 4, false, NOW(), NOW()),
('I-06', '끝없는 하루', '잠을 잊다', 'DAILY_INTENSITY', 5, false, NOW(), NOW());

-- 카테고리 7: 기록의 깊이 (WRITER)
INSERT IGNORE INTO achievement (code, name, description, category, grade, hidden, created_at, updated_at) VALUES
('W-03', '단편 작가', '생각을 풀어놓다', 'WRITER', 2, false, NOW(), NOW()),
('W-04', '에세이스트', '깊은 사색의 흔적', 'WRITER', 3, false, NOW(), NOW()),
('W-05', '사색의 건축가', '생각의 구조를 세우다', 'WRITER', 3, false, NOW(), NOW()),
('W-06', '기록 수집가', '기록이 곧 재산', 'WRITER', 2, false, NOW(), NOW()),
('W-07', '기억의 도서관', '한 권의 책이 되다', 'WRITER', 4, false, NOW(), NOW()),
('W-08', '연대기 저자', '역사를 쓰다', 'WRITER', 5, false, NOW(), NOW());

-- 카테고리 8: 이미지 활용 (VISUAL)
INSERT IGNORE INTO achievement (code, name, description, category, grade, hidden, created_at, updated_at) VALUES
('V-01', '장면 수집가', '순간을 담다', 'VISUAL', 2, false, NOW(), NOW()),
('V-02', '포토 에세이', '사진으로 말하다', 'VISUAL', 2, false, NOW(), NOW()),
('V-03', '갤러리 큐레이터', '나만의 전시회', 'VISUAL', 3, false, NOW(), NOW()),
('V-04', '50장의 기억', '50장의 기억', 'VISUAL', 2, false, NOW(), NOW()),
('V-05', '기록에 색을 입히다', '기록에 색을 입히다', 'VISUAL', 3, false, NOW(), NOW());

-- 카테고리 9: 시간대 (TIME_ZONE)
INSERT IGNORE INTO achievement (code, name, description, category, grade, hidden, created_at, updated_at) VALUES
('T-01', '얼리버드', '새벽을 여는 사람', 'TIME_ZONE', 2, false, NOW(), NOW()),
('T-02', '모닝 루틴', '아침형 인간', 'TIME_ZONE', 2, false, NOW(), NOW()),
('T-03', '새벽의 수호자', '세상이 잠든 시간', 'TIME_ZONE', 2, false, NOW(), NOW()),
('T-04', '밤의 올빼미', '밤이 깊을수록 빛나다', 'TIME_ZONE', 2, false, NOW(), NOW()),
('T-05', '올빼미의 둥지', '밤은 나의 시간', 'TIME_ZONE', 2, false, NOW(), NOW()),
('T-06', '점심의 틈새', '쉬는 시간도 아깝다', 'TIME_ZONE', 2, false, NOW(), NOW()),
('T-07', '퇴근 후 집중', '하루의 마무리는 몰입', 'TIME_ZONE', 2, false, NOW(), NOW());

-- 카테고리 10: 요일/패턴 (PATTERN)
INSERT IGNORE INTO achievement (code, name, description, category, grade, hidden, created_at, updated_at) VALUES
('P-01', '월요일 정복자', '한 주의 시작을 잡다', 'PATTERN', 2, false, NOW(), NOW()),
('P-02', '금요일 피니셔', '끝까지 놓지 않다', 'PATTERN', 2, false, NOW(), NOW()),
('P-03', '주말 전사', '쉬는 날도 성장한다', 'PATTERN', 2, false, NOW(), NOW()),
('P-04', '풀위크 마스터', '빈틈없는 일주일', 'PATTERN', 3, false, NOW(), NOW()),
('P-05', '풀위크 반복', '완벽한 한 달', 'PATTERN', 4, false, NOW(), NOW());

-- 카테고리 11: 서비스 이용 기간 (VETERAN)
INSERT IGNORE INTO achievement (code, name, description, category, grade, hidden, created_at, updated_at) VALUES
('E-01', '1주년', '일주일의 인연', 'VETERAN', 1, false, NOW(), NOW()),
('E-02', '한 달의 동행', '함께한 한 달', 'VETERAN', 2, false, NOW(), NOW()),
('E-03', '분기의 동반자', '세 달의 여정', 'VETERAN', 3, false, NOW(), NOW()),
('E-04', '반년의 기록', '반 년을 함께하다', 'VETERAN', 4, false, NOW(), NOW()),
('E-05', '1년의 증명', '1년간의 성장', 'VETERAN', 5, false, NOW(), NOW());

-- 카테고리 12: 히든 (HIDDEN)
INSERT IGNORE INTO achievement (code, name, description, category, grade, hidden, created_at, updated_at) VALUES
('H-01', '자정의 경계인', '날짜를 넘기는 자', 'HIDDEN', 2, true, NOW(), NOW()),
('H-02', '불사조', '재를 딛고 일어서다', 'HIDDEN', 2, true, NOW(), NOW()),
('H-03', '완벽주의자', '빈틈없는 기록', 'HIDDEN', 3, true, NOW(), NOW()),
('H-04', '첫날의 열정', '시작부터 남다르다', 'HIDDEN', 2, true, NOW(), NOW()),
('H-05', '더블 마라톤', '한 번으론 부족하다', 'HIDDEN', 3, true, NOW(), NOW()),
('H-06', '고요한 새벽', '가장 깊은 시간', 'HIDDEN', 3, true, NOW(), NOW()),
('H-07', '10의 법칙', '숫자 10의 의미', 'HIDDEN', 2, true, NOW(), NOW()),
('H-08', '기록 폭주', '멈출 수 없는 펜', 'HIDDEN', 3, true, NOW(), NOW());
