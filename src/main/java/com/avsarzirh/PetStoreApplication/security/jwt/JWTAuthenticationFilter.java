package com.avsarzirh.PetStoreApplication.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    //!!! Filtrenin amaci, istegin uygulamaya girisine izin vermek/reddetmektir.
    //!!! Istegin Authorization Header'inda, token var mi? Varsa gecerli mi?
    //!!! Bu kontroller sonucunda istek sonraki filtreye ya iletilir, ya da 401 ile reddedilir.

    private final JWTUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //! doFilterInternal metodu, bizim filtremizin tam olarak ne is yapacagini belirledigimiz metoddur.

        String jwt = extractJWTFromRequest(request);

        if (jwt != null && jwtUtils.validateJWT(jwt)) {
            try {
                String username = jwtUtils.extractUsernameFromJWT(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                //!SecurityContext'e ekleme
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (UsernameNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        //!Eger bizim olusturdugumuz filtre, belirli istekleri filtrelememeliyse, bu metod kullanilmali.
        AntPathMatcher antPathMatcher = new AntPathMatcher();

        // /auth/** da kullanilabilir
        return antPathMatcher.match("/auth/register", request.getServletPath()) ||
                antPathMatcher.match("/auth/login", request.getServletPath());
    }

    private String extractJWTFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization"); //! Token dogrudan Authorization'da gelmez. Bearer token olarak gelir.

        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        return null;
    }
}
