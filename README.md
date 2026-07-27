# order-service — Prethink sample application

A small but realistic Spring Boot REST service used to produce rich
[Moderne Prethink](https://docs.moderne.io/user-documentation/agent-tools/prethink/)
results. The `.github/workflows/prethink.yml` workflow builds the LST for this
project, runs the Prethink recipe weekly, and opens a PR with the regenerated
`.moderne/context/` files.

## Build & test

```bash
./gradlew build      # requires JDK 21+ (the CI workflow uses JDK 25)
./gradlew bootRun    # starts the API on http://localhost:8080
```

## What it is

An order-management API: create orders, place/ship them, look them up per
customer. Spring MVC controllers, a service layer, Spring Data JPA
repositories, and an in-memory H2 database.

| Layer | Classes |
|-------|---------|
| REST (Spring MVC) | `OrderController`, `CustomerController` |
| Service | `OrderService`, `PricingService`, `NotificationService`, `LegacyOrderReport` |
| Persistence (JPA) | `OrderRepository`, `CustomerRepository`, `Order`, `Customer`, `OrderItem` |
| DTOs | `CreateOrderRequest`, `OrderLineRequest` |

## Signals it deliberately gives Prethink

- **Architecture / framework discovery** — Spring MVC endpoints, Spring Data
  JPA repositories, and an H2 datasource for the CALM + Mermaid diagrams.
- **High complexity** — `PricingService.discountRate` (tier × coupon × volume
  branching) and `LegacyOrderReport.build` are intentionally branchy.
- **Code smells** — `LegacyOrderReport` is a God Class (formatting +
  aggregation + CSV in one place); the DTOs are Data Classes;
  `NotificationService.orderPlaced` shows Feature Envy over `Customer`/`Order`.
- **Test-gap / high-risk untested methods** — `PricingServiceTest` covers only
  part of the pricing logic; `LegacyOrderReport` and the order lifecycle
  (`placeOrder`/`ship`) have no tests, so they rank as high-risk untested code.
- **Dependency inventory** — spring-boot-starter-web, -data-jpa, -validation,
  and the H2 runtime dependency.
