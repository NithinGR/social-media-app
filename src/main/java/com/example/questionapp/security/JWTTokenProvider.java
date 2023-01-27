package com.example.questionapp.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTTokenProvider {                                                     //JWT tokeni generate edecek classtır. Her bir user için login de bir jwt token oluşturucaz.

    @Value("${questionapp.app.secret}")                                             //degeri application.propertiesin içindedir.
    private String APP_SECRET_KEY;

    @Value("${questionapp.expires.in}")
    private long EXPIRES_IN;                                                        //token ne kadar geçerli olsun.

    public String generateJWTToken(Authentication auth){                            //tokeni generate edicek metodtur.
        JWTUserDetails userDetails = (JWTUserDetails) auth.getPrincipal();          //principal = Giriş yapan kullanıcının bilgilerini ifade eder.
        Date expireDate = new Date(new Date().getTime() + EXPIRES_IN);              //Date ne zamana expire olacagi yani tokenin süresi biteceği bilgisini tutuyor.
        return Jwts.builder()
                .setSubject(Long.toString(userDetails.getId()))
                .setIssuedAt(new Date()).setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512,APP_SECRET_KEY).compact();       //token hazır fonksiyonlar kullanılarak oluşturuldu. SignatureAlgorithm ile Key de belirlendi.
    }

    public Long getUserIdFromJWT(String token){                                            //oluşturulan keyden user id' yi çözen metod. Burda username de kullanılabilir.
        Claims claims = Jwts.parser().setSigningKey(APP_SECRET_KEY).parseClaimsJws(token).getBody();
        return Long.parseLong(claims.getSubject());                                 //keyi çözüp userId yi aldık.
    }

    public boolean validateToken(String token) {                                           //frond endden gelen istekte tokenin geçerli olup olmamasını kontrol eden metod
        try {
            Jwts.parser().setSigningKey(APP_SECRET_KEY).parseClaimsJws(token);      //parse edilirse bizim oluşturduğumuz bir tokendir.
            return !isTokenExpired(token);                                          //tokenin süresi ne halde geçmişmi onu kontrol eder.
        } catch (SignatureException e) {
            return false;
        } catch (MalformedJwtException e) {
            return false;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (UnsupportedJwtException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parser().setSigningKey(APP_SECRET_KEY).parseClaimsJws(token).getBody().getExpiration();  //ne zaman expire olacağıdır.
        return expiration.before(new Date());                                                                           //tokeni 3 günlük yapmıştık onu şimdinin tarihine göre kontrol eder.
    }


}
