package org.example.utils;

import com.alibaba.fastjson2.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.TokenDTO;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * http token 验证
 *
 * @author chenkedi
 */
@Component
@Slf4j
public class JWTUtil {

    // 加密秘钥
    private static String secretKey = "ADB8E3D5287312ADCBA74014928CE2CEE";

    /**
     * 生成签名
     *
     * @return 加密的Token
     */
    public static String createSign(String seed, Integer expire) {
        Date iatDate = new Date();
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.MINUTE, expire);
        Date expiresDate = nowTime.getTime();

        return JWT.create()
                .withClaim("seed", null == seed ? null : seed)
                .withIssuedAt(iatDate)           // sign time
                .withExpiresAt(expiresDate)      // expire time
                .sign(Algorithm.HMAC256(secretKey));
    }

    /**
     * 校验Token是否正确
     *
     * @return 是否正确
     */
    public static TokenDTO verifyToken(String token) {
        //根据传来的Token获取Seed
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        TokenDTO tokenDTO = new TokenDTO();
        try {
            Map<String, Claim> claims = verifier.verify(token).getClaims();
            Claim seed_claim = claims.get("seed");
            String jsonSign = seed_claim.asString();
            JSONObject jsonObject = JSONObject.parseObject(jsonSign);
            tokenDTO.setAccountId(jsonObject.getLong("accountId"));
            tokenDTO.setRoleId(jsonObject.getInteger("roleId"));
        } catch (Exception e) {
            tokenDTO = null;
            log.error(e.toString());
        }
        return tokenDTO;
    }

    /**
     * 无需secret解密，获得Token中的Seed信息
     *
     * @return Token中包含的种子值，通常是用户ID
     */
    public static String getSeed(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        Map<String, Claim> claims = verifier.verify(token).getClaims();
        Claim seed_claim = claims.get("seed");
        return seed_claim.asString();
    }

    /**
     * 获取Token的过期时间
     *
     * @param token JWT Token
     * @return 过期时间（Date格式）
     */
    public static Date getExpiresDate(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        return verifier.verify(token).getExpiresAt();
    }

    /**
     * 解析token获取过期时间
     *
     * @param token JWT Token
     * @return 过期时间的时间戳（毫秒）
     */
    public static long getExpiresAt(String token) {
        Date expiresDate = getExpiresDate(token);
        return expiresDate != null ? expiresDate.getTime() : 0;
    }

    public static void main(String[] args) {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setAccountId(1L);
        tokenDTO.setRoleId(1);
        String req = createSign(tokenDTO.toString(), 720000000);
        System.out.println(req);
        System.out.println(JWTUtil.verifyToken(req));
    }
}
