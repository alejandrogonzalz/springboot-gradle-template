# 🔒 Single Session Authentication Strategy

## Overview

This application uses a **Single Session Only** authentication strategy for maximum security. Users can only be logged in on **ONE device at a time**.

---

## 🎯 How It Works

### **Login Flow:**
```
1. User logs in from Device A
   ├─ Delete all existing refresh tokens for this user
   └─ Create new refresh token for Device A

2. User logs in from Device B (while still logged in on Device A)
   ├─ Delete all existing refresh tokens (Device A token deleted)
   └─ Create new refresh token for Device B

3. Device A tries to refresh access token
   └─ ❌ 401 UNAUTHORIZED - "Invalid refresh token"
```

---

## 📊 Database State

### **Before Login (Clean State):**
```sql
SELECT * FROM refresh_tokens WHERE user_id = 1;
-- Empty result
```

### **After Login from Laptop:**
```sql
SELECT * FROM refresh_tokens WHERE user_id = 1;

| id | token              | user_id | expires_at          | created_at          |
|----|-------------------|---------|---------------------|---------------------|
| 1  | eyJhbGciOiJIUz...  | 1       | 2025-12-20 10:00:00 | 2025-12-13 10:00:00 |
```

### **After Login from Phone (Laptop kicked out):**
```sql
SELECT * FROM refresh_tokens WHERE user_id = 1;

| id | token              | user_id | expires_at          | created_at          |
|----|-------------------|---------|---------------------|---------------------|
| 2  | eyJhbGciOiJIUz...  | 1       | 2025-12-20 11:30:00 | 2025-12-13 11:30:00 |
-- Laptop token (id=1) was deleted!
```

---

## 🔄 User Experience

### **Scenario 1: Normal Login**
```
👤 User: Logs in on Laptop
✅ Success: Can use the application
```

### **Scenario 2: Login from Another Device**
```
👤 User: Logs in on Phone

📱 Phone: ✅ Works normally
💻 Laptop: ❌ Next API call returns 401 Unauthorized
```

**What the user sees on Laptop:**
```json
{
  "success": false,
  "message": "Authentication required. Please provide a valid token.",
  "timestamp": "2025-12-13T11:30:00.123456Z"
}
```

**Frontend should:**
1. Detect 401 error
2. Clear local tokens
3. Redirect to login page
4. Show message: "You've been logged out because you signed in from another device"

---

## 🚨 What Happens to Access Tokens?

### **Important:** Access tokens are NOT invalidated!

```
User logs in on Laptop:
├─ Access Token: Valid for 15 minutes ✅
└─ Refresh Token: Valid for 7 days ✅

User logs in on Phone (5 minutes later):
├─ Laptop Refresh Token: ❌ DELETED
└─ Laptop Access Token: ✅ STILL VALID for 10 more minutes!

After 15 minutes:
└─ Laptop Access Token: ❌ EXPIRED
    └─ Laptop tries to refresh → 401 (refresh token deleted)
```

**This means:**
- 🔓 User may have up to 15 minutes of parallel access
- 🔒 After access token expires, only the latest login works

---

## 💡 Implementation Details

### **Code Location:**
```java
// src/main/java/com/example/backend/user/service/AuthenticationService.java

@Transactional
public AuthenticationResponse login(LoginRequest request) {
    // ... authentication ...

    // 🔒 SINGLE SESSION: Delete all existing refresh tokens
    refreshTokenRepository.deleteByUser(user);

    // Create new refresh token
    RefreshToken refreshTokenEntity = RefreshToken.builder()
        .token(refreshToken)
        .user(user)
        .expiresAt(Instant.now().plusSeconds(604800)) // 7 days
        .build();
    refreshTokenRepository.save(refreshTokenEntity);

    // ...
}
```

### **Database Constraint:**
```sql
-- Each user can have only ONE active refresh token at a time
-- Enforced by application logic (deleteByUser before insert)
```

---

## 🔐 Security Benefits

### **✅ Advantages:**
1. **Maximum Security** - Prevents account sharing
2. **Session Control** - User always knows where they're logged in
3. **Simple** - No complex session management
4. **Clear Audit Trail** - One token = one active session

### **❌ Limitations:**
1. **UX Impact** - Users get kicked out when logging in elsewhere
2. **No Multi-Device** - Can't use phone and laptop simultaneously
3. **Access Token Gap** - Up to 15 minutes of parallel access

---

## 📱 Frontend Implementation

### **React Example:**
```javascript
import axios from 'axios';

// Set up axios interceptor to handle 401
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Clear tokens
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');

      // Show friendly message
      toast.error('You were logged out because you signed in from another device.');

      // Redirect to login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### **Vue Example:**
```javascript
// main.js or axios-config.js
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.clear();
      router.push('/login');
      ElMessage.error('Session expired. Please log in again.');
    }
    return Promise.reject(error);
  }
);
```

---

## 🧪 Testing Scenarios

### **Test 1: Single Device Login**
```bash
# Login from Laptop
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"User123!"}'

# Save accessToken and refreshToken
# ✅ Can use API normally
```

### **Test 2: Login from Second Device**
```bash
# Login from Phone (same user)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"User123!"}'

# New tokens received
# ✅ Phone works
# ❌ Laptop's refresh token is now invalid
```

### **Test 3: Laptop Tries to Refresh**
```bash
# Try to refresh with Laptop's old refresh token
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Cookie: refreshToken=<old-laptop-token>"

# Response: 401 Unauthorized
{
  "success": false,
  "message": "Invalid refresh token"
}
```

---

## 🔄 Logout Behavior

### **Logout deletes the current session:**
```bash
POST /api/v1/auth/logout
Cookie: refreshToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Deletes refresh token from database
# User must login again
```

**After logout:**
- ✅ Refresh token deleted from database
- ❌ Access token still valid for remaining time (up to 15 min)
- ❌ Cannot get new access token (refresh token deleted)

---

## 📊 Comparison with Other Strategies

| Feature | Single Session | Multi-Session (5) | Unlimited |
|---------|---------------|-------------------|-----------|
| **Devices** | 1 | 5 | ∞ |
| **Security** | ⭐⭐⭐⭐⭐ Highest | ⭐⭐⭐⭐ High | ⭐⭐⭐ Medium |
| **UX** | ⭐⭐ Poor | ⭐⭐⭐⭐ Good | ⭐⭐⭐⭐⭐ Best |
| **Use Case** | Banking, High-Security | Consumer Apps | Developer Tools |
| **Tokens per User** | 1 | 5 | Unlimited |
| **DB Growth** | Minimal | Controlled | Grows over time |

---

## ⚙️ Configuration

### **Token Lifetimes:**
```yaml
# src/main/resources/application.yml
jwt:
  expiration: 900000        # 15 minutes (access token)
  refresh-expiration: 604800000  # 7 days (refresh token)
```

### **To Change Strategy:**
Simply remove or modify this line in `AuthenticationService.login()`:
```java
// Remove this line to allow multiple sessions:
refreshTokenRepository.deleteByUser(user);
```

---

## 🎯 Summary

**Current Implementation: Single Session Only**

- ✅ User can login from ONE device only
- ✅ New login kicks out previous session
- ✅ Maximum security (banking-level)
- ✅ Simple implementation
- ❌ No multi-device support
- ❌ Users may be confused when kicked out

**Perfect for:** Banking apps, high-security systems, admin panels

**Not ideal for:** Social media, streaming services, casual apps
