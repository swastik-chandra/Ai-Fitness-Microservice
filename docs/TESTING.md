# Testing Strategy

## Testing Pyramid

```
                    ┌─────────────┐
                    │   E2E (16)  │  ← Playwright (3 browsers)
                   ┌┤─────────────├┐
                   │  API (10)     │  ← Newman/Postman
                  ┌┤──────────────├┐
                  │ Integration (3) │  ← @SpringBootTest + MockMvc
                 ┌┤───────────────├┐
                 │  Unit (30+)      │  ← JUnit 5 + Mockito
                 └──────────────────┘
```

## Backend Testing (JUnit 5 + Mockito)

### Unit Tests
| Service | File | Tests | Coverage |
|---------|------|-------|----------|
| AuthService | `AuthServiceTest.java` | 5 | register, login, refresh, logout, validate |
| UserService | `UserServiceTest.java` | 4 | register, findById, findByEmail, not-found |
| ActivityService | `ActivityServiceTest.java` | 5 | track+Kafka, invalidUser, getUserActivities, getById, notFound |
| AnalyticsService | `AnalyticsServiceTest.java` | 4 | getSummary, updateOnEvent, getStreaks, cacheHit |
| NotificationService | `NotificationServiceTest.java` | 4 | createAndSend, unreadCount, markAsRead, markAllAsRead |
| AIMessageListener | `ActivityMessageListenerTest.java` | 2 | processActivity, errorHandling |

### Controller Tests (@WebMvcTest)
| Controller | File | Tests |
|------------|------|-------|
| AuthController | `AuthControllerTest.java` | register, login, validation |
| UserController | `UserControllerTest.java` | register, invalidEmail, wrongCredentials |

### Running Backend Tests
```bash
# Single service
cd authService && ./mvnw test

# All services
./scripts/deploy.sh dev test
```

## API Testing (Newman/Postman)

Collection: `postman/fitness-platform.postman_collection.json`

### Test Scenarios (10 tests)
1. **Auth**: Register → Login → Validate Token
2. **Activities**: Track Activity → Get User Activities
3. **Analytics**: Get Summary
4. **Notifications**: Get Notifications → Get Unread Count
5. **AI**: Chat → Generate Workout Plan
6. **Health**: Gateway Health Check

### Running
```bash
npx newman run postman/fitness-platform.postman_collection.json \
  --env-var "baseUrl=http://localhost:8080"
```

## E2E Testing (Playwright)

Config: `e2e/playwright.config.ts` — runs in Chromium, Firefox, WebKit

### Test Suites (16 tests)
| File | Tests | Scenarios |
|------|-------|-----------|
| `auth.spec.ts` | 5 | Login page, register page, form validation, navigation |
| `dashboard.spec.ts` | 3 | Dashboard redirect, sidebar, logout |
| `features.spec.ts` | 8 | Activities, AI features, responsive (mobile/tablet/desktop), navigation |

### Running
```bash
cd e2e
npm install
npx playwright install
npx playwright test
npx playwright test --headed  # Watch in browser
```

## CI/CD Integration

The `ci.yml` pipeline runs:
1. **Backend Tests**: All 8 services in parallel matrix
2. **Frontend**: Lint + TypeScript check + Build
3. **Security Scan**: Trivy filesystem + Docker image scanning
4. **E2E**: Playwright against staging (post-deploy)
