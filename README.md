
An **offline Android application** designed to help Ethiopian households manage shared expenses fairly, avoid forgotten payments, and reduce financial conflicts.

---

## 📌 Overview

In Ethiopia, shared living is common among families, students, and workers. Managing rent, utilities, and other shared expenses can be challenging.  
This app provides a **simple, offline-first solution** for splitting expenses, tracking payments, and setting reminders—without requiring internet access or user accounts.

---

## ✨ Features

- 👥 **Household Member Management**
  - Add and remove members
  - Easy identification using names and icons

- 💸 **Expense Management**
  - Track Rent, Electricity, Water, Food, WiFi, and Other expenses
  - Edit or delete existing expenses

- ⚖️ **Automatic Expense Splitting**
  - Equal cost splitting among all members
  - Eliminates manual calculations

- ⏰ **Payment Reminders**
  - Alarm and notification reminders for due dates
  - Helps prevent late payments and penalties

- 📊 **Monthly Expense Summary**
  - Clear overview of household expenses and balances

- 📴 **Fully Offline**
  - No internet connection required
  - Works anytime, anywhere

---

## 🗂️ Storage Type

- **Local Device Storage (Offline Storage)**
- Uses a custom `DataStorage` utility class
- Data is stored securely on the user's device
- No cloud, server, or online database involved

❌ No Firebase  
❌ No SQLite / Room  
❌ No user login required  

---

## 🛠 Technology Stack

- **Platform:** Android
- **Programming Language:** Java
- **UI Design:** XML
- **Architecture:** Activity-based
- **Notifications:** AlarmManager
- **Storage:** Local internal storage (SharedPreferences / file-based)

---

## 🎯 Target Users

- 👨‍👩‍👧 Families
- 🎓 University students
- 🧑‍💼 Workers sharing rental houses
- 🏠 Roommates and friends

---

## ⚠️ Challenges Faced

- Offline-only storage limits backup and multi-device access
- Equal expense splitting only (no custom splits)
- Android-only platform
- Manual data entry may cause errors
- No cloud sync or authentication

---

## 🚀 Future Improvements

- ☁️ Cloud backup and data synchronization
- 🔐 User accounts and role-based access
- 📐 Custom expense splitting (percentage-based)
- 📈 Advanced analytics and reports
- 🌍 Multi-language support (Amharic, Afaan Oromo, Tigrinya)
- 📱 iOS and Web version
- 💳 Integration with local mobile payments (e.g., Telebirr)

---

## 📱 Screenshots
*(Add screenshots here if available)*

---

## 📄 License

This project is developed for **educational purposes**.

---

## 🙌 Author

**Kidist Tadesse**  
Android Developer | Student Project

