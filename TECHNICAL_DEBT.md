# Technical Debt Register

| # | Item | Named in | Status | Resolution |
|---|---|---|---|---|
| 1 | No API versioning strategy: breaking-change risk | Session 8 Architecture Clinic #1 | **RESOLVED** (Session 22, `ab595b1`) | product-service already served `/api/v1/products`. The Gateway now also routes the legacy `/api/products/**` to `/api/v1/products/**` with `Deprecation: true` and `Sunset: Wed, 01 Oct 2026 00:00:00 GMT`. Legacy writes are still ADMIN-only. Remove the legacy route after one deprecation cycle. |
| 2 | No idempotency on payment retry: duplicate-charge risk | Session 4 / Session 12 homework | OPEN | — |
| 3 | Saga dual write: `orderRepository.save` then `kafkaTemplate.send`, with no shared transaction | Session 22 | OPEN | — |
| 4 | No Bulkhead, TimeLimiter or Retry anywhere; the only CircuitBreaker (`createOrderWithPayment`) is unreachable from any endpoint | Session 23 (Lab 19) | OPEN | See `k6/FINDINGS.md` |

## Prioritization of the open items

- **Idempotency (next):** a double charge is the most expensive failure on the platform, but the risk is
  latent today. Order→payment has only `@CircuitBreaker`, with no `@Retry`, so it must land before anyone
  adds a retry to that call.
- **Outbox (after persistence):** the dual-write gap in `OrderService.createOrder` is real. But
  `OrderRepository` is an in-memory map with no database transaction to share, so the outbox has to follow
  moving order-service onto JPA/PostgreSQL.
