# Connect CMA - Complete API Reference

**Quick Reference Guide for All API Endpoints**  
*Organized by Business Process with Request/Response Examples*

---

## 📋 **QUICK REFERENCE INDEX**

| Process | Endpoints | Description |
|---------|-----------|-------------|
| [🔐 Authentication](#-authentication--onboarding) | 12 endpoints | Registration, login, verification, applications |
| [👤 Profile Management](#-profile-management) | 8 endpoints | User profiles, photos, preferences |
| [📅 Daily Matching](#-daily-batch-matching) | 8 endpoints | Daily batches, match discovery, actions |
| [💬 Conversations](#-conversations--messaging) | 10 endpoints | Chat management, messaging, read status |
| [🚫 Safety & Moderation](#-safety--moderation) | 4 endpoints | Report, block, unmatch functionality |
| [⚙️ Admin Management](#-admin--management) | 12 endpoints | Application review, user management |
| [🔧 System](#-system--configuration) | 2 endpoints | Health check, app configuration |

**Base URL**: `https://api.datingapp.com/v1`  
**Authentication**: `Authorization: Bearer {jwt_token}` (required for most endpoints)

---

## 🔐 **AUTHENTICATION & ONBOARDING**

### **User Registration**
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123",
  "confirmPassword": "securePassword123", // todo: we dont need this
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-01",
  "gender": "Male",
  "location": "San Francisco, CA"
}

→ {
  "token": "jwt_token_here"
} //done
```

### **Email Verification Flow**
```http
POST /api/auth/send-verification-code
{
  "email": "user@example.com",
  "userName": "John Doe"
} // this now a paramter 

POST /api/auth/verify-email-code
{
  "email": "user@example.com",
  "code": "123456"
} // this is now a param 

POST /api/auth/resend-verification-code
{
  "email": "user@example.com",
  "userName": "John Doe"
} // cma need to rewaire to just trigger send verification code again

//done 
```

### **Login & Session**
```http
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "securePassword123"
} // needs to be made a query param

→ {
  "success": true,
  "token": "jwt_token_here",
  "refreshToken": "refresh_token_here",
  "user": {
    "id": "user_123",
    "email": "user@example.com",
    "name": "John Doe"
  }
} // why do we need this data? 

POST /api/auth/refresh
{
  "refreshToken": "refresh_token_here"
}

POST /api/auth/logout
```

### **Password Management**
```http
POST /api/auth/forgot-password
POST /api/auth/reset-password  
POST /api/auth/verify-reset-token
PUT /api/auth/change-password
```

### **Application Process**
```http
POST /api/applications/submit
{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-01",
  "gender": "Male",
  "location": "San Francisco, CA",
  "jobTitle": "Software Engineer",
  "industry": "Technology",
  "applicationNotes": "Looking for serious relationship",
  "photoUrls": ["https://cdn.example.com/photo1.jpg"]
}

GET /api/applications/status/{connectId}
POST /api/applications/status/by-email
```

---

## 👤 **PROFILE MANAGEMENT**

### **Get User Profile**
```http
GET /api/users/me?includePreferences=true
Authorization: Bearer {token}

→ {
  "id": "user_123",
  "name": "John Doe", 
  "age": 28,
  "bio": "Love hiking, coffee, and meaningful conversations",
  "photos": [
    {
      "id": "photo_1",
      "url": "https://cdn.example.com/photo1.jpg",
      "isPrimary": true,
      "order": 1
    }
  ],
  "location": "San Francisco, CA",
  "interests": ["Hiking", "Coffee", "Photography"],
  "profile": {
    "pronouns": "he/him",
    "gender": "Male",
    "sexuality": "Straight",
    "interestedIn": "Women",
    "jobTitle": "Software Engineer",
    "company": "Tech Startup",
    "university": "UC Berkeley",
    "educationLevel": "Bachelor's Degree",
    "religiousBeliefs": "Agnostic",
    "hometown": "Los Angeles, CA",
    "politics": "Liberal",
    "datingIntentions": "Serious relationship",
    "relationshipType": "Monogamous",
    "height": "6'0\"",
    "ethnicity": "Mixed",
    "children": "No kids",
    "familyPlans": "Want kids someday",
    "pets": "Dog person",
    "zodiacSign": "Aquarius"
  },
  "writtenPrompts": [
    {
      "prompt": "My simple pleasures",
      "answer": "Weekend farmers market visits and sunset hikes"
    }
  ],
  "pollPrompts": [
    {
      "prompt": "Best first date idea",
      "question": "What sounds perfect?", 
      "options": ["Coffee", "Museum", "Food trucks", "Hiking"],
      "selectedOption": "Coffee"
    }
  ],
  "fieldVisibility": {
    "jobTitle": true,
    "religiousBeliefs": false
  },
  "preferences": {
    "preferredGender": "women",
    "minAge": 24,
    "maxAge": 32,
    "minHeight": 60,
    "maxHeight": 72,
    "datingIntention": "serious",
    "drinkingPreference": "sometimes",
    "smokingPreference": "never"
  }
}
```

### **Profile Operations**
```http
GET /api/users/{userId}
PUT /api/users/{userId}
DELETE /api/users/{userId}
GET /api/profile
PUT /api/profile/preferences
POST /api/profile/refresh-photo-urls
GET /api/users/search
```

### **Photo Management**
```http
POST /api/users/me/photos
Content-Type: multipart/form-data
Form Data: photo=[file], isPrimary=false, order=3

→ {
  "success": true,
  "photo": {
    "id": "photo_7",
    "url": "https://cdn.example.com/photos/7.jpg",
    "isPrimary": false,
    "order": 3
  }
}

DELETE /api/users/me/photos/{photoId}
```

---

## 📅 **DAILY BATCH MATCHING**

### **Batch Status & Countdown**
```http
GET /api/matches/daily/status
Authorization: Bearer {token}

→ {
  "currentTime": "2024-01-15T14:30:00Z",
  "nextBatchTime": "2024-01-15T19:00:00Z",
  "todaysBatch": {
    "date": "2024-01-15",
    "available": false,
    "availableAt": "2024-01-15T19:00:00Z",
    "totalMatches": 10,
    "reviewed": 0,
    "remaining": 10
  },
  "yesterdaysBatch": {
    "date": "2024-01-14",
    "completed": true,
    "totalMatches": 8,
    "reviewed": 8,
    "mutualMatches": 2
  }
}
```

### **Get Daily Matches**
```http
GET /api/discovery/matches/latest
Authorization: Bearer {token}

→ {
  "success": true,
  "users": [
    {
      "connectId": "user_456",
      "firstName": "Sarah",
      "lastName": "Johnson",
      "age": 26,
      "location": "San Francisco, CA",
      "photos": [
        {
          "url": "https://cdn.example.com/sarah1.jpg",
          "isPrimary": true
        }
      ],
      "interests": ["Yoga", "Photography", "Travel"],
      "bio": "Adventure seeker with a passion for capturing beautiful moments",
      "writtenPrompts": [
        {
          "prompt": "My travel bucket list",
          "response": "Japan for cherry blossoms, Iceland for northern lights"
        }
      ]
    }
  ],
  "matchSetId": "set_789",
  "totalUsers": 10,
  "completed": false
}
```

### **Submit Match Actions (Batched)**
```http
POST /api/discovery/matches/actions
{
  "matchSetId": "set_789",
  "actions": [
    {
      "targetUserId": "user_456",
      "action": "LIKE",
      "timestamp": "2024-01-15T20:15:00Z"
    },
    {
      "targetUserId": "user_789", 
      "action": "PASS",
      "timestamp": "2024-01-15T20:16:00Z"
    }
  ]
}

→ {
  "success": true,
  "processedActions": 2,
  "mutualMatches": [
    {
      "matchId": "match_abc",
      "user": {
        "id": "user_456",
        "name": "Sarah Johnson"
      },
      "matchedAt": "2024-01-15T20:15:00Z",
      "conversationId": "conv_xyz"
    }
  ]
}
```

### **Batch Results**
```http
GET /api/matches/daily/{date}/results
GET /api/discovery/matches/status
GET /api/discovery/matches/countdown
GET /api/discovery/matches/history
```

### **Individual Match Actions**
```http
POST /api/users/{userId}/like
POST /api/users/{userId}/dislike  
POST /api/users/{userId}/pass
```

---

## 💬 **CONVERSATIONS & MESSAGING**

### **Get Conversations**
```http
GET /api/conversations?includeArchived=false
Authorization: Bearer {token}

→ {
  "success": true,
  "conversations": [
    {
      "id": "conv_xyz",
      "matchId": "match_abc",
      "otherUser": {
        "id": "user_456",
        "name": "Sarah Johnson",
        "photos": [
          {
            "url": "https://cdn.example.com/sarah1.jpg",
            "isPrimary": true
          }
        ]
      },
      "lastMessage": {
        "id": "msg_999",
        "content": "Thanks for the like! 😊",
        "senderId": "user_456", 
        "sentAt": "2024-01-15T21:45:00Z"
      },
      "unreadCount": 1,
      "matchedAt": "2024-01-15T20:15:00Z"
    }
  ]
}
```

### **Messaging**
```http
GET /api/conversations/{conversationId}/messages?page=0&limit=50

→ {
  "success": true,
  "messages": [
    {
      "id": "msg_999",
      "senderId": "user_456",
      "content": "Thanks for the like! 😊",
      "sentAt": "2024-01-15T21:45:00Z"
    }
  ]
}

POST /api/conversations/{conversationId}/messages
{
  "content": "Hi! Great to match with you 👋"
}

→ {
  "success": true,
  "messageData": {
    "id": "msg_1000",
    "senderId": "user_123",
    "content": "Hi! Great to match with you 👋",
    "sentAt": "2024-01-15T22:00:00Z"
  }
}
```

### **Conversation Management**
```http
GET /api/conversations/{conversationId}
POST /api/conversations/create
POST /api/conversations/{conversationId}/archive
POST /api/conversations/{conversationId}/unarchive
POST /api/conversations/{conversationId}/read
GET /api/conversations/stats
```

---

## 🚫 **SAFETY & MODERATION**

### **User Safety Actions**
```http
POST /api/users/{userId}/report
{
  "reason": "inappropriate_content",
  "details": "User sent inappropriate messages"
}

→ {
  "success": true,
  "reportId": "report_123456",
  "message": "User reported successfully"
}

POST /api/users/{userId}/block
→ {
  "success": true,
  "message": "User blocked successfully"
}
```

### **Conversation Safety**
```http
POST /api/conversations/{conversationId}/unmatch
→ {
  "success": true,
  "message": "Conversation ended successfully"
}

POST /api/matches/{matchId}/report
{
  "reason": "inappropriate_behavior",
  "details": "Details about the issue"
}
```

---

## ⚙️ **ADMIN & MANAGEMENT**

### **Application Review**
```http
GET /api/admin/applications/pending
Authorization: Bearer {admin_token}

→ {
  "success": true,
  "applications": [
    {
      "id": "app_123456",
      "email": "newuser@example.com",
      "firstName": "Jane",
      "lastName": "Smith",
      "dateOfBirth": "1995-03-15",
      "gender": "Female",
      "location": "New York, NY",
      "jobTitle": "Marketing Manager",
      "industry": "Advertising",
      "photoUrls": ["https://cdn.example.com/temp/photo1.jpg"],
      "submittedAt": "2024-01-15T10:00:00Z",
      "status": "PENDING"
    }
  ]
}

PUT /api/admin/applications/{applicationId}/approve
{
  "notes": "Great profile, approved for membership"
}

PUT /api/admin/applications/{applicationId}/reject
{
  "rejectionReason": "incomplete_profile",
  "notes": "Please provide additional photos"
}
```

### **User Management**
```http
GET /api/admin/users
GET /api/admin/users/{userId}
PUT /api/admin/users/{userId}
POST /api/admin/users/{userId}/suspend
POST /api/admin/users/{userId}/reactivate
DELETE /api/admin/users/{userId}
```

### **Admin Operations**
```http
GET /api/admin/applications/all
GET /api/admin/applications/stats
GET /api/admin/applications/{applicationId}
POST /api/admin/setup/create-admin
GET /api/admin/stats/demographics
```

---

## 🔧 **SYSTEM & CONFIGURATION**

### **Health Check**
```http
GET /api/health
→ {
  "status": "healthy",
  "timestamp": "2024-01-15T22:00:00Z",
  "version": "1.2.3",
  "services": {
    "database": "healthy",
    "cache": "healthy",
    "storage": "healthy"
  }
}
```

### **App Configuration**
```http
GET /api/config
Authorization: Bearer {token}

→ {
  "features": {
    "dailyBatchEnabled": true,
    "conversationsEnabled": true,
    "photoUploadEnabled": true
  },
  "limits": {
    "maxPhotos": 6,
    "maxBioLength": 500,
    "maxMessageLength": 1000
  },
  "batchConfig": {
    "deliveryTime": "19:00",
    "batchSize": 10,
    "timezone": "user_local"
  }
}
```

---

## 🔄 **ERROR RESPONSES**

### **Standard Error Format**
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request data",
    "details": {
      "field": "age",
      "reason": "Must be between 18 and 100"
    }
  },
  "timestamp": "2024-01-15T22:00:00Z",
  "requestId": "req_abc123"
}
```

### **Common Error Codes**
| Code | Description | HTTP Status |
|------|-------------|-------------|
| `BATCH_NOT_READY` | Daily batch not yet available | 403 |
| `BATCH_EXPIRED` | Daily batch has expired | 410 |
| `USER_NOT_FOUND` | Target user doesn't exist | 404 |
| `CONVERSATION_NOT_FOUND` | Conversation doesn't exist | 404 |
| `RATE_LIMITED` | Too many requests | 429 |
| `UNAUTHORIZED` | Invalid/expired token | 401 |
| `VALIDATION_ERROR` | Request validation failed | 400 |
| `INSUFFICIENT_PHOTOS` | Minimum photos required | 400 |
| `PROFILE_INCOMPLETE` | Missing required fields | 400 |

---

## 🏗 **IMPLEMENTATION NOTES**

### **Caching Patterns**
- **User Profiles**: Cache-first (24 hours)
- **Daily Batches**: Time-restricted + cache (12 hours)
- **Match Actions**: Collect locally → batch send
- **Safety Actions**: Immediate cache + background API
- **Conversations**: Real-time with cache fallback

### **Time Restrictions**
- Daily batch endpoints only available after 7PM local time
- Batch results accessible after completion
- Countdown calculated client-side

### **Authentication**
- JWT tokens required for all user endpoints
- Admin endpoints require admin-level tokens
- Refresh tokens for session management

### **Rate Limits**
- API requests: 100/minute per user
- Photo uploads: 10/hour per user
- Reports: 5/day per user
- Messages: 10/minute per conversation

---

## 📊 **SUMMARY STATS**

- **Total Endpoints**: 72
- **Business Processes**: 7
- **Request Classes**: 45+ in codebase
- **Response Models**: 25+ defined
- **Authentication Required**: 68/72 endpoints
- **Admin Only**: 12 endpoints
- **Public Endpoints**: 4 (health, some auth)

---

**Last Updated**: December 2024  
**API Version**: v1  
**Documentation Version**: 1.0.0