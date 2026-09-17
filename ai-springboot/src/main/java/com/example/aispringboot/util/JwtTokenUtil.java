package com.example.aispringboot.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.aispringboot.config.JwtConfig;
import com.example.aispringboot.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Component
public class JwtTokenUtil implements ApplicationContextAware {
    private static final String ISSUER = "mind-assistant";
    private static ApplicationContext applicationContext;

    //用于在静态工具类中获取Spring容器管理的bean
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        JwtTokenUtil.applicationContext = applicationContext;
    }

    private static JwtConfig getJwtConfig() {
        return applicationContext.getBean(JwtConfig.class);
    };

    //生成token的方法
    public static String generateToken(Long userId, String username, String userType) {
        try {
            //获取jwt配置
            JwtConfig jwtConfig = getJwtConfig();
            //生成签名的算法
            Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
            //生成过期时间
            Date expiration = new Date(System.currentTimeMillis() + jwtConfig.getExpiration());

            String token = JWT.create()
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withClaim("userType", userType)
                    .withExpiresAt(expiration) //设置过期时间
                    .withIssuedAt(new Date()) //设置签发时间
                    .withIssuer(ISSUER) //设置签发者
                    .sign(algorithm);
            return token;
        } catch (Exception e) {
            throw new BusinessException("生成token 失败：" + e);
        }
    }

    //提取token
    public static String extractTokenFromRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String tokenHeader = request.getHeader("token");
        if (StringUtils.hasText(tokenHeader)) {
            return tokenHeader;
        }
        return null;
    }

    //获取当前token
    public static String getCurrentToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String token = (String) request.getAttribute("jwtToken");
            if (token != null) {
                return token;
            }

            //备用方案：从请求头中获取token
            String headToken = extractTokenFromRequest(request);
            return headToken;
        }
        return null;
    }

    //验证token
    public static TokenVerificationResult validateToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        String username = jwt.getClaim("username").asString();
        String userType = jwt.getClaim("userType").asString();
        if (userId != null && StringUtils.hasText(username) && userType != null){
            return new TokenVerificationResult(userId, username, userType, true);
        }
        return null;
    }

    //验证token的有效性
    public static DecodedJWT verifyToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException("Token 不能为空");
        }
        //token解码
        JwtConfig jwtConfig = getJwtConfig();
        Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        verifier.verify(token);
        return verifier.verify(token);
    }

    //Token验证结果封装类
    @Getter
    public static class TokenVerificationResult {
        private final Long userId;
        private final String username;
        private final String userType;
        private final boolean valid;

        public TokenVerificationResult(Long userId, String username, String userType, boolean valid) {
            this.userId = userId;
            this.username = username;
            this.userType = userType;
            this.valid = valid;
        }
    }
}