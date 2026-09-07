package br.com.teachback.backend.security;

import br.com.teachback.backend.model.Usuario;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;


    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Usuario usuario){
        try{
            SecretKey key = getSigningKey();
            return Jwts.builder()
                    .subject(usuario.getEmail())
                    .issuer("teachback-api")
                    .issuedAt(new Date())
                    .expiration(Date.from(generateExpirationDate()))
                    .signWith(key)
                    .compact();
        } catch (Exception e){
            throw new RuntimeException("Erro ao gerar token ", e);
        }
    }

    public String validateToken(String token){
        try{
            SecretKey key = getSigningKey();

            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e){
            return "";
        }
    }

    private Instant generateExpirationDate(){
        return LocalDateTime.now()
                .plusHours(4)
                .toInstant(ZoneOffset.of("-03:00"));
    }
}
