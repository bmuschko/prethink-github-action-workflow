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

## The Prethink workflow

[`.github/workflows/prethink.yml`](.github/workflows/prethink.yml) runs weekly (and
on demand from the Actions tab). It installs the Moderne CLI, builds the LST, runs
the Prethink recipe to regenerate the `.moderne/context/` files, and opens a pull
request with the changes. No Moderne login is required.

See the
[Prethink documentation](https://docs.moderne.io/user-documentation/agent-tools/prethink/)
for what the generated context is and how agents use it.

## Required setting: allow Actions to create pull requests

The final step opens a PR using the built-in `GITHUB_TOKEN`. By default GitHub
**blocks** the token from creating pull requests, so the step fails with:

```
pull request create failed: GraphQL: GitHub Actions is not permitted to
create or approve pull requests (createPullRequest)
```

Enable the setting **once** per repository (or at the organization level):

**Settings → Actions → General → Workflow permissions →**
✅ **Allow GitHub Actions to create and approve pull requests**

> The workflow already grants itself write access to push the branch and open the
> PR via its job-level `permissions:` block (`contents: write`,
> `pull-requests: write`), so you do **not** need to change the default "Read
> repository contents" workflow permission — only tick the checkbox above.

You can also toggle it with the GitHub CLI:

```bash
# Enable
gh api -X PUT repos/OWNER/REPO/actions/permissions/workflow \
  -f default_workflow_permissions=read \
  -F can_approve_pull_request_reviews=true

# Verify
gh api repos/OWNER/REPO/actions/permissions/workflow
# => { "default_workflow_permissions": "read", "can_approve_pull_request_reviews": true }
```
