# MyBudget Roadmap

**Goal of this roadmap:** a working app where you can connect a (sandbox) bank account through Plaid, pull its transactions into your own database, and put each transaction into a category you choose, all from a simple web page.

Budgets, alerts, logins for multiple users, and similar features come **after** this. See "After the roadmap" at the bottom.

## How to use this file

- Work top to bottom. Each phase builds on the one before it.
- Each step has a **Search:** line. Those are the terms to look up when you get stuck.
- Each phase ends with a **Checkpoint**: something you can see working. Don't move on until it works.
- Commit to git at the end of each phase, at least.


## The big picture

The app is split into **layers**. Each layer only talks to the one below it:

```
Browser (HTML pages + a little JavaScript)
   │  HTTP requests
   ▼
Controller   ← receives requests, returns pages or JSON
   ▼
Service      ← the actual logic ("sync transactions", "assign category")
   ▼
Repository   ← reads/writes the database
   ▼
Database (PostgreSQL)

Service ──► PlaidApi (the Plaid SDK) ──► Plaid's servers
```

How connecting a bank works with Plaid (the "Link flow"):

1. Your **server** asks Plaid for a **link token** (`/link/token/create`).
2. Your **web page** uses that link token to open **Plaid Link**, Plaid's popup where the user logs into their bank.
3. When the user finishes, Link gives your page a **public token**.
4. Your page sends the public token to your **server**.
5. Your server trades it for an **access token** (`/item/public_token/exchange`) and **saves the access token** in the database.
6. From then on, your server uses the access token to fetch accounts and transactions. The access token never goes to the browser.

Plaid vocabulary:
- **Item**: one login at one bank. One Item can have several **accounts** (checking, savings, credit card).
- **Access token**: the secret key for one Item. Treat it like a password.
- **Cursor**: a bookmark Plaid gives you so the next transaction sync only returns what changed.

---

## Phase 0: Tools and project setup

- [x] Install a JDK (Java 21 or newer) and check it with `java -version`. *(You have JDK 27.)*
- [x] Create `pom.xml` with the Spring Boot parent, the web MVC starter and the `plaid-java` dependency.
- [x] Run the app with `mvn spring-boot:run` and open `http://localhost:8080`. You should see "Yurrr".
- [x] Understand what each annotation on `MyBudget.java` does: `@SpringBootApplication`, `@ConfigurationPropertiesScan`, `@RestController`, `@RequestMapping`.
  Search: *@SpringBootApplication what does it do*, *spring component scan*
- [x] Move the `home()` endpoint out of `MyBudget.java` into its own controller class. Keep the main class only for starting the app.
- [x] Decide on a package layout. *(Started: the `plaid` package exists.)* Suggested: one package per feature, all under `dev.hammonds.mybudget`:
  `plaid`, `account`, `transaction`, `category`, `web` (pages).
  Note: `MyBudget.java` currently sits in `dev.hammonds`, one level above `mybudget`. That works, because Spring scans everything beneath the main class's package, but consider moving it into `dev.hammonds.mybudget`.
  Search: *spring boot package by feature*, *spring boot component scan base package*
- [x] Get your Plaid **sandbox** keys from the Plaid dashboard and put them in `.env`.
- [x] Create `PlaidProperties` and map `plaid.*` in `application.properties` to environment variables.
- [x] Make the app actually read `.env`. **Spring does not read `.env` files on its own.** Options: export the variables in your shell before running, set them in your IDE run configuration, or use a small plugin/library that loads `.env`.
  Search: *spring boot environment variables application.properties*, *spring boot dotenv*

**Checkpoint:** the app starts, `/` responds, and the Plaid values from `.env` reach `PlaidProperties` (temporarily log them, then remove the log, and never commit secrets).

---

## Phase 1: Spring fundamentals (learn these before going further)

Not features. These are the ideas everything else uses. Do a tiny experiment for each.

- [x] **Beans and the application context**: Spring creates objects for you and keeps them in a container.
  Search: *spring bean*, *spring application context*
- [x] **Dependency injection through the constructor**: a class asks for what it needs in its constructor, and Spring passes it in.
  Search: *spring constructor injection*
