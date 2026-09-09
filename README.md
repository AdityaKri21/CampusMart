# CampusMart

**Your Campus, Everything You Need.**

CampusMart is a campus-specific marketplace and commerce app built for students and campus vendors. It brings food ordering, shopping, printing services, and student-to-student resale into a single platform, with role-based access for Students, Vendors, and Admins.

Built as a full end-to-end native Android application — Kotlin backend logic, XML-based UI, and Firebase as the cloud backend.
<p align="center">
<img width="250" height="250" alt="CampusMart" src="https://github.com/user-attachments/assets/5b87880e-3b50-4e19-8241-b9e8853c37ed" />
</p>

---

## Features

### For Students
- Sign up / log in with email and password
- Browse all registered campus vendors, filterable by category (Food, Stationery, Printing, Daily Use, Misc) and searchable by name
- View a vendor's product catalog and add items to a cart
- Checkout with a choice of **Takeaway** or **Delivery** (with hostel/room address)
- View order history and track order status in real time
- Rate completed orders — ratings automatically roll up into the vendor's average rating
- Request a **Print Job** directly with a Google Drive document link, number of copies, color/B&W selection, and special instructions (for vendors in the Printing category)
- Buy and sell used items (books, electronics, furniture, cycles, etc.) in the **Student Marketplace**, including seller contact details on request
- Request elevated access (Vendor or Admin) directly from their profile
- Edit personal profile details (phone, room number, hostel block)

  
  <img width="200" height="400" alt="login" src="https://github.com/user-attachments/assets/e2895284-047f-4dc7-8e62-2da20464c821" />
  <img width="200" height="400" alt="home" src="https://github.com/user-attachments/assets/ebfeb71b-eff7-4794-b153-0854c5cbfeab" />
  <img width="200" height="400" alt="menu" src="https://github.com/user-attachments/assets/e6287354-0018-4739-a542-d28fc6302609" />
  <img width="200" height="400" alt="cart" src="https://github.com/user-attachments/assets/9a44e04e-91a6-4072-9d5f-e369d1a23e24" />
---  
  <img width="200" height="400" alt="checkout" src="https://github.com/user-attachments/assets/bb0033b4-fcc5-49a7-81dc-91d29afda393" />
  <img width="200" height="400" alt="marketplace" src="https://github.com/user-attachments/assets/49074925-0cf4-490e-9a85-2f0c7caef8ea" />
  <img width="200" height="400" alt="orderHistory" src="https://github.com/user-attachments/assets/0228f8f2-f3e3-47f8-999d-903c967779d0" />
  <img width="200" height="400" alt="profile" src="https://github.com/user-attachments/assets/38af2d10-d5e3-41ac-ab2f-f018fe5f7ef2" />


### For Vendors
- Full **Vendor Dashboard** accessible after admin approval
- Manage their own product catalog — add, edit, and delete products (including image URLs)
- Edit shop details (name, category, location, shop image)
- View and manage incoming orders, split into **Ongoing** and **Completed**
- Progress orders through a defined status lifecycle: `Placed → Accepted → Preparing → Ready → Completed`

  <img width="200" height="400" alt="vendor dashboard" src="https://github.com/user-attachments/assets/7fae6d65-1c2c-4a33-90b8-401f76726007" />
  <img width="200" height="400" alt="manageMenu" src="https://github.com/user-attachments/assets/e2177c79-fbf2-4ef8-b44d-7ee68c7b6938" />
  <img width="200" height="400" alt="ongoingOrders" src="https://github.com/user-attachments/assets/7670a0dc-64f5-42f3-a80f-564f7c3c5f31" />
  <img width="200" height="400" alt="completed Orders" src="https://github.com/user-attachments/assets/f7069945-6eb9-4af8-b32e-1a44092351c0" />


### For Admins
- Review and approve/reject incoming **Vendor access requests**
- Approving a request automatically provisions a Vendor ID and creates the corresponding vendor storefront
- (Bootstrap) First admin account is provisioned manually via Firebase Console; that admin can promote others from within the app

  <img width="200" height="400" alt="adminDashboard" src="https://github.com/user-attachments/assets/f49b2561-f920-4d13-9e38-4897f36483b3" />
  

