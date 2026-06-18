package com.example.boardapp.config;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.boardapp.domain.Member;
import com.example.boardapp.mapper.MemberMapper;

// 데이터베이스로부터 인증을 요청한 사용자(로그인을 요청한 사용자)의 정보를 가져오는 서비스
@Service // 서비스 빈으로 등록합니다. 컴포넌트 스캔 방식: 빈으로 등록하고자 하는 클래스에 어노테이션을 붙입니다
public class CustomUserDetailsService implements UserDetailsService {
  
  // 멤버 매퍼객체에 의존합니다. 데이터베이스와 통신하는 부분이기 때문에
  private final MemberMapper memberMapper;
  // final: 이 필드를 불변성을 가지도록 만듭니다

  public CustomUserDetailsService(MemberMapper memberMapper) {
    // final로 선언한 멤버에게 값할당을 안하면 컴파일 에러가 발생합니다.
    this.memberMapper = memberMapper;
  }

  // 데이터베이스로부터 입력받는 사용자이름을 가진 유저를 조회합니다.
  // UserDetails(반환타입): 시큐리티가 인증에 사용하는 객체 규격
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    
    // 데이터베이스로부터 사용자 이름에 해당하는 멤버를 검색합니다.
    Member member = memberMapper.findByUsername(username)
      // Optional의 메서드. 검색 결과가 없으면 예외를 던집니다. (널 체크할 필요가 없어짐)
      .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다"));

    // UserDetails 객체를 생성해 반환합니다.
    return User.builder() // 빌더 패턴으로 객체(UserDetails)를 생성합니다
      .username(member.getUsername())
      .password(member.getPassword())
      .build();
  }
}
