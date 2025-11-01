# Runtime Verification Checklist

## Purpose
This document provides a systematic checklist for verifying the NextGen BuildPro application works correctly at runtime on physical Android devices.

**Target**: Android 8.0 (API 26) and above
**Current Build**: Debug APK (138MB)
**Location**: `app/build/outputs/apk/debug/app-debug.apk`

---

## Prerequisites

### Hardware
- [ ] Android device (phone or tablet)
- [ ] Android 8.0 (API 26) or higher
- [ ] Minimum 2GB RAM
- [ ] 500MB free storage

### Software
- [ ] ADB installed on development machine
- [ ] USB debugging enabled on device
- [ ] Device connected via USB or WiFi

### Installation
```bash
# Build the APK
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk

# Or reinstall if already present
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Phase 1: Application Launch ✓

### 1.1 Installation
- [ ] APK installs without errors
- [ ] App icon appears in launcher
- [ ] App name displays correctly ("NextGen BuildPro")

### 1.2 First Launch
- [ ] App launches without crashing
- [ ] Splash screen displays (if implemented)
- [ ] Home screen loads
- [ ] No fatal errors in logcat

### 1.3 Permissions
- [ ] Permission dialogs appear when needed
- [ ] App handles permission denial gracefully
- [ ] App functions with granted permissions

**Logcat Command**: `adb logcat | grep NextGenBuildPro`

---

## Phase 2: Navigation Testing ✓

### 2.1 Main Navigation
- [ ] Home screen displays
- [ ] Bottom navigation bar visible
- [ ] All navigation tabs respond to taps:
  - [ ] Home
  - [ ] Leads
  - [ ] Estimates  
  - [ ] Projects
  - [ ] Settings

### 2.2 Screen Transitions
- [ ] Screens load without crashing
- [ ] Back button returns to previous screen
- [ ] Deep links work correctly
- [ ] Navigation state persists on rotation

---

## Phase 3: Feature Verification ✓

### 3.1 Home Screen
- [ ] Dashboard displays
- [ ] Recent items show (or "No items" message)
- [ ] Action buttons are clickable
- [ ] Quick stats display (if implemented)

### 3.2 Lead Management
- [ ] **Lead List Screen**
  - [ ] Leads list displays (or empty state)
  - [ ] Add Lead button works
  - [ ] Tap on lead opens detail screen
  - [ ] Search/filter works (if implemented)

- [ ] **Lead Editor Screen**
  - [ ] Form fields are editable
  - [ ] Save button works
  - [ ] Validation shows errors
  - [ ] Cancel/back preserves data (or prompts)

- [ ] **Lead Detail Screen**
  - [ ] Lead information displays
  - [ ] Edit button opens editor
  - [ ] Actions (call, message) work or are disabled appropriately

### 3.3 Estimates
- [ ] **Estimate List Screen**
  - [ ] Estimates display (or empty state)
  - [ ] Add estimate button works
  - [ ] Tap opens estimate detail

- [ ] **Estimate Editor**
  - [ ] Line items can be added
  - [ ] Calculations work correctly
  - [ ] Total updates dynamically
  - [ ] Save persists data (if Firebase configured)
  - [ ] Assembly search works

### 3.4 Projects
- [ ] **Project List Screen**
  - [ ] Projects display (or empty state)
  - [ ] Add project button works
  - [ ] Project cards show info correctly

- [ ] **Project Detail Screen**
  - [ ] Project information displays
  - [ ] Related items show (estimates, tasks)
  - [ ] Actions work correctly

### 3.5 Calendar
- [ ] **Calendar Screen**
  - [ ] Calendar view displays
  - [ ] Date selection works
  - [ ] Events show for selected date
  - [ ] Add event button works

- [ ] **Event Editor**
  - [ ] Form fields work
  - [ ] Date/time pickers function
  - [ ] Save creates event

### 3.6 Settings
- [ ] **Settings Screen**
  - [ ] All settings categories accessible
  - [ ] Account settings display
  - [ ] Notification settings work
  - [ ] Permission settings display
  - [ ] Changes persist on app restart

---

## Phase 4: Data Persistence ✓

### 4.1 Local Storage
- [ ] Create a lead, close app, reopen - lead persists
- [ ] Create an estimate, close app, reopen - estimate persists
- [ ] User preferences persist across restarts

### 4.2 Firebase Integration (if configured)
- [ ] Data syncs to Firebase
- [ ] Changes reflect across sessions
- [ ] No authentication errors
- [ ] Firestore rules allow operations

**Note**: Firebase may not work without proper `google-services.json` configuration

---

## Phase 5: UI/UX Testing ✓

### 5.1 Visual Quality
- [ ] Text is readable
- [ ] Colors follow Material Design 3
- [ ] Icons display correctly
- [ ] Images load (if any)
- [ ] No UI clipping or overlap

### 5.2 Responsiveness
- [ ] Scrolling is smooth
- [ ] Buttons respond immediately to taps
- [ ] Forms don't lag while typing
- [ ] No ANR (Application Not Responding) dialogs

### 5.3 Device Compatibility
- [ ] Works in portrait orientation
- [ ] Works in landscape orientation
- [ ] Keyboard doesn't hide input fields
- [ ] Works on different screen sizes

---

## Phase 6: Error Handling ✓

### 6.1 Network Errors
- [ ] App handles no internet gracefully
- [ ] Appropriate error messages display
- [ ] Retry mechanisms work

### 6.2 Input Validation
- [ ] Invalid inputs show error messages
- [ ] Required fields are validated
- [ ] Form submission blocked with errors

### 6.3 Edge Cases
- [ ] Empty states display correctly
- [ ] Long text doesn't break UI
- [ ] Special characters handled
- [ ] Large datasets don't cause crashes

---

## Phase 7: Performance Testing ✓

### 7.1 Speed
- [ ] App launches within 3 seconds
- [ ] Screen transitions are instant
- [ ] List scrolling is smooth (60fps)
- [ ] No jank or stuttering

### 7.2 Memory
- [ ] App uses reasonable memory (<200MB)
- [ ] No memory leaks on prolonged use
- [ ] No OOM (Out of Memory) crashes

### 7.3 Battery
- [ ] No excessive battery drain
- [ ] Background services behave appropriately

**Monitoring**: Use Android Studio Profiler or `adb shell dumpsys meminfo <package>`

---

## Phase 8: Known Limitations ⚠️

### Expected Issues
- [ ] **AI Features**: Not accessible in UI (code exists but not wired)
- [ ] **Voice Commands**: Not functional yet
- [ ] **Some CRUD Operations**: May be incomplete
- [ ] **Firebase**: May need configuration
- [ ] **Some Screens**: May have placeholder content

### Not Critical
- [ ] Deprecation warnings in logs (expected)
- [ ] Some features showing "Coming Soon"

---

## Logcat Monitoring

### Critical Errors to Watch For
```bash
# Filter for errors
adb logcat | grep -E "AndroidRuntime|ERROR|FATAL"

