package com.expense;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "spring.data.mongodb.uri=mongodb://127.0.0.1:27017/expense_test",
    "app.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
    "app.frontend-url=http://localhost:5173"
})
@AutoConfigureMockMvc
class JwtSecurityTest {
    private static final String TEST_SECRET = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    @Autowired private MockMvc mvc;
    @MockBean private TransactionRepository transactions;

    @Test
    void transactionAndDashboardRoutesRejectAnonymousRequests() throws Exception {
        mvc.perform(get("/api/transactions")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/dashboard/summary")).andExpect(status().isUnauthorized());
    }

    @Test
    void actuatorHealthAndProbeEndpointsArePublicButOtherActuatorPathsAreProtected() throws Exception {
        mvc.perform(get("/actuator/health"))
            .andExpect(result -> assertNotEquals(401, result.getResponse().getStatus()));
        mvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health/unknown")).andExpect(status().isUnauthorized());
    }

    @Test
    void malformedAndExpiredJwtTokensAreRejected() throws Exception {
        mvc.perform(get("/api/transactions").header("Authorization", "Bearer not-a-jwt"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/transactions").header("Authorization", "Bearer " + token(Instant.now().minusSeconds(60))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void validJwtAllowsRequestsAsTheTokenSubject() throws Exception {
        when(transactions.findAllByUserIdOrderByDateDesc("user-1")).thenReturn(List.of());

        mvc.perform(get("/api/transactions").header("Authorization", "Bearer " + token(Instant.now().plusSeconds(300))))
            .andExpect(status().isOk());
    }

    private String token(Instant expiresAt) {
        Instant now = Instant.now();
        return Jwts.builder().subject("user-1").issuedAt(Date.from(now)).expiration(Date.from(expiresAt))
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET))).compact();
    }
}