### App-wide
- Full **light and dark mode** support across every screen
- Role-based navigation and access control throughout

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | XML layouts, View Binding, Material Components |
| Architecture | MVVM-influenced (Fragment/Activity → Repository → Firestore) |
| Navigation | Jetpack Navigation Component (single-Activity + Fragments for the main app shell) |
| Backend | Firebase Authentication, Cloud Firestore, Firebase Storage (URL-based image workaround currently in use) |
| Async | Kotlin Coroutines |
| Image Loading | Glide |
| Build System | Gradle (Kotlin DSL), Version Catalogs (`libs.versions.toml`) |

---

## Architecture

The app follows a simple layered structure:

```
UI Layer (Activities / Fragments)
        ↓
Repository Layer (AuthRepository, VendorRepository, ProductRepository,
                   OrderRepository, MarketplaceRepository, AdminRepository)
        ↓
Firebase (Authentication + Cloud Firestore)
```

- **Repositories** wrap all Firestore/Auth calls and expose suspend functions, keeping UI code free of direct Firebase calls.
- **Adapters** (`VendorAdapter`, `ProductAdapter`, `OrderAdapter`, `ListingAdapter`, `RoleRequestAdapter`) handle all RecyclerView rendering.
- **CartManager** is an in-memory singleton holding the active cart — intentionally not persisted to Firestore until checkout, since cart state is ephemeral and single-vendor by design.
- Firestore collections: `users`, `vendors` (with a `products` subcollection per vendor), `orders`, `marketplaceListings`, `roleRequests`.

---

## Project Structure

```
app/src/main/java/com/example/campusmart/
├── data/
│   ├── model/          — data classes (User, Vendor, Product, Order, CartItem, MarketplaceListing, RoleRequest)
│   ├── repository/      — Firestore/Auth logic (one repository per feature area)
│   ├── cart/            — CartManager (in-memory singleton)
│   └── util/            — CategoryMapper (shared category-bucketing logic)
├── ui/
│   ├── home/             — vendor browsing, search, category filters
│   ├── vendordetail/     — product list / print request screen
│   ├── cart/             — cart screen
│   ├── orders/           — student order history + ratings
│   ├── marketplace/       — buy/sell listings
│   ├── profile/           — profile + role requests
│   └── adapter/           — all RecyclerView adapters
├── LoginActivity.kt / SignupActivity.kt
├── MainActivity.kt                  — hosts bottom navigation
├── AdminDashboardActivity.kt
├── VendorDashboardActivity.kt
├── ManageProductsActivity.kt
├── VendorOrdersActivity.kt
└── CheckoutActivity.kt
```

---

## Getting Started

### Prerequisites
- Android Studio (recent stable version)
- A Firebase project with **Authentication** (Email/Password enabled), **Cloud Firestore**, and **Cloud Storage** set up

### Setup
1. Clone this repository
2. Open in Android Studio and let Gradle sync
3. Create your own `google-services.json` via the Firebase Console (Project Settings → Your apps) and place it in the `app/` directory — this file is intentionally excluded from version control
4. Set Firestore to test mode for local development, or configure Security Rules (see note below)
5. Run the app

### Bootstrapping the first Admin account
Sign up normally through the app, then manually change that user's `role` field from `"student"` to `"admin"` in the Firestore Console (`users` collection). All subsequent admins can be promoted from within the app via the role-request approval flow.

---

## Known Limitations / Roadmap

- **Firestore Security Rules**: currently running in test mode during development. Production use requires proper role-based rules before deployment.
- **Image hosting**: product/vendor/listing images currently use externally-hosted URLs (e.g. Postimages, Imgur) pasted as plain text, rather than native in-app upload via Firebase Storage — a deliberate workaround while on Firebase's free Spark plan, which no longer supports Storage. Switching to Blaze (pay-as-you-go, free at this app's scale) would unlock native image picking and upload.
- **Push notifications**: Firebase Cloud Messaging dependency is included but not yet wired up for order/approval notifications.
- **Product-level search**: current search matches vendor name/category only, not individual products within a vendor's catalog.

---

## License

This project was built as a personal learning/portfolio project.