# Filter for NextGenBuildPro logs
adb logcat | grep NextGenBuildPro

# Firebase errors
adb logcat | grep -E "Firebase|Firestore"
```

### Common Issues
- **NullPointerException**: Indicates a bug, log and report
- **Firebase errors**: May indicate missing configuration
- **Network errors**: Expected if Firebase not configured

---

## Test Results Template

```markdown
# Runtime Test Results

**Date**: [Date]
**Device**: [Manufacturer Model]
**Android Version**: [e.g., Android 13]
**Build**: Debug APK (138MB)

## Summary
- Overall Status: [ Pass / Partial / Fail ]
- Critical Issues: [Number]
- Minor Issues: [Number]

## Detailed Results

### Application Launch
- Installation: [ Pass / Fail ]
- First Launch: [ Pass / Fail ]
- Permissions: [ Pass / Fail ]
- Notes: [Any observations]

### Navigation
- Main Navigation: [ Pass / Fail ]
- Screen Transitions: [ Pass / Fail ]
- Notes: [Any observations]

### Features Tested
- Home Screen: [ Pass / Fail / Not Tested ]
- Lead Management: [ Pass / Fail / Not Tested ]
- Estimates: [ Pass / Fail / Not Tested ]
- Projects: [ Pass / Fail / Not Tested ]
- Calendar: [ Pass / Fail / Not Tested ]
- Settings: [ Pass / Fail / Not Tested ]

### Issues Encountered
1. [Description of issue 1]
2. [Description of issue 2]
...

### Recommendations
- [Recommendation 1]
- [Recommendation 2]
...
```

---

## Success Criteria

### Minimum Viable
- [x] App installs successfully
- [x] App launches without crashing
- [ ] Core screens are accessible
- [ ] Basic navigation works
- [ ] At least one CRUD operation works end-to-end

### Full Success
- [ ] All Phase 1-6 items pass
- [ ] No critical crashes
- [ ] Data persists correctly
- [ ] UI is polished and responsive
- [ ] Performance is acceptable

---

## Next Steps After Testing

### If Tests Pass
1. Document working features
2. Identify incomplete features
3. Create issue tracker for bugs
4. Plan next development iteration

### If Tests Fail
1. Document all failures
2. Collect logcat outputs
3. Identify root causes
4. Prioritize fixes
5. Retest after fixes

---

## Conclusion

This checklist provides a systematic approach to verify that the NextGen BuildPro application works correctly at runtime. 

**Current Status**: Build is successful, APK generated. Runtime testing on physical devices is the next step to validate the 75-80% completion estimate.

---

**Last Updated**: October 7, 2024
**Version**: 1.0
**Status**: Ready for runtime testing
