package com.example.boardapp.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.example.boardapp.domain.Member;


// 멤버 테이블을 다루는 매퍼
@Mapper // 매퍼로 만들어라 
public interface MemberMapper { // 스프링이 내부적으로 인터페이스를 구현해줍니다.
  
  // 멤버 생성하는 메서드
  void save(Member member);

  // 사용자 이름으로 멤버 찾기
  Optional<Member> findByUsername(String username);
  // 검색 결과를 Optional 객체로 반환합니다
  // Optional 객체를 쓰는 이유: 널 체크 생략가능
}