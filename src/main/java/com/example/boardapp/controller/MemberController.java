package com.example.boardapp.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.boardapp.domain.Member;
import com.example.boardapp.dto.SignUpRequest;
import com.example.boardapp.mapper.MemberMapper;

import jakarta.validation.Valid;

@Controller // 컨트롤러 빈으로 등록합니다.
@RequestMapping("/member") // URL prefix: 이 컨트롤러가 다루는 모든 URL 앞에 이 값을 붙여줍니다
public class MemberController { // 멤버 관련된 요청/응답을 처리하는 컨트롤러
  
  // 의존성 선언
  private final MemberMapper memberMapper; // 멤버 데이터 다루는 용도
  private final PasswordEncoder passwordEncoder; // 비밀번호 암호화하는 객체

  public MemberController(MemberMapper memberMapper, PasswordEncoder passwordEncoder) {
    this.memberMapper = memberMapper;
    this.passwordEncoder = passwordEncoder;
  }

  // 회원가입 페이지 요청을 처리하는 핸들러(처리하는 사람)
  @GetMapping("/join") // GET /join 요청을 처리합니다
  // Model: MVC(Model-View-Controller)에서 모델에 해당합니다.
  // 모델의 역할: 데이터를 활용해 뷰를 업데이트합니다.
  public String joinForm(Model model) {
    
    // 뷰의 memberForm(회원가입 폼)과 가입요청DTO(SignUpRequest)를 매핑합니다.
    // 사용자가 가입할 때 입력한 값이 이 DTO에 저장됩니다.
    model.addAttribute("memberForm", new SignUpRequest());

    // 회원가입 페이지를 클라이언트에게 전송합니다. (서버의 응답)
    return "member/join"; // 나중에 이 페이지를 만들겁니다
  }

  // 회원가입 처리 핸들러
  @PostMapping("/join") // 요청을 처리할 주소. POST(데이터 생성 요청) /join (주소)
  // dto 매개변수 앞에 두개의 어노테이션 @Valid, @ModelAttribute가 붙은 상태
  // @Valid: dto를 대상으로 사전에 정의한 유효성 검사를 실행해라
  // @ModelAttribute: 가입 페이지의 memberForm(회원가입 폼)과 가입요청 dto를 매핑합니다
  // BindingResult: validation이 가입요청DTO(SignUpRequest)를 대상으로 유효성 검증을 마치고
  // 결과를 담은 객체
  public String join(@Valid @ModelAttribute("memberForm") SignUpRequest dto, BindingResult bindingResult) {
    
    // 비밀번호와 비밀번호 확인(password confirm)이 일치하지 않을 때
    if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
      // 이 경우 bindingResult에 직접 에러를 추가합니다.
      // bindingResult에 에러를 수집하는 중..
      // rejectValue(필드, 에러코드, 에러 메시지)
      bindingResult.rejectValue("passwordConfirm", "passwordMismatch", "비밀번호가 일치하지 않습니다");
    }

    // 사용자가 입력한 유저네임이 데이터베이스에 존재하는지 검사합니다
    // dto.getUsername(): 사용자가 가입할 때 입력한 유저네임(아이디)
    // isPresent: 옵셔널의 메서드. 내부에 값(Member)이 존재하는지 확인합니다 (boolean타입으로 결과 반환)
    if (memberMapper.findByUsername(dto.getUsername()).isPresent()) {
      // 아이디 중복 오류를 bindingResult에 새롭게 추가합니다.
      bindingResult.rejectValue("username", "duplicatedUsername", "이미 존재하는 아이디입니다");
    }

    // bindingResult에 에러가 있다면
    // 에러: validation이 추가한 에러, 우리가 직접 추가한 에러 등
    if (bindingResult.hasErrors()) {
      // 에러메시지와 함께 가입페이지를 다시 전송합니다.
      return "member/join";
    }

    // 모든 검사를 통과한 경우 가입 절차를 시작합니다.

    // 멤버 객체를 생성합니다.
    // 비밀번호를 암호화해서 저장합니다.
    Member member = new Member(dto.getUsername(), passwordEncoder.encode(dto.getPassword()));

    // 데이터베이스에 저장합니다.
    memberMapper.save(member);

    // 로그인 요청 핸들러로 이동합니다.
    return "redirect:/member/login";
  }

  // 로그인 페이지 요청 핸들러
  @GetMapping("/login") // GET /login 요청을 처리합니다
  public String loginForm() {
    // 클라이언트에게 로그인 페이지를 전송합니다.
    return "member/login";
  }
}