- [ ] **`@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`**: what each one tells Spring.
  Search: *spring stereotype annotations*
- [ ] **`@Configuration` + `@Bean`**: how you create a bean for a class you didn't write (like `PlaidApi`).
  Search: *spring @Bean vs @Component*
- [ ] **`@ConfigurationProperties`**: how `application.properties` values end up in `PlaidProperties`.
  Search: *spring boot configuration properties record*
- [ ] **Controllers and HTTP**: `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestParam`, `@RequestBody`, returning JSON.
  Search: *spring boot rest controller tutorial*, *HTTP GET vs POST*
- [ ] **Java records**: why they're useful for small data classes (request/response objects, often called "DTOs").
  Search: *java record*, *DTO pattern*

**Checkpoint:** you can explain, in your own words, how a `PlaidApi` object ends up inside a controller without you ever writing `new PlaidApi(...)`.

---

## Phase 2: Plaid client setup

- [x] Read up on the Plaid Link flow: Items, link token → public token → access token. *(Your notes are in `PlaidController.java`.)*
- [ ] Read Plaid's Transactions overview, especially `/transactions/sync`.
  Search: *Plaid quickstart*, *Plaid transactions overview*, *Plaid sandbox test credentials*
- [x] Rewrite `PlaidConfig.java` yourself, so you understand every line. It builds the `PlaidApi` bean from `PlaidProperties`.
- [x] Fix the hardcoded environment: right now it always uses `ApiClient.Sandbox`. Make it choose the Plaid environment based on `plaid.environment`.
  Search: *plaid-java ApiClient environment*
- [x] Learn how the Java SDK makes calls: you build a request object, call a method on `PlaidApi`, then `.execute()`, which gives you a `Response`. Check whether it succeeded before reading the body.
  Search: *plaid-java README*, *retrofit Response isSuccessful errorBody*
- [x] Write a throwaway test endpoint that makes one simple Plaid call (for example, creating a link token) and prints the result. This shows your keys and setup work.

**Checkpoint:** one Plaid API call succeeds from your app in sandbox.

---

## Phase 3: Database

You need somewhere to keep access tokens, accounts, transactions and categories.

- [ ] Install **Docker** and run **PostgreSQL** in a container (a `compose.yaml` file in the project root is the easy way).
  Search: *postgres docker compose*, *spring boot docker compose support*
- [ ] Add dependencies: Spring Data JPA, the PostgreSQL driver, and Flyway (for database migrations).
  Search: *spring boot data jpa postgres*, *spring boot 4 flyway starter*
- [ ] Configure the database connection in `application.properties` (URL, username, password, read from env vars like the Plaid keys).
  Search: *spring.datasource.url postgres*
- [ ] Learn the core ideas:
  - **Entity**: a Java class that maps to a table (`@Entity`, `@Id`, `@GeneratedValue`).
  - **Repository**: an interface extending `JpaRepository`; Spring writes the implementation for you.
  - **Migration**: a numbered SQL file (like `V1__create_tables.sql`) that Flyway runs once to create or change tables.
  - **Relationships**: `@ManyToOne` / `@OneToMany` (an account belongs to an item; a transaction belongs to an account).
  Search: *JPA entity tutorial*, *spring data jpa repository*, *flyway spring boot migration*, *JPA ManyToOne*
- [ ] Design your tables. A suggested starting point:

  | Table | Important columns |
  |---|---|
  | `plaid_item` | id, plaid_item_id, access_token, institution_name, transactions_cursor |
  | `account` | id, plaid_account_id, item (FK), name, type, subtype, mask |
  | `category` | id, name (unique) |
  | `transaction` | id, plaid_transaction_id (unique), account (FK), date, name, merchant_name, amount, pending, plaid_category, category (FK, nullable) |

  Things to look up while designing:
  - Store money as `NUMERIC`/`BigDecimal`, **never** `double`. Search: *why not use double for money*
  - In Plaid, **positive amounts are money leaving the account** (spending) and negative amounts are money coming in.
  - `transaction` is a reserved-ish word in SQL; consider naming the table `transactions`.
  - `category` is nullable on transactions because new transactions start "Uncategorized".
