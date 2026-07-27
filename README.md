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

[`.github/workflows/prethink.yml`](.github/workflows/prethink.yml) regenerates the
Moderne Prethink context files (`.moderne/context/`) and opens a pull request with
the results.

**Triggers**

- **Schedule** — every Sunday at 06:00 UTC (`cron: "0 6 * * 0"`).
- **Manual** — via **workflow_dispatch** (the *Run workflow* button on the Actions tab).

**Steps**

1. **Checkout repository** — full history (`fetch-depth: 0`) so the LST build and
   git operations have context.
2. **Install JDK 25** — required by the Moderne CLI.
3. **Install the Moderne CLI** — via the official install script from
   `https://app.moderne.io/cli`. The CLI lands in `~/.moderne/cli/bin`, which is
   added to `PATH`. No Moderne login or tenant configuration is required.
4. **Install the Prethink recipe** — the `rewrite-prethink` recipe artifact.
5. **Build the LST** — `mod build .` produces the Lossless Semantic Tree the
   recipe operates on.
6. **Run the Prethink recipe** — `UpdatePrethinkContextStarter` generates the
   `.moderne/context/` files. No LLM provider configuration is required.
7. **Apply generated files** — `mod git apply . --last-recipe-run` writes the
   results into the working tree.
8. **Open a PR** — if there are changes, commits them to a timestamped
   `prethink/update-context-<date>` branch and opens a pull request. If nothing
   changed, it exits cleanly without opening an empty PR.

Two values are pinned as `env` at the top of the workflow:

| Variable | Purpose |
| --- | --- |
| `MODERNE_RECIPE_ARTIFACT` | Recipe artifact coordinates. Defaults to `io.moderne.recipe:rewrite-prethink:LATEST`; pin a specific version for reproducible runs. |
| `PRETHINK_RECIPE` | The starter recipe that generates the context files (`io.moderne.prethink.UpdatePrethinkContextStarter`). |

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
