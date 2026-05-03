package dev.guillemdiaz.herald;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class HeraldApplicationTests {

    @MockitoBean
    RedisTemplate<String, Object> redisTemplate;

    @Test
    void contextLoads() {
    }

}