- [ ] Write the Flyway migration that creates those tables.
- [ ] Create the entities and repositories.
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` so Hibernate checks your entities match the tables Flyway built, instead of changing tables itself.
  Search: *spring.jpa.hibernate.ddl-auto validate flyway*

**Checkpoint:** the app starts, Flyway creates the tables, and you can save and load a `Category` through its repository (try it from a test endpoint or a test).

---

## Phase 4: Connect a bank (Plaid Link)

### Backend

- [ ] Create a `PlaidService` (or similar) that holds the Plaid logic, so controllers stay thin.
- [ ] Endpoint: **create link token**. `POST /api/plaid/link-token` calls `/link/token/create` with products = transactions, country = US, a client name, a language, and a user id (any fixed string is fine for now). Returns the link token as JSON.
  Search: *plaid link token create*, *plaid-java LinkTokenCreateRequest*
- [ ] Endpoint: **exchange public token**. `POST /api/plaid/exchange` receives the public token, calls `/item/public_token/exchange`, and saves a `plaid_item` row with the access token and item id.
  Search: *plaid item public_token exchange*
- [ ] After the exchange, call `/accounts/get` and save that item's accounts.
  Search: *plaid accounts get*
- [ ] Never send the access token back to the browser, and never log it.

### Frontend (just enough)

- [ ] Add **Thymeleaf** (`spring-boot-starter-thymeleaf`) so Spring can return HTML pages. Learn the difference between `@Controller` (returns a page) and `@RestController` (returns data).
  Search: *spring boot thymeleaf tutorial*, *@Controller vs @RestController*
- [ ] Make a home page with a **"Connect a bank"** button.
- [ ] Add Plaid Link's script tag to the page. Write a small piece of JavaScript that:
  1. calls your link-token endpoint with `fetch`,
  2. opens Plaid Link with that token,
  3. in Link's `onSuccess` callback, sends the public token to your exchange endpoint with `fetch`.
  Search: *plaid link web*, *javascript fetch POST JSON*, *javascript async await*
- [ ] Log in through Link with Plaid's sandbox test credentials.
  Search: *plaid sandbox credentials user_good*

**Checkpoint:** clicking "Connect a bank" and logging in with sandbox credentials leaves a new row in `plaid_item` and rows in `account`.

---

## Phase 5: Load transactions

- [ ] Read how `/transactions/sync` works: you send the item's access token and its saved **cursor** (empty the first time). Plaid returns `added`, `modified` and `removed` lists, a `next_cursor`, and `has_more`.
  Search: *plaid transactions sync*, *plaid transactions sync pagination has_more*
- [ ] Write a service method `syncTransactions(item)` that:
  - [ ] calls `/transactions/sync` in a loop while `has_more` is true,
  - [ ] **inserts** each transaction in `added` (map Plaid's fields to your entity),
  - [ ] **updates** each transaction in `modified` (find it by `plaid_transaction_id`),
  - [ ] **deletes** each transaction in `removed`,
  - [ ] saves the new cursor on the item **only after** everything succeeded.
- [ ] Make the method **transactional**, so a failure partway through doesn't leave half-saved data.
  Search: *spring @Transactional*, *@Transactional self invocation*
- [ ] Watch out: right after linking, Plaid may not have transactions ready yet, so the first sync can come back empty. Syncing again a bit later fixes this. (Webhooks solve it properly; that's for after the roadmap.)
  Search: *plaid transactions sync empty first call*, *plaid sandbox transactions*
- [ ] Endpoint: `POST /api/transactions/sync` runs the sync for every saved item.
- [ ] Save Plaid's own category (`personal_finance_category.primary`) in `plaid_category`. It's useful as a hint later.
  Search: *plaid personal finance category*

**Checkpoint:** after a sync, the `transactions` table has sandbox transactions, and running sync again does **not** create duplicates.

---

## Phase 6: Categories

- [ ] Insert a few default categories (Groceries, Dining, Rent, Transport, Subscriptions, Income, Other) with a Flyway migration.
- [ ] `CategoryService` + endpoints to **list**, **create**, **rename**, and **delete** categories.
  Search: *spring boot CRUD tutorial*
- [ ] Decide what happens to transactions when their category is deleted (set them back to uncategorized is simplest).
  Search: *foreign key ON DELETE SET NULL*
- [ ] Reject a blank or duplicate category name and return a helpful error.
  Search: *spring boot validation @Valid @NotBlank*, *spring @ControllerAdvice exception handler*
- [ ] Endpoint to **assign a category** to a transaction (for example `POST /transactions/{id}/category`).
- [ ] Endpoint to list transactions, newest first, with an option to filter by category and to show only uncategorized ones.
  Search: *spring data jpa derived query methods*, *spring data jpa sort*
- [ ] *(Optional but nice)* Return per-category totals for a month. This is the first step toward budgets.
  Search: *spring data jpa @Query group by sum*

**Checkpoint:** using the endpoints (Postman, curl, or your browser), you can create a category, assign it to a transaction, and see the transaction listed under that category.

---

## Phase 7: Simple UI

Plain HTML pages rendered by Thymeleaf, with a little CSS. Aim for usable, not pretty.

- [ ] Learn the Thymeleaf basics: passing data in with `Model`, `th:each` (loops), `th:text`, `th:if`, and `th:action` on forms.
  Search: *thymeleaf th:each*, *spring mvc Model addAttribute*, *thymeleaf form submit*
- [ ] Shared layout: a top nav with links to Home / Transactions / Categories.
  Search: *thymeleaf fragments layout*
- [ ] **Home page**: "Connect a bank" button, a list of connected banks and accounts, and a "Sync transactions" button.
- [ ] **Transactions page**: a table (date, description, amount, account, category). Each row has a category dropdown and a Save button that submits a form.
  Search: *html select dropdown form*, *post redirect get pattern*
- [ ] Filters on the transactions page: by category, "uncategorized only", and by month.
- [ ] **Categories page**: list categories, add a new one, rename, delete.
- [ ] Show amounts as money, with spending and income looking different (for example, red and green).
  Search: *thymeleaf format number currency*
- [ ] Show friendly messages for success and errors after a form submit.
  Search: *spring RedirectAttributes flash attributes*
- [ ] Add a minimal CSS file under `src/main/resources/static`. Or drop in a classless CSS framework so it looks decent with no effort.
  Search: *spring boot static resources*, *classless css framework*

**Checkpoint (the finish line):** from a fresh database, you can open the app → connect a sandbox bank → sync → see transactions → create a category → categorize transactions from the dropdown → filter to see them. 🎉

---

## Phase 8: Clean up and harden (before showing it off)

- [ ] Handle Plaid errors properly: read the error body, log it, and show a clear message instead of a crash page.
  Search: *plaid errors ITEM_LOGIN_REQUIRED*, *spring boot global exception handling*
- [ ] Use logging (`Slf4j` `Logger`) instead of `System.out.println`, and make sure no secrets are logged.
  Search: *spring boot logging slf4j*
- [ ] Write tests:
  - [ ] unit tests for services, with Plaid mocked using **Mockito**,
  - [ ] repository tests against a real Postgres using **Testcontainers**,
  - [ ] one or two controller tests with **MockMvc**.
  Search: *spring boot testing*, *mockito when thenReturn*, *testcontainers spring boot*, *MockMvc*
- [ ] Write a README: what the app does, how to set up `.env`, how to start the database and app, and how to log in with sandbox credentials.
- [ ] Be honest about security: access tokens are stored in plain text, so note that in the README as a known limitation (or fix it, see below).

---

## After the roadmap (ideas for later)

- [ ] Monthly **budgets** per category and "spent vs. budgeted" progress bars (the original idea in `ideas.md`).
- [ ] **Auto-categorization rules**: "if merchant contains 'Starbucks' → Dining", applied on every sync.
- [ ] Detect **subscriptions** (recurring transactions). Search: *plaid transactions recurring get*
- [ ] **Webhooks** so Plaid tells you when new transactions are ready, instead of you pressing Sync. You'll need a tool like ngrok for local development.
- [ ] **User accounts and login** with Spring Security, so each person only sees their own data.
- [ ] **Encrypt access tokens** in the database.
- [ ] Charts on a dashboard page.
- [ ] Deploy it somewhere (Docker image + a cloud host).
