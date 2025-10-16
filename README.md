# MMOSS – Task 3 Final Version (Java, Text-based)

## Overview
This project provides a **text-based Java implementation** of the Monash Merchant Online Supermarket System (**MMOSS**), aligned with **Task 3** requirements:
- **Fixed logins only**: two customers + one admin (no registration; no profile editing)
- **Admin product & inventory**: full CRUD
- **Shopping & Cart**: full feature set (4-person team scope)
- **Checkout**: balance-only payment, updated fee/discount policy, **no delivery address input**
- **CSV** persistence (no database)

## Accounts (Fixed)
| Role | Email | Password |
|---|---|---|
| Customer | `student@student.monash.edu` | `Monash1234!` |
| Customer | `staff@monash.edu` | `Monash1234!` |
| Admin | `admin@monash.edu` | `Admin1234!` |

## Project Structure
```
src/edu/monash/
  app/Main.java
  domain/*.java      (User, Product, Order, Cart, Store, PromoCode, etc.)
  repo/*.java        (UsersRepo, ProductsRepo, OrdersRepo, CsvUtil, etc.)
  service/*.java     (AuthService, CatalogService, CheckoutService, etc.)
  usecase/*.java     (LoginUser, ViewProfile)

data/                (CSV files for users, products, stores, orders, etc.)
docs/                (place annotated screenshots if needed for submission)
```

## Feature Summary
- **User/Login**: only three fixed accounts can sign in (two customers, one admin); no registration; no profile editing.
- **Admin (Full)**: add/edit/delete products (extended fields supported); CSV persistence.
- **Catalog**: browse/search/filter (brand/category/price/in-stock); out-of-stock shown last.
- **Cart**: add/remove/clear; 20 item types max; ≤10 qty per item; insertion order preserved.
- **Checkout**: balance-only; delivery fee $20 (student FREE), pickup FREE; student pickup 5% when no promo code; first-pickup strategies supported (`FIRST_PICKUP`, `NEWMONASH20`); **no delivery address input**.
- **Persistence**: orders/order_items/payments recorded; products/users/balances/memberships/stores/promos in CSV.

## Installation Guidelines (IDE)
> The system is designed to **run inside the IDE** without external dependencies.
1. **Install IntelliJ IDEA** (Community or Ultimate).
2. **Open** the project folder `MMOSS_Task3_Final` in IntelliJ.
3. Set **Project SDK** to **Java 17–23**.
4. Mark `src/` as **Sources** (if not auto-detected).
5. Create a **Run Configuration**:
   - Main class: `edu.monash.app.Main`
   - Working Directory: project root (so CSVs read from `data/`)
6. **Run** ▶️ the configuration and interact in the console.
7. (Optional) Place annotated screenshots under `docs/` for submission.

## Troubleshooting
- **No data loaded / CSV not found**  
  Ensure the working directory is the project root (contains `data/`).  
  In IntelliJ: Run Configuration → Working directory → set to project root.

- **Garbled characters on Windows console**  
  Switch console encoding to UTF-8 or run in IntelliJ built-in console.

- **Cannot log in**  
  Only the three fixed accounts are accepted; check exact email/password.

- **Class not found / cannot run**  
  Confirm SDK set to Java 17–23; ensure `src/` is marked as Sources and main class is `edu.monash.app.Main`.

## Team Roles (Balanced)
| Member | Role | Main Responsibility | Key Files |
|--------|------|---------------------|-----------|
| A | Login & Profile (read-only) | Fixed-account login flow and profile viewing | `src/edu/monash/service/AuthService.java`, `src/edu/monash/usecase/LoginUser.java`, `src/edu/monash/usecase/ViewProfile.java` |
| B | Catalog & Cart | Browsing, search, filter, cart orchestration | `src/edu/monash/service/CatalogService.java`, `src/edu/monash/service/CartService.java`, `src/edu/monash/domain/Product.java`, `src/edu/monash/domain/ShoppingCart.java`, `src/edu/monash/domain/CartItem.java` |
| C | Checkout & Pricing | Checkout pipeline, promo & pricing rules, payments | `src/edu/monash/service/CheckoutService.java`, `src/edu/monash/service/PricingService.java`, `src/edu/monash/service/PromoService.java`, `src/edu/monash/repo/PaymentsRepo.java` |
| D | Admin & Persistence | Admin product CRUD; CSV repositories; integration | `src/edu/monash/service/AdminService.java`, `src/edu/monash/repo/ProductsRepo.java`, `src/edu/monash/repo/UsersRepo.java`, `src/edu/monash/repo/AccountsRepo.java`, `src/edu/monash/repo/OrdersRepo.java`, `src/edu/monash/app/Main.java` |

## Javadoc
All public classes and methods include Javadoc comments.  
To generate HTML docs in IntelliJ: **Tools → Generate JavaDoc** and target the `src/` directory.

---
**This is the final Task 3-compliant submission (text-based, Java, CSV, OOP).**
