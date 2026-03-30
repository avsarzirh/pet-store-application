package com.avsarzirh.PetStoreApplication.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {

    //!!! commence metodu, AuthneticationException firlayacagi zaman, gonderilecek response'u belirlemek icin
    //!!! cagirilacak metoddur.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //401 Unauthorized

        //body'i hazirlamak
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Unathorized");
        body.put("message", "Hop hemserim nereye?");
        body.put("path", request.getServletPath()); //bir standarttir, nereye istek attik ta bu hata yasandi

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
