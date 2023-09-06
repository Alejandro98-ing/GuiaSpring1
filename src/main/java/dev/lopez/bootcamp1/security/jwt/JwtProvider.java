package dev.lopez.bootcamp1.security.jwt;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

import dev.lopez.bootcamp1.security.dto.JwtDto;
import dev.lopez.bootcamp1.security.entity.UsuarioPrincipal;
import io.jsonwebtoken.*;

@Component
public class JwtProvider {
    private final static Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private int expiration;

    /**
     * 
     * @param authentication
     * @return Un token que contiene el username, los roles, fecha de expiración y una firma
     */
    public String generateToken(Authentication authentication){
        UsuarioPrincipal usuarioPrincipal = (UsuarioPrincipal) authentication.getPrincipal();
        List<String> roles = usuarioPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        return Jwts.builder().setSubject(usuarioPrincipal.getUsername()).claim("roles", roles).setIssuedAt(new Date()).setExpiration(new Date(new Date().getTime() + expiration)).signWith(SignatureAlgorithm.HS512, secret.getBytes()).compact();
    }

    /**
     * 
     *@param token
     *@return true si el token es válido o false si el token es inválido
     */
    public String getNombreusuarioFromToken(String token){
        return Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * 
     * @param token
     * @return true si el token es válido o false si el token es inválido
     */
    public boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token);
            return true;
        }catch (MalformedJwtException e){
            logger.error("Token mal formado");
        }catch (UnsupportedJwtException e){
            logger.error("Token no soportado");
        }catch (ExpiredJwtException e){
            logger.error("Token expirado");
        }catch (IllegalArgumentException e){
            logger.error("Token vacío");
        }catch (SignatureException e){
            logger.error("Error en la firma");
        }
        return false;
    }

    /**
     * 
     * @param jwtDto
     * @return un nuevo token en caso de que el token que se le pase como argumento haya expirado
     * @throws ParseException
     */
    public String refreshToken(JwtDto jwtDto) throws ParseException{
        try{
            Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(jwtDto.getToken());
        }catch (ExpiredJwtException e){
            JWT jwt = JWTParser.parse(jwtDto.getToken());
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            String nombreUsuario = claims.getSubject();
            List<String> roles = (List<String>)claims.getClaim("roles");

            return Jwts.builder().setSubject(nombreUsuario).claim("roles", roles).setIssuedAt(new Date()).setExpiration(new Date(new Date().getTime() + expiration)).signWith(SignatureAlgorithm.HS512, secret.getBytes()).compact();
        }

        return null;
    }
}
