package com.security.jwt.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.security.jwt.auth.PrincipalDetails;
import com.security.jwt.model.Member;
import com.security.jwt.repository.MemberRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * Security 가 filter 를 가지고 있는데 그 필터중에 BasicAuthenticationFilter 라는 것이 있다.
 * 권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 무조건 타게 되어있다.
 * 만약에 권한이나 인증이 필요한 주소가 아니라면 이 필터를 안탄다.
 */
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private MemberRepository repository;

    public JwtAuthorizationFilter(final AuthenticationManager authenticationManager, MemberRepository memberRepository) {
        super(authenticationManager);
        this.repository = memberRepository;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        System.out.println("인증이나 권한이 필요한 url이 요청됨");

        String jwtHeader = request.getHeader("Authorization");
        System.out.println("jwtHeader : " + jwtHeader);

        // header가 있는지 확인
        if (jwtHeader == null || !jwtHeader.startsWith("Bearer")) {
            chain.doFilter(request,response);
            return;
        }

        String jwtToken = request.getHeader("authorization").replace("Bearer ", "");

        String username = JWT.require(Algorithm.HMAC512("joshua")).build().verify(jwtToken).getClaim("username").asString();

        // 서명이 정상적으로 되었다는 것임, 즉 인증이되었다는 것
        if (username != null) {
            Member memberEntity = repository.findByUsername(username);

            PrincipalDetails principalDetails = new PrincipalDetails(memberEntity);

            //JWT 토큰 서명을 통해서 서명이 정상이면 Authentication 객체를 만들어 준다.
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());

            // 강제로 시큐리티 세션에 접근하여 Authentication 객체를 저장한 것
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }
        chain.doFilter(request, response);
    }
}
