-- schema.sql: 서버가 시작될 때 실행할 sql문을 작성하는 곳

-- 멤버 테이블 생성하기
CREATE TABLE IF NOT EXISTS member ( -- if not exists(member 테이블이 없으면): 테스트용
  id BIGINT AUTO_INCREMENT PRIMARY KEY, -- bigint: 일반 정수형(int)보다 더 큰 자료형
  username VARCHAR(50) NOT NULL UNIQUE, -- UNIQUE: 중복되면 안됨
  password VARCHAR(100) NOT NULL
);

-- 게시물 테이블 생성하기
CREATE TABLE IF NOT EXISTS post (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(200) NOT NULL, -- 게시물 제목
  content TEXT NOT NULL, -- 게시물 내용
  member_id BIGINT NOT NULL, -- 외래키 (게시물과 사용자를 연결합니다)
  FOREIGN KEY (member_id) REFERENCES member(id) -- member테이블의 기본키를 참조합니다 
);