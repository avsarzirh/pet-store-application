package com.avsarzirh.PetStoreApplication.security.jwt;

import com.avsarzirh.PetStoreApplication.security.service.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtils {

    @Value("${app.jwtSecret}")
    private String jwtSecret;
    @Value("${app.jwtExpiartionMs}")
    private long expirationMs;

    //! JWT'leri sign ederken bu metodu cagiracagiz. Bu metod bize signing key'i verecek.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    //!!! 1 - GENERATE JWT
    public String generateJWT(Authentication authentication) {
        //! Authentication, Security Context'in icerisinde bulunan yapidir.
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userDetails.getUsername()) //Token kime ait? Oznesi kim?
                .issuedAt(new Date()) //Tokenin verilme tarihi
                .expiration(new Date(new Date().getTime() + expirationMs)) //Tokenin suresi ne zaman bitecek
                .signWith(getSigningKey())
                .compact(); //Token hazirlanir
    }

    //!!! 2 - VALIDATE JWT
    public boolean validateJWT(String jwt) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey()) //Parser'a kullanacagi signing key'i veriyoruz.
                    .build() //Parser olusturuyoruz
                    .parseSignedClaims(jwt); //Claim'leri coz diyoruz

            //! Ekstra olarak, tokenin icerisindeki bilgilerin dogrulugu da teyit edilebilir!

            return true; //Eger bu satira gelinebiliyorsa, token cozulebiliyor, yani gecerlidir.
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    //!!! 3 - EXTRACT USERNAME FROM JWT
    public String extractUsernameFromJWT(String jwt) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .getSubject();
    }
}
