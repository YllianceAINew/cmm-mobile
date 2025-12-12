# Project overview
_Exported on 12/12/2025 at 10:59:47 EST from Cursor (2.2.17)_

---

**User**

can you tell me about this project?

---

**Cursor**


Exploring the project structure to understand what it is.



Reviewing key files to understand the project better.



Checking a few more files to understand the project's functionality:



## Project Overview

**MultimediaChat** — Android multimedia chat app with messaging, voice, and video calling.

### Key Features

1. **Messaging**
   - XMPP-based instant messaging
   - Group chat rooms
   - File and image sharing
   - QR code scanning
   - Encrypted database (SQLCipher)

2. **Voice & Video Calling**
   - VoIP via Linphone
   - Video calls
   - Audio calls
   - Incoming/outgoing call screens

3. **Social Features**
   - Contact management
   - Friend requests
   - Profile management
   - Tags/categories for contacts
   - Mobile contacts integration

4. **Media**
   - Camera integration
   - Photo gallery
   - Image cropping
   - Video compression (custom module)
   - Wallpaper customization

### Technical Stack

- **Platform**: Android (minSdk 18, targetSdk 28)
- **Build System**: Gradle 3.4.0
- **Architecture**: Multi-module project
  - `multimediachat` (main app)
  - `android-async-http` (HTTP client)
  - `videocompress` (video compression with native code)

### Key Libraries & Technologies

- **Linphone**: VoIP/video calling
- **XMPP (aSmack)**: Messaging protocol
- **SQLCipher**: Encrypted database
- **Glide**: Image loading
- **Native Libraries**: Video codecs (H.264, AMR, G.729, Silk, WebRTC, etc.)
- **FFmpeg**: Media processing

### Project Structure

- **535+ Java files** in the main module
- **Native code** (C/C++) for video compression and media processing
- **Multiple architectures** supported (armeabi, armeabi-v7a)
- **AIDL interfaces** for inter-process communication

The app appears production-ready with a built APK (`MultimediaChat1.0.apk`) in the release folder.

---

**User**

As you know, it has poor UI design and I'm going to improve the UI beautifully with beautiful colors.
Can you tell me how to improve UI and UX of my entire project's activities? 

---

**Cursor**


Reviewing the current UI implementation to identify improvements.



Examining layout files to understand the current UI structure:



Reviewing key layout files:



Creating a UI/UX improvement guide with a modern color palette and implementation steps.


```markdown
# UI/UX Improvement Guide for MultimediaChat

## Overview
This guide provides a comprehensive plan to modernize the UI/UX of your Android chat application with beautiful colors and Material Design principles.

---

## 🎨 Phase 1: Modern Color Palette

### Primary Color Scheme (Recommended)
- **Primary Color**: `#6366F1` (Indigo - modern, professional)
- **Primary Dark**: `#4F46E5` (Darker indigo for headers)
- **Primary Light**: `#818CF8` (Lighter indigo for accents)
- **Accent Color**: `#10B981` (Emerald green - for success, online status)
- **Error/Red**: `#EF4444` (Modern red for errors, delete actions)
- **Warning**: `#F59E0B` (Amber for warnings)

### Background Colors
- **Background**: `#FFFFFF` (Pure white for main screens)
- **Surface**: `#F9FAFB` (Light gray for cards, elevated surfaces)
- **Surface Dark**: `#F3F4F6` (For dividers, subtle backgrounds)

### Text Colors
- **Primary Text**: `#111827` (Almost black - high contrast)
- **Secondary Text**: `#6B7280` (Medium gray - for descriptions)
- **Hint Text**: `#9CA3AF` (Light gray - for placeholders)
- **White Text**: `#FFFFFF` (For text on dark backgrounds)

### Chat-Specific Colors
- **Sent Message**: `#6366F1` (Primary indigo)
- **Received Message**: `#E5E7EB` (Light gray)
- **Online Status**: `#10B981` (Green)
- **Offline Status**: `#9CA3AF` (Gray)

---

## 📐 Phase 2: Design System Implementation

### 2.1 Update Colors Resource File
Replace the old color palette in `res/values/colors.xml` with the modern palette above.

### 2.2 Typography System
- **Headline 1**: 32sp, Bold, Primary Text
- **Headline 2**: 24sp, Bold, Primary Text
- **Headline 3**: 20sp, Semi-Bold, Primary Text
- **Body 1**: 16sp, Regular, Primary Text
- **Body 2**: 14sp, Regular, Secondary Text
- **Caption**: 12sp, Regular, Hint Text
- **Button Text**: 14sp, Medium, White

### 2.3 Spacing System
- **xs**: 4dp
- **sm**: 8dp
- **md**: 16dp
- **lg**: 24dp
- **xl**: 32dp
- **xxl**: 48dp

### 2.4 Corner Radius
- **Small**: 4dp (buttons, small cards)
- **Medium**: 8dp (cards, input fields)
- **Large**: 12dp (large cards, dialogs)
- **Round**: 50% (avatars, circular buttons)

---

## 🎯 Phase 3: Key Activities to Improve

### 3.1 Main Activity & Navigation
**Current Issues:**
- Old Holo theme
- Basic tab navigation
- No elevation/shadow effects

**Improvements:**
- Implement Material Design 3 components
- Add bottom navigation bar (modern approach)
- Add elevation and shadows for depth
- Smooth transitions and animations

### 3.2 Chat Room Activity
**Current Issues:**
- Basic message bubbles
- Poor color contrast
- No message reactions/animations

**Improvements:**
- Modern message bubble design with rounded corners
- Better spacing between messages
- Message status indicators (sent, delivered, read)
- Smooth scrolling animations
- Typing indicators
- Message reactions (emoji)

### 3.3 Chat List Activity
**Current Issues:**
- Basic list items
- Poor visual hierarchy
- No swipe actions

**Improvements:**
- Card-based list items with elevation
- Better profile picture display (circular with border)
- Unread message badges
- Swipe actions (archive, delete, mute)
- Last seen timestamps
- Online/offline indicators

### 3.4 Login & Registration
**Current Issues:**
- Basic form design
- No visual feedback

**Improvements:**
- Modern input fields with floating labels
- Password strength indicators
- Smooth validation feedback
- Loading states with animations
- Social login buttons (if applicable)

### 3.5 Profile Activities
**Current Issues:**
- Basic profile layout
- Poor image display

**Improvements:**
- Hero image header with parallax effect
- Circular profile pictures
- Better information hierarchy
- Edit profile with smooth transitions
- Status indicators

### 3.6 Call Activities (Incoming/Outgoing)
**Current Issues:**
- Basic call UI
- Poor visual feedback

**Improvements:**
- Modern call interface with gradient backgrounds
- Animated call buttons
- Better caller information display
- Smooth animations for call states

---

## 🛠️ Phase 4: Implementation Steps

### Step 1: Update Color Resources
1. Replace `colors.xml` with modern color palette
2. Update all color references in layouts
3. Test color contrast ratios (WCAG AA compliance)

### Step 2: Update Styles & Themes
1. Create Material Design 3 theme
2. Update `styles.xml` with new typography
3. Add elevation and shadow styles
4. Create reusable component styles

### Step 3: Update Layout Files
1. Add Material Design components (CardView, MaterialButton, etc.)
2. Implement proper spacing using dimens
3. Add elevation and shadows
4. Improve touch targets (minimum 48dp)

### Step 4: Add Animations
1. Activity transitions
2. List item animations
3. Button press feedback
4. Loading animations
5. Message send/receive animations

### Step 5: Improve Components
1. **Buttons**: Rounded corners, proper states, ripple effects
2. **Input Fields**: Floating labels, error states, helper text
3. **Cards**: Elevation, rounded corners, proper padding
4. **Dialogs**: Modern design, smooth animations
5. **Progress Indicators**: Modern circular progress bars

### Step 6: Dark Mode Support (Optional but Recommended)
1. Create `values-night` folder
2. Define dark color palette
3. Test all screens in dark mode

---

## 📱 Phase 5: Specific UI Components to Modernize

### 5.1 Message Bubbles
- **Sent**: Primary color background, white text, rounded corners (12dp top-right, 4dp others)
- **Received**: Light gray background, dark text, rounded corners (12dp top-left, 4dp others)
- Add shadow for depth
- Add message status icons (sent ✓, delivered ✓✓, read ✓✓ blue)

### 5.2 Action Bar / Toolbar
- Use Material Design Toolbar
- Add elevation
- Better icon sizing and spacing
- Search functionality with modern design

### 5.3 Bottom Navigation
- Replace tabs with Material Bottom Navigation
- Add icons with labels
- Active state indicators
- Smooth transitions

### 5.4 Dialogs
- Rounded corners (16dp)
- Proper padding and spacing
- Modern button styles
- Smooth animations

### 5.5 Lists
- Card-based items
- Proper dividers
- Swipe actions
- Pull-to-refresh with modern indicator

---

## 🎨 Phase 6: Visual Enhancements

### 6.1 Icons
- Use Material Design icons
- Consistent icon sizing
- Proper color application
- Icon states (normal, pressed, disabled)

### 6.2 Images
- Circular profile pictures with border
- Image loading placeholders
- Error states for failed image loads
- Smooth image transitions

### 6.3 Empty States
- Friendly empty state illustrations
- Helpful messages
- Call-to-action buttons

### 6.4 Loading States
- Modern progress indicators
- Skeleton screens for content loading
- Smooth loading animations

---

## ✅ Phase 7: UX Improvements

### 7.1 Navigation
- Clear navigation hierarchy
- Breadcrumbs where needed
- Back button behavior
- Deep linking support

### 7.2 Feedback
- Toast messages with modern design
- Snackbars for actions
- Error messages with helpful text
- Success confirmations

### 7.3 Accessibility
- Proper content descriptions
- Minimum touch target sizes (48dp)
- Color contrast compliance
- Support for screen readers

### 7.4 Performance
- Optimize image loading
- Smooth scrolling
- Efficient list recycling
- Lazy loading where appropriate

---

## 📋 Implementation Checklist

### Colors & Themes
- [ ] Update colors.xml with modern palette
- [ ] Create Material Design 3 theme
- [ ] Update styles.xml
- [ ] Test color contrast

### Layouts
- [ ] Update MainActivity layout
- [ ] Update ChatRoomActivity layout
- [ ] Update ChatList layout
- [ ] Update Login/Registration layouts
- [ ] Update Profile layouts
- [ ] Update Call activity layouts

### Components
- [ ] Modernize buttons
- [ ] Update input fields
- [ ] Improve cards
- [ ] Modernize dialogs
- [ ] Update progress indicators

### Animations
- [ ] Add activity transitions
- [ ] Add list animations
- [ ] Add button feedback
- [ ] Add loading animations

### Testing
- [ ] Test on different screen sizes
- [ ] Test in light/dark mode
- [ ] Test accessibility
- [ ] Performance testing

---

## 🚀 Quick Wins (Start Here)

1. **Update Color Palette** - Quick visual improvement
2. **Modernize Buttons** - Immediate impact
3. **Improve Message Bubbles** - Core feature enhancement
4. **Update Action Bar** - Better navigation
5. **Add Elevation/Shadows** - Depth and modern look

---

## 📚 Resources

- Material Design 3 Guidelines: https://m3.material.io
- Color Contrast Checker: https://webaim.org/resources/contrastchecker/
- Material Icons: https://fonts.google.com/icons
- Android Design Guidelines: https://developer.android.com/design

---

## 💡 Tips

1. **Start Small**: Begin with one activity, perfect it, then move to others
2. **Consistency**: Maintain consistent design patterns across all screens
3. **User Testing**: Get feedback from users during the redesign
4. **Performance**: Don't sacrifice performance for aesthetics
5. **Accessibility**: Always consider accessibility in your design

---

## Next Steps

1. Review this guide
2. Prioritize which activities to update first
3. Start with color palette update
4. Gradually implement improvements
5. Test thoroughly before release

Good luck with your UI/UX improvements! 🎨✨

```


Creating an updated colors.xml with a modern color palette:



Creating an improved colors.xml with a modern palette, keeping existing color names for compatibility:


```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ============================================ -->
    <!-- MODERN COLOR PALETTE - Updated 2024 -->
    <!-- ============================================ -->
    
    <!-- Primary Brand Colors -->
    <color name="colorPrimary">#6366F1</color> <!-- Modern Indigo -->
    <color name="colorPrimaryDark">#4F46E5</color> <!-- Darker Indigo -->
    <color name="colorPrimaryLight">#818CF8</color> <!-- Light Indigo -->
    <color name="colorAccent">#10B981</color> <!-- Emerald Green -->
    
    <!-- Background Colors -->
    <color name="white">#FFFFFF</color>
    <color name="black">#000000</color>
    <color name="transparent">#00000000</color>
    
    <color name="ap_background_color">#FFFFFF</color>
    <color name="view_backcolor">#F9FAFB</color>
    <color name="chat_room_bg_color">#F9FAFB</color>
    <color name="search_pane_background">#F3F4F6</color>
    
    <!-- Text Colors -->
    <color name="primary_text_color">#111827</color> <!-- Almost black -->
    <color name="list_primary_text_color">#111827</color>
    <color name="font_text_color">#111827</color>
    <color name="secondary_text">#6B7280</color> <!-- Medium gray -->
    <color name="hint_color">#9CA3AF</color> <!-- Light gray -->
    
    <!-- Message Colors -->
    <color name="message_right_background_color">#6366F1</color> <!-- Sent message - Primary -->
    <color name="message_right_text_color">#FFFFFF</color>
    <color name="message_left_background_color">#E5E7EB</color> <!-- Received message - Light gray -->
    <color name="message_left_text_color">#111827</color>
    
    <!-- Status Colors -->
    <color name="general_online_color">#10B981</color> <!-- Online - Emerald -->
    <color name="green_color">#10B981</color>
    <color name="tag_sel_color">#10B981</color>
    <color name="tag_all_sel_color">#10B981</color>
    
    <!-- Error/Red Colors -->
    <color name="red">#EF4444</color>
    <color name="general_red_color">#EF4444</color>
    <color name="general_red_color_sel">#DC2626</color>
    <color name="badge_bg_color">#EF4444</color>
    <color name="list_item_select_color">#EF4444</color>
    <color name="call_red_default">#EF4444</color>
    <color name="call_red_selected">#DC2626</color>
    
    <!-- Action Bar & Navigation -->
    <color name="actionbar_backcolor">#6366F1</color>
    <color name="background_tab_pressed">#6366F1</color>
    <color name="background_tab">#FFFFFF</color>
    <color name="tabfont_color">#6B7280</color>
    
    <!-- Buttons -->
    <color name="button_ok_default_color">#6366F1</color>
    <color name="button_ok_selected_color">#4F46E5</color>
    <color name="button_ok_disabled_color">#A5B4FC</color>
    <color name="btn_primary_normal">#6366F1</color>
    <color name="btn_primary_select">#4F46E5</color>
    <color name="btn_primary_disabled">#A5B4FC</color>
    
    <!-- Dividers & Borders -->
    <color name="divider_gray_color">#E5E7EB</color>
    <color name="line_color">#E5E7EB</color>
    <color name="list_item_divider_color">#E5E7EB</color>
    <color name="primary_border_color">#D1D5DB</color>
    <color name="editbox_border_color">#D1D5DB</color>
    <color name="editbox_border_focuscolor">#6366F1</color>
    <color name="circular_image_border">#E5E7EB</color>
    
    <!-- Input Fields -->
    <color name="editbox_text_color">#111827</color>
    <color name="editbox_text_hintcolor">#9CA3AF</color>
    <color name="edit_background">#F3F4F6</color>
    <color name="chat_room_input_text_hint_color">#9CA3AF</color>
    
    <!-- List Items -->
    <color name="item_bgcolor_selected">#F3F4F6</color>
    <color name="primary_list_item_bg">#FFFFFF</color>
    <color name="list_item_bg_bottom_color">#E5E7EB</color>
    
    <!-- Dialogs -->
    <color name="dialog_bg_color">#FFFFFF</color>
    <color name="dlg_back_color">#FFFFFF</color>
    <color name="dlg_trans_bg">#80000000</color>
    <color name="modal_background_color">#80000000</color>
    
    <!-- Chat List -->
    <color name="chatlist_backcolor">#00000000</color>
    <color name="chat_list_time_stamp_color">#6B7280</color>
    <color name="chat_friend_msg_time_text_color">#9CA3AF</color>
    
    <!-- Profile -->
    <color name="profile_border_color">#E5E7EB</color>
    <color name="friend_profile_activity_header_text_color">#FFFFFF</color>
    <color name="friend_profile_activity_body_text_color">#111827</color>
    <color name="my_profile_user_id">#6B7280</color>
    <color name="my_profile_user_name">#111827</color>
    <color name="my_profile_label">#111827</color>
    
    <!-- Gray Scale -->
    <color name="gray_color">#9CA3AF</color>
    <color name="darkgray_color">#6B7280</color>
    <color name="light_gray_color">#F3F4F6</color>
    <color name="light_sel_gray_color">#E5E7EB</color>
    <color name="font_disable_color">#9CA3AF</color>
    
    <!-- Call Colors -->
    <color name="call_blue_default">#6366F1</color>
    <color name="call_blue_selected">#4F46E5</color>
    <color name="call_white">#44FFFFFF</color>
    <color name="call_slide_background">#80000000</color>
    <color name="call_background_top">#1F2937</color>
    <color name="call_background_center">#80000000</color>
    <color name="call_background_bottom">#6B7280</color>
    <color name="call_background_bottom_selected">#9CA3AF</color>
    <color name="call_background_button_space">#1F2937</color>
    <color name="call_background_red">#EF4444</color>
    <color name="call_background_blue">#6366F1</color>
    
    <!-- Legacy Colors (Maintained for Compatibility) -->
    <color name="colorA">#6366F1</color>
    <color name="colorB">#000000</color>
    <color name="colorC">#6B7280</color>
    <color name="colorD">#9CA3AF</color>
    <color name="colorE">#D1D5DB</color>
    <color name="colorF">#E5E7EB</color>
    <color name="colorG">#F3F4F6</color>
    <color name="colorH">#FFFFFF</color>
    <color name="colorI">#EF4444</color>
    <color name="colorL">#10B981</color>
    <color name="Bluecolor">#6366F1</color>
    
    <!-- Additional Modern Colors -->
    <color name="primary_blue_color">#6366F1</color>
    <color name="blue">#6366F1</color>
    <color name="dark_blue">#4F46E5</color>
    <color name="attach_title">#6366F1</color>
    
    <!-- Text Variants -->
    <color name="text_default">#111827</color>
    <color name="text_selected">#6366F1</color>
    <color name="text_over">#9CA3AF</color>
    <color name="text_disabled">#D1D5DB</color>
    <color name="text_contrast">#FFFFFF</color>
    <color name="text_button">#FFFFFF</color>
    <color name="text_button_disabled">#9CA3AF</color>
    <color name="text_button_selected">#FFFFFF</color>
    <color name="text_button_over">#6366F1</color>
    
    <!-- Special Purpose Colors -->
    <color name="topbar_sel_color">#1A000000</color>
    <color name="drawlayout_bg_color">#6366F1</color>
    <color name="slidebgcolor">#1F2937</color>
    <color name="passcodebgcolor">#33FFFFFF</color>
    <color name="chatbarseparatorcolor">#E5E7EB</color>
    <color name="spinner_item_bg">#6B7280</color>
    <color name="list_item_header_text_color">#6B7280</color>
    <color name="confirm_dlg_header_line_color">#6366F1</color>
    <color name="confirm_dlg_contact_color">#6366F1</color>
    <color name="option_item_bgcolor_selected">#F3F4F6</color>
    <color name="option_item_bgcolor">#FFFFFF</color>
    <color name="dlg_item_divide_color">#E5E7EB</color>
    <color name="chat_slide_friend_block_btn_bg_color">#EF4444</color>
    <color name="chat_slide_friend_unblock_btn_bg_color">#10B981</color>
    <color name="font_uchatid_color">#6B7280</color>
    <color name="font_chatname_color">#111827</color>
    <color name="chat_sticker_bottom_bar_aboveline_color">#E5E7EB</color>
    <color name="chat_bottom_panel_btn_text_color">#111827</color>
    <color name="chat_bottom_panel_btn_color_sel">#F3F4F6</color>
    <color name="chat_bottom_panel_btn_color">#FFFFFF</color>
    
    <!-- Button Cancel Colors -->
    <color name="button_cancel_default_color">#9CA3AF</color>
    <color name="button_cancel_selected_color">#6B7280</color>
    <color name="button_cancel_disabled_color">#D1D5DB</color>
    <color name="btn_gray_normal">#6B7280</color>
    <color name="btn_gray_select">#4B5563</color>
    
    <!-- Labels & Badges -->
    <color name="label_bg_color">#E5E7EB</color>
    <color name="gray_label_color">#6B7280</color>
    <color name="input_label_bg">#E5E7EB</color>
    <color name="editbox_label_text_color">#FFFFFF</color>
    <color name="editbox_label_bg_color">#806366F1</color>
    
    <!-- Menu & Dropdown -->
    <color name="menu_bg_color">#EE6366F1</color>
    <color name="menu_button_background">#1F2937</color>
    <color name="dropdown_bg_color">#10B981</color>
    <color name="dropdown_sel_color">#6366F1</color>
    <color name="droplist_item_divide_color">#E5E7EB</color>
    <color name="dropdown_text_color">#FFFFFF</color>
    <color name="spinner_select">#E0E7FF</color>
    
    <!-- Action Bar -->
    <color name="action_bar_btn_color_normal">#6366F1</color>
    <color name="action_bar_btn_color_disabled">#A5B4FC</color>
    <color name="action_bar_btn_color_select">#4F46E5</color>
    
    <!-- Search -->
    <color name="search_item_view_background">#F3F4F6</color>
    <color name="search_black_bg">#E0000000</color>
    
    <!-- Other UI Elements -->
    <color name="from_name_color">#6B7280</color>
    <color name="green_text_color">#10B981</color>
    <color name="tags_list_item_bg">#EE6366F1</color>
    <color name="tags_list_item_divider">#EEE5E7EB</color>
    <color name="record_description">#7F000000</color>
    <color name="font_desc_color">#FFFFFF</color>
    <color name="passcode_number_color">#FFFFFF</color>
    <color name="dark_primaryfont_color">#FFFFFF</color>
    <color name="light_tabfont_disable_color">#809CA3AF</color>
    <color name="general_border_color">#D1D5DB</color>
    <color name="inactive_background">#809CA3AF</color>
    <color name="landscape_background">#F3F4F6</color>
    <color name="missed_calls_background">#EF4444</color>
    <color name="conn_status_bar_background">#4B5563</color>
    
    <!-- QR Code -->
    <color name="viewfinder_laser">#EF4444</color>
    <color name="viewfinder_mask">#60000000</color>
    <color name="result_view">#B0000000</color>
    
    <!-- Item Store (if used) -->
    <color name="item_store_tab_indicator_color">#6366F1</color>
    <color name="item_store_tab_underline_color">#E5E7EB</color>
    <color name="item_store_name_text_color">#111827</color>
    <color name="item_store_bg_color_grey">#F3F4F6</color>
    <color name="item_store_theme_item_divider_color">#E5E7EB</color>
    
    <!-- My Page -->
    <color name="mypage_tab_text_sel_color">#6366F1</color>
    <color name="mypage_tab_text_color">#FFFFFF</color>
    <color name="mypage_coupon_item_bottom_layout_bg_color">#B26B7280</color>
    <color name="mypage_coupon_item_bottom_layout_price_text_color">#9CA3AF</color>
    <color name="mypage_coupon_item_select_bg_color">#1A000000</color>
    
    <!-- Additional -->
    <color name="solid_white">#FFFFFF</color>
    <color name="fb_lyt_color">#E5E7EB</color>
    <color name="editbox_bg_focuscolor">#00000000</color>
    <color name="text_cursor">#6366F1</color>
</resources>

```


Creating an improved styles.xml with modern typography and component styles:


```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- ============================================ -->
    <!-- MODERN THEME & STYLES -->
    <!-- ============================================ -->
    
    <!-- Base Theme -->
    <style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
        <item name="android:textColorPrimary">@color/primary_text_color</item>
        <item name="android:textColorSecondary">@color/secondary_text</item>
        <item name="android:windowBackground">@color/ap_background_color</item>
        <item name="android:textCursorDrawable">@drawable/text_cursor</item>
        <item name="android:statusBarColor">@color/colorPrimaryDark</item>
        <item name="android:navigationBarColor">@color/white</item>
    </style>
    
    <style name="FullScreen" parent="AppTheme">
        <item name="android:windowFullscreen">true</item>
        <item name="android:windowNoTitle">true</item>
    </style>
    
    <style name="NoTitle" parent="AppTheme">
        <item name="android:windowNoTitle">true</item>
    </style>
    
    <!-- ============================================ -->
    <!-- TYPOGRAPHY SYSTEM -->
    <!-- ============================================ -->
    
    <!-- Headlines -->
    <style name="TextAppearance.Headline1">
        <item name="android:textSize">32sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textColor">@color/primary_text_color</item>
        <item name="android:letterSpacing">-0.02</item>
    </style>
    
    <style name="TextAppearance.Headline2">
        <item name="android:textSize">24sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textColor">@color/primary_text_color</item>
        <item name="android:letterSpacing">-0.01</item>
    </style>
    
    <style name="TextAppearance.Headline3">
        <item name="android:textSize">20sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textColor">@color/primary_text_color</item>
    </style>
    
    <!-- Body Text -->
    <style name="TextAppearance.Body1">
        <item name="android:textSize">16sp</item>
        <item name="android:textColor">@color/primary_text_color</item>
        <item name="android:lineSpacingMultiplier">1.4</item>
    </style>
    
    <style name="TextAppearance.Body2">
        <item name="android:textSize">14sp</item>
        <item name="android:textColor">@color/secondary_text</item>
        <item name="android:lineSpacingMultiplier">1.4</item>
    </style>
    
    <!-- Caption -->
    <style name="TextAppearance.Caption">
        <item name="android:textSize">12sp</item>
        <item name="android:textColor">@color/secondary_text</item>
    </style>
    
    <!-- Button Text -->
    <style name="TextAppearance.Button">
        <item name="android:textSize">14sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textColor">@color/white</item>
        <item name="android:textAllCaps">true</item>
        <item name="android:letterSpacing">0.1</item>
    </style>
    
    <!-- ============================================ -->
    <!-- BUTTON STYLES -->
    <!-- ============================================ -->
    
    <style name="Widget.Button.Primary" parent="Widget.AppCompat.Button">
        <item name="android:background">@drawable/btn_primary_background</item>
        <item name="android:textColor">@color/white</item>
        <item name="android:textSize">14sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textAllCaps">true</item>
        <item name="android:paddingLeft">24dp</item>
        <item name="android:paddingRight">24dp</item>
        <item name="android:paddingTop">12dp</item>
        <item name="android:paddingBottom">12dp</item>
        <item name="android:minHeight">48dp</item>
        <item name="android:elevation">2dp</item>
    </style>
    
    <style name="Widget.Button.Secondary" parent="Widget.AppCompat.Button">
        <item name="android:background">@drawable/btn_secondary_background</item>
        <item name="android:textColor">@color/colorPrimary</item>
        <item name="android:textSize">14sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textAllCaps">true</item>
        <item name="android:paddingLeft">24dp</item>
        <item name="android:paddingRight">24dp</item>
        <item name="android:paddingTop">12dp</item>
        <item name="android:paddingBottom">12dp</item>
        <item name="android:minHeight">48dp</item>
        <item name="android:elevation">0dp</item>
    </style>
    
    <style name="Widget.Button.Text" parent="Widget.AppCompat.Button.Borderless">
        <item name="android:textColor">@color/colorPrimary</item>
        <item name="android:textSize">14sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textAllCaps">true</item>
        <item name="android:paddingLeft">16dp</item>
        <item name="android:paddingRight">16dp</item>
        <item name="android:paddingTop">12dp</item>
        <item name="android:paddingBottom">12dp</item>
        <item name="android:minHeight">48dp</item>
    </style>
    
    <!-- ============================================ -->
    <!-- INPUT FIELD STYLES -->
    <!-- ============================================ -->
    
    <style name="Widget.EditText.Modern">
        <item name="android:background">@drawable/edittext_background</item>
        <item name="android:textColor">@color/primary_text_color</item>
        <item name="android:textColorHint">@color/hint_color</item>
        <item name="android:textSize">16sp</item>
        <item name="android:paddingLeft">16dp</item>
        <item name="android:paddingRight">16dp</item>
        <item name="android:paddingTop">12dp</item>
        <item name="android:paddingBottom">12dp</item>
        <item name="android:minHeight">48dp</item>
    </style>
    
    <style name="editbox_search" parent="Widget.EditText.Modern">
        <item name="android:drawableLeft">@drawable/cii_search</item>
        <item name="android:drawablePadding">12dp</item>
        <item name="android:paddingLeft">16dp</item>
        <item name="android:paddingRight">16dp</item>
        <item name="android:singleLine">true</item>
        <item name="android:imeOptions">actionSearch</item>
    </style>
    
    <!-- ============================================ -->
    <!-- CARD STYLES -->
    <!-- ============================================ -->
    
    <style name="Widget.Card">
        <item name="android:background">@drawable/card_background</item>
        <item name="android:elevation">2dp</item>
        <item name="android:layout_margin">8dp</item>
        <item name="cardCornerRadius">12dp</item>
        <item name="cardUseCompatPadding">true</item>
    </style>
    
    <!-- ============================================ -->
    <!-- DIALOG STYLES -->
    <!-- ============================================ -->
    
    <style name="Theme.Dialog.Modern" parent="Theme.AppCompat.Light.Dialog">
        <item name="android:windowBackground">@drawable/dialog_background</item>
        <item name="android:windowMinWidthMajor">85%</item>
        <item name="android:windowMinWidthMinor">85%</item>
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>
    
    <style name="custom_dialog_theme" parent="Theme.Dialog.Modern">
        <item name="android:windowBackground">@color/dlg_trans_bg</item>
        <item name="android:windowIsFloating">false</item>
        <item name="android:windowNoTitle">true</item>
    </style>
    
    <!-- ============================================ -->
    <!-- PROGRESS BAR STYLES -->
    <!-- ============================================ -->
    
    <style name="Widget.ProgressBar.Modern" parent="Widget.AppCompat.ProgressBar">
        <item name="android:indeterminateTint">@color/colorPrimary</item>
        <item name="android:progressTint">@color/colorPrimary</item>
    </style>
    
    <style name="mediumCustomProgressBar" parent="Widget.ProgressBar.Modern">
        <item name="android:indeterminateDrawable">@drawable/progress_medium_holo</item>
    </style>
    
    <style name="largeCustomProgressBar" parent="Widget.ProgressBar.Modern">
        <item name="android:indeterminateDrawable">@drawable/progress_large_holo</item>
    </style>
    
    <!-- ============================================ -->
    <!-- LEGACY FONT STYLES (Updated) -->
    <!-- ============================================ -->
    
    <style name="font1" parent="TextAppearance.Headline2">
        <item name="android:textColor">@color/colorPrimary</item>
        <item name="android:textAllCaps">true</item>
    </style>
    
    <style name="font2" parent="TextAppearance.Body1">
        <item name="android:textColor">@color/colorPrimary</item>
    </style>
    
    <style name="font3" parent="TextAppearance.Caption">
        <item name="android:textColor">@color/colorPrimary</item>
    </style>
    
    <style name="font4" parent="TextAppearance.Headline3">
        <item name="android:textColor">@color/colorAccent</item>
        <item name="android:textAllCaps">true</item>
    </style>
    
    <style name="font5" parent="TextAppearance.Headline1">
        <item name="android:textColor">@color/secondary_text</item>
        <item name="android:textSize">30sp</item>
    </style>
    
    <style name="font6" parent="TextAppearance.Headline3">
        <item name="android:textColor">@color/secondary_text</item>
        <item name="android:textSize">20sp</item>
    </style>
    
    <style name="font7" parent="TextAppearance.Body1">
        <item name="android:textColor">@color/secondary_text</item>
        <item name="android:textStyle">bold</item>
    </style>
    
    <style name="font8" parent="TextAppearance.Body1">
        <item name="android:textColor">@color/secondary_text</item>
        <item name="android:textAllCaps">true</item>
    </style>
    
    <style name="font9" parent="TextAppearance.Caption">
        <item name="android:textColor">@color/secondary_text</item>
    </style>
    
    <style name="font10" parent="TextAppearance.Body1">
        <item name="android:textColor">@color/hint_color</item>
    </style>
    
    <style name="font11" parent="TextAppearance.Body2">
        <item name="android:textColor">@color/hint_color</item>
    </style>
    
    <style name="font12" parent="TextAppearance.Body1">
        <item name="android:textColor">@color/divider_gray_color</item>
    </style>
    
    <style name="font13" parent="TextAppearance.Caption">
        <item name="android:textColor">@color/divider_gray_color</item>
    </style>
    
    <style name="font14" parent="TextAppearance.Headline2">
        <item name="android:textColor">@color/white</item>
    </style>
    
    <style name="font15" parent="TextAppearance.Body1">
        <item name="android:textColor">@color/white</item>
        <item name="android:textAllCaps">true</item>
    </style>
    
    <style name="font16" parent="TextAppearance.Body2">
        <item name="android:textColor">@color/white</item>
    </style>
    
    <style name="font17" parent="TextAppearance.Caption">
        <item name="android:textColor">@color/white</item>
        <item name="android:textStyle">bold</item>
    </style>
    
    <style name="font18" parent="TextAppearance.Caption">
        <item name="android:textColor">@color/white</item>
        <item name="android:textSize">10sp</item>
    </style>
    
    <style name="font19" parent="TextAppearance.Caption">
        <item name="android:textColor">@color/hint_color</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textAllCaps">true</item>
    </style>
    
    <style name="font20" parent="TextAppearance.Body2">
        <item name="android:textColor">@color/general_red_color</item>
    </style>
    
    <style name="font21" parent="TextAppearance.Headline1">
        <item name="android:textColor">@color/white</item>
        <item name="android:textAllCaps">true</item>
        <item name="android:textSize">50sp</item>
    </style>
    
    <style name="font22" parent="TextAppearance.Headline2">
        <item name="android:textColor">@color/white</item>
        <item name="android:textAllCaps">true</item>
        <item name="android:textStyle">bold</item>
    </style>
    
    <style name="font23" parent="TextAppearance.Body1">
        <item name="android:textColor">@color/hint_color</item>
        <item name="android:textStyle">italic</item>
    </style>
    
    <style name="font24" parent="TextAppearance.Caption">
        <item name="android:textColor">@color/colorAccent</item>
    </style>
    
    <style name="font25" parent="TextAppearance.Headline2">
        <item name="android:textColor">@color/white</item>
        <item name="android:textStyle">bold</item>
    </style>
    
    <style name="font26" parent="TextAppearance.Headline1">
        <item name="android:textColor">@color/general_red_color</item>
        <item name="android:textSize">30sp</item>
    </style>
    
    <style name="font27" parent="TextAppearance.Headline2">
        <item name="android:textColor">@color/colorPrimary</item>
        <item name="android:textSize">22sp</item>
    </style>
    
    <style name="font28" parent="TextAppearance.Body1">
        <item name="android:textColor">@color/colorPrimary</item>
    </style>
    
    <!-- ============================================ -->
    <!-- ANIMATIONS -->
    <!-- ============================================ -->
    
    <style name="Animations" />
    
    <style name="Animations.PopDownMenu" />
    
    <style name="Animations.PopDownMenu.Left">
        <item name="@android:windowEnterAnimation">@anim/grow_from_topleft_to_bottomright</item>
        <item name="@android:windowExitAnimation">@anim/shrink_from_bottomright_to_topleft</item>
    </style>
    
    <style name="Animations.PopDownMenu.Right">
        <item name="@android:windowEnterAnimation">@anim/grow_from_topright_to_bottomleft</item>
        <item name="@android:windowExitAnimation">@anim/shrink_from_bottomleft_to_topright</item>
    </style>
    
    <style name="Animations.PopDownMenu.Top">
        <item name="@android:windowEnterAnimation">@anim/grow_from_top_to_bottom</item>
        <item name="@android:windowExitAnimation">@anim/shrink_from_bottom_to_top</item>
    </style>
    
    <style name="Animations.PopUpMenu" />
    
    <style name="Animations.PopUpMenu.Left">
        <item name="@android:windowEnterAnimation">@anim/grow_from_bottomleft_to_topright</item>
        <item name="@android:windowExitAnimation">@anim/shrink_from_topright_to_bottomleft</item>
    </style>
    
    <style name="Animations.PopUpMenu.Right">
        <item name="@android:windowEnterAnimation">@anim/grow_from_bottomright_to_topleft</item>
        <item name="@android:windowExitAnimation">@anim/shrink_from_topleft_to_bottomright</item>
    </style>
    
    <style name="Animations.PopUpMenu.Slide" />
    
    <style name="Animations.PopUpMenu.Slide.Right">
        <item name="@android:windowEnterAnimation">@anim/grow_from_right_to_left</item>
        <item name="@android:windowExitAnimation">@anim/shrink_from_left_to_right</item>
    </style>
    
    <!-- ============================================ -->
    <!-- COMPONENT STYLES -->
    <!-- ============================================ -->
    
    <style name="edittext_component">
        <item name="android:paddingLeft">16dp</item>
        <item name="android:paddingRight">16dp</item>
    </style>
    
    <style name="search_bar_edit" parent="editbox_search">
        <item name="android:background">@drawable/bg_primary_input</item>
        <item name="android:drawableRight">@drawable/cii_search</item>
    </style>
    
</resources>

```


Creating a quick-start implementation guide:


```markdown
# Quick Start: UI/UX Improvement Implementation

## 🚀 Step-by-Step Implementation

### Step 1: Backup Current Files
```bash
# Backup your current color and style files
cp colors.xml colors_old.xml
cp styles.xml styles_old.xml
```

### Step 2: Replace Colors (Choose One Method)

#### Option A: Direct Replacement (Recommended for Fresh Start)
1. Rename `colors_modern.xml` to `colors.xml`
2. Review and adjust colors as needed
3. Test the app to ensure all colors work correctly

#### Option B: Gradual Migration (Safer)
1. Keep both files temporarily
2. Update colors one section at a time
3. Test after each section

### Step 3: Update Styles
1. Review `styles_modern.xml`
2. Merge with your existing `styles.xml` or replace it
3. Update theme references in AndroidManifest.xml if needed

### Step 4: Create Missing Drawable Resources

You'll need to create these drawable files referenced in the styles:

#### `btn_primary_background.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="@color/button_ok_selected_color"/>
            <corners android:radius="8dp"/>
        </shape>
    </item>
    <item android:state_enabled="false">
        <shape android:shape="rectangle">
            <solid android:color="@color/button_ok_disabled_color"/>
            <corners android:radius="8dp"/>
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/button_ok_default_color"/>
            <corners android:radius="8dp"/>
        </shape>
    </item>
</selector>
```

#### `btn_secondary_background.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="@color/view_backcolor"/>
            <stroke android:width="2dp" android:color="@color/colorPrimary"/>
            <corners android:radius="8dp"/>
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/transparent"/>
            <stroke android:width="2dp" android:color="@color/colorPrimary"/>
            <corners android:radius="8dp"/>
        </shape>
    </item>
</selector>
```

#### `edittext_background.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_focused="true">
        <shape android:shape="rectangle">
            <solid android:color="@color/white"/>
            <stroke android:width="2dp" android:color="@color/editbox_border_focuscolor"/>
            <corners android:radius="8dp"/>
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/white"/>
            <stroke android:width="1dp" android:color="@color/editbox_border_color"/>
            <corners android:radius="8dp"/>
        </shape>
    </item>
</selector>
```

#### `card_background.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/white"/>
    <corners android:radius="12dp"/>
</shape>
```

#### `dialog_background.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/dialog_bg_color"/>
    <corners android:radius="16dp"/>
</shape>
```

### Step 5: Update Key Layout Files

#### Priority Layouts to Update:
1. **activity_main.xml** - Add modern splash screen
2. **chat_room_activity.xml** - Modernize message bubbles
3. **chat_list_item.xml** - Card-based design
4. **main_tab_navigation.xml** - Modern bottom navigation
5. **login_activity.xml** - Modern input fields

### Step 6: Add Material Design Components

Update your `build.gradle` dependencies:
```gradle
dependencies {
    // Material Design Components
    implementation 'com.google.android.material:material:1.9.0'
    
    // CardView
    implementation 'androidx.cardview:cardview:1.0.0'
    
    // RecyclerView (if not already included)
    implementation 'androidx.recyclerview:recyclerview:1.3.1'
}
```

### Step 7: Test & Iterate

1. **Visual Testing**: Check all screens for color consistency
2. **Accessibility**: Test color contrast ratios
3. **Dark Mode**: Consider adding dark theme support
4. **User Feedback**: Get feedback from beta testers

---

## 🎯 Priority Activities to Update First

### High Priority (Core Features)
1. ✅ **ChatRoomActivity** - Most used screen
2. ✅ **ChatListFragment** - First thing users see
3. ✅ **MainTabNavigationActivity** - Navigation hub
4. ✅ **LoginActivity** - First impression

### Medium Priority
5. **Profile Activities** - User identity
6. **Call Activities** - Important feature
7. **Settings** - User preferences

### Low Priority
8. **Help/Tutorial** - Less frequent
9. **About/Info** - Rarely accessed

---

## 🎨 Color Customization Tips

### If You Want Different Colors:

1. **Primary Color**: Change `colorPrimary` in colors.xml
   - Good choices: `#6366F1` (Indigo), `#8B5CF6` (Purple), `#06B6D4` (Cyan), `#F59E0B` (Amber)

2. **Accent Color**: Change `colorAccent`
   - Should contrast with primary
   - Good choices: `#10B981` (Green), `#EF4444` (Red), `#F59E0B` (Amber)

3. **Test Contrast**: Use online tools to ensure WCAG AA compliance
   - https://webaim.org/resources/contrastchecker/

---

## 📱 Material Design 3 Features to Consider

### 1. Dynamic Color (Android 12+)
- System-wide color theming
- Adapts to user's wallpaper

### 2. Motion & Animation
- Shared element transitions
- Material motion patterns
- Smooth state changes

### 3. Adaptive Layouts
- Responsive design for tablets
- Foldable device support
- Multi-window support

---

## 🔧 Common Issues & Solutions

### Issue: Colors not applying
**Solution**: Clean and rebuild project
```bash
./gradlew clean
./gradlew build
```

### Issue: Styles not found
**Solution**: Check if styles are in correct `values` folder and XML is valid

### Issue: Drawable resources missing
**Solution**: Create the drawable files listed in Step 4

### Issue: App crashes after color update
**Solution**: Check for hardcoded color values in Java/Kotlin code

---

## 📊 Progress Tracking

Use this checklist to track your progress:

- [ ] Colors updated
- [ ] Styles updated
- [ ] Drawable resources created
- [ ] MainActivity updated
- [ ] ChatRoomActivity updated
- [ ] ChatList updated
- [ ] LoginActivity updated
- [ ] Profile activities updated
- [ ] Call activities updated
- [ ] Testing completed
- [ ] User feedback collected
- [ ] Final polish applied

---

## 💡 Pro Tips

1. **Start Small**: Update one screen at a time
2. **Version Control**: Commit after each major change
3. **Screenshots**: Take before/after screenshots
4. **User Testing**: Get feedback early and often
5. **Performance**: Monitor app performance during updates
6. **Accessibility**: Always test with accessibility features enabled

---

## 🎓 Learning Resources

- [Material Design 3](https://m3.material.io/)
- [Android Design Guidelines](https://developer.android.com/design)
- [Material Components for Android](https://github.com/material-components/material-components-android)
- [Color Theory for Designers](https://www.smashingmagazine.com/2010/02/color-theory-for-designers-part-1/)

---

## 🚀 Next Steps After Basic Update

1. **Add Animations**: Smooth transitions between screens
2. **Dark Mode**: Implement dark theme support
3. **Custom Fonts**: Add custom typography
4. **Micro-interactions**: Add delightful details
5. **Accessibility**: Full accessibility audit
6. **Performance**: Optimize rendering and animations

Good luck with your UI/UX improvements! 🎨✨

```


Created a UI/UX improvement plan for your project. Here's what's included:

## What I've Created

### 1. **UI_UX_IMPROVEMENT_GUIDE.md** — Guide
   - Modern color palette recommendations
   - Design system guidelines
   - Activity-by-activity improvement suggestions
   - Implementation phases
   - Checklist for tracking progress

### 2. **colors_modern.xml** — Updated color palette
   - Modern color scheme (Indigo primary, Emerald accent)
   - Maintains backward compatibility (same color names)
   - Updated values for a modern look
   - Ready to use (rename to `colors.xml` when ready)

### 3. **styles_modern.xml** — Updated styles
   - Typography system (Headlines, Body, Caption)
   - Modern button styles
   - Input field styles
   - Card and dialog styles
   - Updated legacy font styles

### 4. **QUICK_START_UI_IMPROVEMENT.md** — Quick implementation guide
   - Step-by-step instructions
   - Drawable resource templates
   - Priority list for activities
   - Troubleshooting tips

## Key Improvements Recommended

### Color Palette
- Primary: `#6366F1` (Indigo)
- Accent: `#10B981` (Emerald Green)
- Modern grays for text and backgrounds
- Improved contrast for accessibility

### Design System
- Consistent typography (32sp headlines to 12sp captions)
- Standardized spacing (4dp to 48dp)
- Rounded corners (4dp, 8dp, 12dp)
- Elevation and shadows for depth

### Priority Activities
1. ChatRoomActivity — message bubbles
2. ChatList — card-based items
3. MainTabNavigation — modern navigation
4. LoginActivity — modern forms

## How to Get Started

1. Review the guides in the files above
2. Start with colors: use `colors_modern.xml` as a reference
3. Update one activity at a time (start with ChatRoomActivity)
4. Test frequently to catch issues early
5. Iterate based on feedback

## Quick Wins

1. Replace `colors.xml` with the modern palette
2. Update message bubble colors
3. Modernize buttons with rounded corners
4. Add elevation to cards
5. Improve input field styling

The guides include specific code examples, color values, and step-by-step instructions. Start with the QUICK_START guide, then refer to the detailed guide for deeper improvements.

Would you like me to:
1. Update a specific activity layout file?
2. Create the missing drawable resources?
3. Help implement a specific feature from the guide?

---

**User**

can you know the versions of gradle and androidsdk, maven repository which this project use?

---

**Cursor**


Checking Gradle, Android SDK, and Maven repository versions.



## Project build configuration

### Gradle
- Gradle version: 5.4.1
  - Location: `gradle/wrapper/gradle-wrapper.properties`
  - Distribution URL: `https://services.gradle.org/distributions/gradle-5.4.1-all.zip`

### Android Gradle Plugin
- Android Gradle Plugin: 3.4.0
  - Location: `build.gradle` (root)
  - Classpath: `com.android.tools.build:gradle:3.4.0`

### Android SDK
- Compile SDK: 28 (Android 9.0 Pie)
- Target SDK: 28 (Android 9.0 Pie)
- Min SDK: 18 (Android 4.3 Jelly Bean)
- Build Tools: 28.0.3

### Maven repositories
- google() — Google's Maven repository
- jcenter() — JCenter (deprecated; should be replaced)

### Support library versions
- Support Library: 28.0.0
  - `appcompat-v7:28.0.0`
  - `support-v4:28.0.0`
  - `support-v13:28.0.0`
  - `design:28.0.0`
  - `recyclerview-v7:28.0.0`
  - `exifinterface:28.0.0`
  - `multidex:1.0.1`
  - `constraint-layout:1.1.3`

### Important notes
1. JCenter is deprecated. Replace `jcenter()` with `mavenCentral()` or remove it if not needed.
2. Gradle 5.4.1 is outdated. Consider upgrading to 7.x or 8.x for better performance and security.
3. Android Gradle Plugin 3.4.0 is outdated. Consider upgrading to 7.x or 8.x (requires Gradle 7.x+).
4. Target SDK 28 is outdated. Consider targeting SDK 33 or higher for Play Store requirements.

Should I create an upgrade guide to modernize these versions?

---

**User**

I don't want to update the version and just keep the same version of gradle and same version of sdk. So the code you generate MUST be run on that version. Is this enable?

---

**Cursor**


Yes, I'll keep the same versions. All generated code will be compatible with your current setup:

- Gradle: 5.4.1
- Android Gradle Plugin: 3.4.0
- Compile SDK: 28
- Target SDK: 28
- Min SDK: 18
- Build Tools: 28.0.3
- Support Library: 28.0.0 (not AndroidX)

This means:
- Using Support Library 28.0.0 (not AndroidX)
- Using only APIs available in SDK 28
- Using Gradle 5.4.1 compatible syntax
- Ensuring dependencies are compatible with SDK 28

Verifying compatibility and preparing a summary document:



Creating a compatibility document confirming all generated code will work with your current versions:


```markdown
# Version Compatibility Guarantee

## ✅ Confirmed: All Generated Code Will Work With Current Versions

### Current Project Configuration (FIXED - Will NOT Change)

| Component | Version | Status |
|-----------|---------|--------|
| **Gradle** | 5.4.1 | ✅ Locked |
| **Android Gradle Plugin** | 3.4.0 | ✅ Locked |
| **Compile SDK** | 28 (Android 9.0 Pie) | ✅ Locked |
| **Target SDK** | 28 (Android 9.0 Pie) | ✅ Locked |
| **Min SDK** | 18 (Android 4.3 Jelly Bean) | ✅ Locked |
| **Build Tools** | 28.0.3 | ✅ Locked |
| **Support Library** | 28.0.0 | ✅ Locked |
| **Constraint Layout** | 1.1.3 | ✅ Locked |

---

## 🔒 Code Generation Rules

### 1. Support Library (NOT AndroidX)
✅ **Will Use:**
- `android.support.*` packages
- `com.android.support.*` dependencies
- Support Library 28.0.0 compatible components

❌ **Will NOT Use:**
- `androidx.*` packages
- AndroidX dependencies
- Material Components (requires AndroidX)

### 2. Available Components for UI Improvements

#### ✅ Can Use (SDK 28 Compatible):
- `android.support.v7.widget.CardView` (from design library)
- `android.support.design.widget.*` (Material Design components)
- `android.support.v7.widget.RecyclerView`
- `android.support.v7.app.AppCompatActivity`
- `android.support.v7.widget.Toolbar`
- `android.support.design.widget.FloatingActionButton`
- `android.support.design.widget.TextInputLayout`
- `android.support.design.widget.BottomNavigationView`
- `android.support.constraint.ConstraintLayout`
- `android.support.v4.widget.SwipeRefreshLayout`

#### ❌ Cannot Use:
- Material Components library (requires AndroidX)
- AndroidX packages
- APIs introduced after SDK 28
- Newer Material Design 3 components

### 3. Dependencies That Work

#### ✅ Safe to Add (SDK 28 Compatible):
```gradle
// Already in project - can use
implementation 'com.android.support:design:28.0.0'
implementation 'com.android.support:cardview-v7:28.0.0'
implementation 'com.android.support:recyclerview-v7:28.0.0'
implementation 'com.android.support:appcompat-v7:28.0.0'
implementation 'com.android.support.constraint:constraint-layout:1.1.3'
```

#### ❌ Cannot Add:
- Material Components (requires AndroidX)
- Any AndroidX dependencies
- Dependencies requiring SDK > 28

### 4. Layout XML Rules

✅ **Will Use:**
```xml
<!-- Support Library namespaces -->
xmlns:app="http://schemas.android.com/apk/res-auto"
xmlns:android="http://schemas.android.com/apk/res/android"

<!-- Support Library components -->
<android.support.v7.widget.CardView>
<android.support.design.widget.FloatingActionButton>
<android.support.constraint.ConstraintLayout>
```

❌ **Will NOT Use:**
```xml
<!-- AndroidX (will NOT use) -->
<androidx.cardview.widget.CardView>
<com.google.android.material.button.MaterialButton>
```

### 5. Java/Kotlin Code Rules

✅ **Will Use:**
```java
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.BottomNavigationView;
```

❌ **Will NOT Use:**
```java
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
```

---

## 🎨 UI Improvements Compatible with SDK 28

### What CAN Be Done:
1. ✅ Modern color palette (just colors, no library needed)
2. ✅ CardView with elevation and rounded corners
3. ✅ Material Design components from Support Library 28.0.0
4. ✅ BottomNavigationView (from design library)
5. ✅ TextInputLayout with floating labels
6. ✅ RecyclerView with modern layouts
7. ✅ ConstraintLayout for modern layouts
8. ✅ Custom drawable resources
9. ✅ Animations (using Support Library animation APIs)
10. ✅ Ripple effects (built into Support Library)

### What CANNOT Be Done:
1. ❌ Material Components library features
2. ❌ New Material Design 3 components
3. ❌ Some newer animation APIs (but most work)
4. ❌ Dynamic colors (Android 12+ feature)

---

## 📝 Example: Compatible Code Generation

### ✅ CORRECT (Will Generate This):
```xml
<!-- Layout -->
<android.support.v7.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp">
    
    <android.support.constraint.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <!-- Content -->
    </android.support.constraint.ConstraintLayout>
</android.support.v7.widget.CardView>
```

```java
// Java Code
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.design.widget.FloatingActionButton;

public class MyActivity extends AppCompatActivity {
    // Code here
}
```

### ❌ WRONG (Will NOT Generate This):
```xml
<!-- This would be WRONG -->
<com.google.android.material.card.MaterialCardView>
<androidx.cardview.widget.CardView>
```

```java
// This would be WRONG
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
```

---

## ✅ Guarantee

**All code I generate will:**
1. ✅ Use Support Library 28.0.0 (NOT AndroidX)
2. ✅ Use only APIs available in SDK 28
3. ✅ Be compatible with Gradle 5.4.1
4. ✅ Use Android Gradle Plugin 3.4.0 syntax
5. ✅ Work with your current build configuration
6. ✅ Not require any version upgrades

---

## 🚀 Ready to Proceed

I'm ready to generate UI improvement code that will work perfectly with your current setup. Just let me know what you'd like me to update, and I'll ensure 100% compatibility with:

- Gradle 5.4.1
- Android Gradle Plugin 3.4.0
- SDK 28
- Support Library 28.0.0

**No version upgrades required!** 🎉

```


Yes, this is enabled. All generated code will work with your current versions.

## Confirmed compatibility

I've created a compatibility document (`VERSION_COMPATIBILITY.md`) that outlines the rules. Here's what I'll ensure:

### What I'll use (compatible with your setup)
- Support Library 28.0.0 (NOT AndroidX)
- `android.support.*` packages
- SDK 28 APIs only
- Gradle 5.4.1 compatible syntax
- Components from Support Library 28.0.0:
  - CardView
  - RecyclerView
  - BottomNavigationView
  - TextInputLayout
  - FloatingActionButton
  - Material Design components from design library

### What I won't use (incompatible)
- AndroidX packages
- Material Components library
- APIs newer than SDK 28
- Any code requiring version upgrades

### Example of compatible code I'll generate
```xml
<!-- ✅ This is what I'll generate -->
<android.support.v7.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp">
```

```java
// ✅ This is what I'll generate
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
```

All UI improvements will work with:
- Gradle 5.4.1
- Android Gradle Plugin 3.4.0
- SDK 28
- Support Library 28.0.0

No version upgrades needed. Ready to proceed with UI improvements that are 100% compatible with your current setup.

Would you like me to:
1. Update specific layout files with modern designs?
2. Create the missing drawable resources?
3. Modernize a specific activity?

```xml
<!-- ✅ This is what I'll generate -->
<android.support.v7.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp">
```

```java
// ✅ This is what I'll generate
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
```

---

**User**

@VERSION_COMPATIBILITY.md I'm going to update UI/UX of ChatRoomActivity at first. First of all , provide me improvement list of updating the ChatRoomActivity about what you are going to improve and how you are going to improve.
After that, when I say 'start' then start generating code about that.

---

**Cursor**


Analyzing ChatRoomActivity and preparing an improvement plan. Examining the current implementation:



Checking drawable resources for message bubbles:



Creating the improvement plan document:


```markdown
# ChatRoomActivity UI/UX Improvement Plan

## 📋 Overview
This document outlines all improvements that will be made to the ChatRoomActivity, ensuring 100% compatibility with:
- Gradle 5.4.1
- Android Gradle Plugin 3.4.0
- SDK 28
- Support Library 28.0.0

---

## 🎯 Improvement Categories

### 1. **Message Bubbles (Left & Right)**
**Current State:**
- Basic rounded rectangles with old drawable backgrounds
- Poor color contrast
- No elevation/shadow effects
- Inconsistent corner radius
- Basic text styling

**Improvements:**
- ✅ Modern rounded corners (12dp for main corner, 4dp for others)
- ✅ Updated colors using modern palette:
  - Sent messages: Primary indigo (#6366F1) background, white text
  - Received messages: Light gray (#E5E7EB) background, dark text
- ✅ Add elevation (2dp) for depth using drawable shadows
- ✅ Better padding (12dp horizontal, 10dp vertical)
- ✅ Improved text styling with better line spacing
- ✅ Smooth corner radius transitions

**Files to Update:**
- `message_view_left.xml` - Received messages
- `message_view_right.xml` - Sent messages
- Create new drawable resources: `chat_bubble_left.xml`, `chat_bubble_right.xml`

---

### 2. **Chat Input Bar (Send Bar)**
**Current State:**
- Basic LinearLayout with simple EditText
- Old-style icons
- No visual feedback on interactions
- Basic separator line
- Poor spacing

**Improvements:**
- ✅ Modern input field with rounded corners (8dp)
- ✅ Better spacing and padding (16dp margins)
- ✅ Improved button styling with ripple effects
- ✅ Modern send button with better visual feedback
- ✅ Better separator styling (subtle shadow/elevation)
- ✅ Improved voice message button design
- ✅ Better emoji button placement and styling
- ✅ Input field background with focus states

**Files to Update:**
- `chat_room_send_bar.xml`
- Create drawable: `input_field_background.xml`, `send_button_background.xml`

---

### 3. **Message List Container**
**Current State:**
- Basic ListView (old component)
- No elevation or modern styling
- Basic background

**Improvements:**
- ✅ Keep ListView (compatible with SDK 28) but improve styling
- ✅ Better background color (light gray #F9FAFB)
- ✅ Improved padding and margins
- ✅ Better scrollbar styling
- ✅ Smooth scrolling indicators

**Files to Update:**
- `chat_room_content_layout.xml`

---

### 4. **Date Separators**
**Current State:**
- Basic label with simple background
- Poor visual hierarchy

**Improvements:**
- ✅ Modern pill-shaped design
- ✅ Better typography (14sp, medium weight)
- ✅ Subtle background with rounded corners
- ✅ Better spacing (16dp padding)
- ✅ Improved color contrast

**Files to Update:**
- `chat_date_item.xml`
- Create drawable: `date_separator_background.xml`

---

### 5. **Message Status Indicators**
**Current State:**
- Basic icons (send, received)
- Poor visibility
- No color differentiation

**Improvements:**
- ✅ Better icon sizing (12dp)
- ✅ Color-coded status:
  - Sending: Gray
  - Sent: Primary color
  - Delivered: Primary color (double check)
  - Read: Accent green color
- ✅ Better positioning and spacing
- ✅ Smooth visibility transitions

**Files to Update:**
- `message_view_left.xml`
- `message_view_right.xml`

---

### 6. **Profile Pictures in Messages**
**Current State:**
- Basic circular images
- No border or elevation
- Poor spacing

**Improvements:**
- ✅ Add subtle border (2dp, light gray)
- ✅ Better spacing from message bubble (8dp)
- ✅ Improved sizing consistency
- ✅ Better alignment

**Files to Update:**
- `message_view_left.xml`
- `message_view_right.xml`

---

### 7. **Media Messages (Images/Videos)**
**Current State:**
- Basic image display
- No rounded corners on media
- Poor thumbnail styling

**Improvements:**
- ✅ Rounded corners on media thumbnails (8dp)
- ✅ Better aspect ratio handling
- ✅ Improved play button overlay
- ✅ Better loading states
- ✅ Modern download/upload indicators

**Files to Update:**
- `message_view_left.xml`
- `message_view_right.xml`

---

### 8. **Voice Messages**
**Current State:**
- Basic progress bar
- Simple play button
- Poor visual design

**Improvements:**
- ✅ Modern progress bar styling
- ✅ Better play button design
- ✅ Improved time display
- ✅ Better background styling
- ✅ Rounded corners on voice message container

**Files to Update:**
- `message_view_left.xml`
- `message_view_right.xml`

---

### 9. **Overall Layout Structure**
**Current State:**
- Basic LinearLayout structure
- No elevation hierarchy
- Poor spacing system

**Improvements:**
- ✅ Better use of ConstraintLayout where appropriate
- ✅ Consistent spacing system (8dp, 16dp, 24dp)
- ✅ Improved margin system
- ✅ Better background colors
- ✅ Smooth transitions between states

**Files to Update:**
- `chat_room_activity.xml`
- `chat_room_content_layout.xml`

---

### 10. **Colors & Theming**
**Current State:**
- Old color palette
- Inconsistent color usage

**Improvements:**
- ✅ Apply modern color palette from `colors_modern.xml`
- ✅ Consistent color usage across all components
- ✅ Better contrast ratios (WCAG AA compliant)
- ✅ Proper color states (normal, pressed, disabled)

**Files to Update:**
- All layout files will reference updated colors

---

## 📁 Files That Will Be Created/Updated

### New Drawable Resources:
1. `chat_bubble_left.xml` - Received message bubble with rounded corners
2. `chat_bubble_right.xml` - Sent message bubble with rounded corners and shadow
3. `input_field_background.xml` - Modern input field background
4. `send_button_background.xml` - Send button with ripple effect
5. `date_separator_background.xml` - Date separator pill design
6. `voice_message_background.xml` - Voice message container background

### Updated Layout Files:
1. `chat_room_activity.xml` - Main activity layout
2. `chat_room_content_layout.xml` - Content container
3. `chat_room_send_bar.xml` - Input bar
4. `message_view_left.xml` - Received messages
5. `message_view_right.xml` - Sent messages
6. `chat_date_item.xml` - Date separators

---

## 🎨 Design Specifications

### Message Bubbles:
- **Corner Radius**: 12dp (main corner), 4dp (other corners)
- **Elevation**: 2dp (sent messages only)
- **Padding**: 12dp horizontal, 10dp vertical
- **Max Width**: 75% of screen width
- **Min Height**: 40dp

### Input Bar:
- **Height**: 56dp (minimum touch target)
- **Padding**: 8dp internal, 16dp external
- **Corner Radius**: 8dp for input field
- **Button Size**: 48dp x 48dp (minimum)

### Spacing System:
- **xs**: 4dp
- **sm**: 8dp
- **md**: 16dp
- **lg**: 24dp

### Typography:
- **Message Text**: 16sp, regular
- **Timestamp**: 12sp, secondary color
- **Date Separator**: 14sp, medium weight

---

## ✅ Compatibility Guarantee

All improvements will:
- ✅ Use Support Library 28.0.0 (NOT AndroidX)
- ✅ Use only SDK 28 compatible APIs
- ✅ Work with Gradle 5.4.1
- ✅ Use `android.support.*` packages
- ✅ Maintain existing functionality
- ✅ Not break any Java code references

---

## 🚀 Implementation Order

1. **Colors** - Update color references first
2. **Drawable Resources** - Create new drawable files
3. **Message Bubbles** - Update left and right message views
4. **Input Bar** - Modernize send bar
5. **Date Separators** - Improve date display
6. **Status Indicators** - Enhance message status
7. **Overall Polish** - Final spacing and alignment

---

## 📊 Expected Results

After improvements:
- ✅ Modern, clean message bubble design
- ✅ Better visual hierarchy
- ✅ Improved readability
- ✅ Professional appearance
- ✅ Better user experience
- ✅ Consistent design language
- ✅ Smooth interactions

---

## ⚠️ Important Notes

- All IDs will remain the same (no Java code changes needed)
- All functionality will be preserved
- Only visual improvements will be made
- Backward compatible with existing code
- No new dependencies required

---

**Ready to proceed when you say "start"!** 🎉

```


## ChatRoomActivity improvement plan

Created a plan document (`CHATROOM_IMPROVEMENT_PLAN.md`) with the improvements. Summary:

### 10 improvement areas

1. Message bubbles (left & right)
   - Modern rounded corners (12dp/4dp)
   - Updated colors (indigo for sent, light gray for received)
   - Elevation/shadow effects
   - Improved padding and typography

2. Chat input bar
   - Rounded input field (8dp)
   - Better spacing and padding
   - Ripple effects on buttons
   - Improved send/voice/emoji buttons

3. Message list container
   - Better background colors
   - Improved padding
   - Better scrollbar styling

4. Date separators
   - Pill-shaped design
   - Better typography
   - Improved contrast

5. Message status indicators
   - Color-coded status (sending/sent/delivered/read)
   - Better icon sizing
   - Smooth transitions

6. Profile pictures
   - Subtle borders
   - Better spacing
   - Improved alignment

7. Media messages
   - Rounded corners on thumbnails
   - Better play button overlay
   - Improved loading states

8. Voice messages
   - Modern progress bar
   - Better play button design
   - Improved container styling

9. Overall layout
   - Consistent spacing system
   - Better use of ConstraintLayout
   - Improved background colors

10. Colors & theming
    - Apply modern color palette
    - Better contrast ratios
    - Consistent color usage

### Files to be created/updated

New drawables (6 files):
- Chat bubble backgrounds
- Input field backgrounds
- Button backgrounds
- Date separator backgrounds

Updated layouts (6 files):
- Main activity layout
- Content layout
- Send bar
- Message views (left & right)
- Date item

### Compatibility

- Support Library 28.0.0 (NOT AndroidX)
- SDK 28 compatible
- Gradle 5.4.1 compatible
- No breaking changes
- All existing IDs preserved

The plan document includes detailed specifications, spacing guidelines, and implementation order.

Say "start" when ready to begin code generation.

---

**User**

start

---

**Cursor**


Starting ChatRoomActivity improvements. Creating the initial todo list, then implementing the changes.



Checking existing chat bubble drawables, then creating the new ones:



Checking which drawables are currently used for message bubbles:



Creating the new drawable resources. Starting with the chat bubble backgrounds:


```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Modern received message bubble (left side) -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/message_left_background_color" />
    <corners
        android:topLeftRadius="4dp"
        android:topRightRadius="12dp"
        android:bottomLeftRadius="12dp"
        android:bottomRightRadius="12dp" />
    <padding
        android:left="12dp"
        android:top="10dp"
        android:right="12dp"
        android:bottom="10dp" />
</shape>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Modern sent message bubble (right side) with shadow -->
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Shadow layer -->
    <item
        android:left="0dp"
        android:right="0dp"
        android:top="2dp"
        android:bottom="0dp">
        <shape android:shape="rectangle">
            <solid android:color="#20000000" />
            <corners
                android:topLeftRadius="12dp"
                android:topRightRadius="4dp"
                android:bottomLeftRadius="12dp"
                android:bottomRightRadius="12dp" />
        </shape>
    </item>
    <!-- Main bubble -->
    <item
        android:left="0dp"
        android:right="0dp"
        android:top="0dp"
        android:bottom="2dp">
        <shape android:shape="rectangle">
            <solid android:color="@color/message_right_background_color" />
            <corners
                android:topLeftRadius="12dp"
                android:topRightRadius="4dp"
                android:bottomLeftRadius="12dp"
                android:bottomRightRadius="12dp" />
        </shape>
    </item>
</layer-list>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Modern received message bubble for media (left side) -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/message_left_background_color" />
    <corners
        android:topLeftRadius="4dp"
        android:topRightRadius="12dp"
        android:bottomLeftRadius="12dp"
        android:bottomRightRadius="12dp" />
</shape>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Modern sent message bubble for media (right side) with shadow -->
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Shadow layer -->
    <item
        android:left="0dp"
        android:right="0dp"
        android:top="2dp"
        android:bottom="0dp">
        <shape android:shape="rectangle">
            <solid android:color="#20000000" />
            <corners
                android:topLeftRadius="12dp"
                android:topRightRadius="4dp"
                android:bottomLeftRadius="12dp"
                android:bottomRightRadius="12dp" />
        </shape>
    </item>
    <!-- Main bubble -->
    <item
        android:left="0dp"
        android:right="0dp"
        android:top="0dp"
        android:bottom="2dp">
        <shape android:shape="rectangle">
            <solid android:color="@color/message_right_background_color" />
            <corners
                android:topLeftRadius="12dp"
                android:topRightRadius="4dp"
                android:bottomLeftRadius="12dp"
                android:bottomRightRadius="12dp" />
        </shape>
    </item>
</layer-list>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Modern input field background with focus states -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_focused="true">
        <shape android:shape="rectangle">
            <solid android:color="@color/white" />
            <stroke
                android:width="2dp"
                android:color="@color/editbox_border_focuscolor" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/white" />
            <stroke
                android:width="1dp"
                android:color="@color/editbox_border_color" />
            <corners android:radius="8dp" />
        </shape>
    </item>
</selector>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Modern send button with ripple effect -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="@color/colorPrimaryDark" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/colorPrimary" />
            <corners android:radius="8dp" />
        </shape>
    </item>
</selector>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Modern date separator pill design -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/view_backcolor" />
    <corners android:radius="12dp" />
    <stroke
        android:width="1dp"
        android:color="@color/divider_gray_color" />
</shape>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Modern voice message container background -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/message_left_background_color" />
    <corners android:radius="8dp" />
</shape>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Modern voice message container background for sent messages -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/message_right_background_color" />
    <corners android:radius="8dp" />
</shape>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Modern media thumbnail with rounded corners -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/transparent" />
    <corners android:radius="8dp" />
</shape>

```


Creating backward-compatible drawables for existing references, then updating the layout files:


```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Backward compatible: received message bubble (left side) -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/message_left_background_color" />
    <corners
        android:topLeftRadius="4dp"
        android:topRightRadius="12dp"
        android:bottomLeftRadius="12dp"
        android:bottomRightRadius="12dp" />
    <padding
        android:left="12dp"
        android:top="10dp"
        android:right="12dp"
        android:bottom="10dp" />
</shape>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Backward compatible: sent message bubble (right side) with shadow -->
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Shadow layer -->
    <item
        android:left="0dp"
        android:right="0dp"
        android:top="2dp"
        android:bottom="0dp">
        <shape android:shape="rectangle">
            <solid android:color="#20000000" />
            <corners
                android:topLeftRadius="12dp"
                android:topRightRadius="4dp"
                android:bottomLeftRadius="12dp"
                android:bottomRightRadius="12dp" />
        </shape>
    </item>
    <!-- Main bubble -->
    <item
        android:left="0dp"
        android:right="0dp"
        android:top="0dp"
        android:bottom="2dp">
        <shape android:shape="rectangle">
            <solid android:color="@color/message_right_background_color" />
            <corners
                android:topLeftRadius="12dp"
                android:topRightRadius="4dp"
                android:bottomLeftRadius="12dp"
                android:bottomRightRadius="12dp" />
        </shape>
    </item>
</layer-list>

```


Updating the layout files, starting with `message_view_left.xml`:



Updating `message_view_left.xml` with modern improvements:


```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <LinearLayout
                android:id="@+id/lyt_profile"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <com.multimediachat.ui.views.CircularImageView
                    android:id="@+id/imgProfile"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="@dimen/chat_profile_img_size"
                    android:layout_alignTop="@+id/messageBody"
                    android:layout_alignParentLeft="true"
                    android:src="@drawable/profilephoto"
                    android:visibility="visible"/>

                <TextView
                    android:id="@+id/fromName"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:singleLine="true"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:layout_marginTop="5dp"
                    android:textColor="@color/from_name_color"
                    android:textSize="@dimen/font_level_2" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_below="@+id/messageBody"
                android:layout_alignLeft="@+id/messageBody"
                android:layout_marginTop="5dip"
                android:layout_marginLeft="10dip">

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="15dp"
                    android:layout_height="15dp"
                    android:layout_gravity="center"
                    android:layout_marginRight="3dp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"

                    android:layout_marginRight="2dp"
                    android:textColor="@color/blue"
                    android:textSize="@dimen/font_level_1"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="2dip"
                    android:layout_marginRight="5dip"
                    android:textColor="@color/from_name_color"
                    android:textSize="@dimen/chat_msg_time_text_size" />
            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="3dp"
                android:layout_marginRight="30dp"
                android:layout_toRightOf="@+id/lyt_profile"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/font_text_color"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingLeft="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="visible">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_from_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="match_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_from_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/font_text_color"
                            android:textSize="@dimen/font_level_3"/>

                    </RelativeLayout>

                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_from_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="visible" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <LinearLayout
                android:id="@+id/lyt_profile"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <com.multimediachat.ui.views.CircularImageView
                    android:id="@+id/imgProfile"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="@dimen/chat_profile_img_size"
                    android:layout_alignTop="@+id/messageBody"
                    android:layout_alignParentLeft="true"
                    android:src="@drawable/profilephoto"
                    android:padding="2dp"
                    android:background="@drawable/profile_border"
                    android:visibility="visible"/>

                <TextView
                    android:id="@+id/fromName"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:singleLine="true"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:layout_marginTop="5dp"
                    android:textColor="@color/from_name_color"
                    android:textSize="@dimen/font_level_2" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_below="@+id/messageBody"
                android:layout_alignLeft="@+id/messageBody"
                android:layout_marginTop="5dip"
                android:layout_marginLeft="10dip">

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="15dp"
                    android:layout_height="15dp"
                    android:layout_gravity="center"
                    android:layout_marginRight="3dp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"

                    android:layout_marginRight="2dp"
                    android:textColor="@color/blue"
                    android:textSize="@dimen/font_level_1"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="2dip"
                    android:layout_marginRight="5dip"
                    android:textColor="@color/from_name_color"
                    android:textSize="@dimen/chat_msg_time_text_size" />
            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="3dp"
                android:layout_marginRight="30dp"
                android:layout_toRightOf="@+id/lyt_profile"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/font_text_color"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingLeft="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="visible">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_from_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="match_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_from_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/font_text_color"
                            android:textSize="@dimen/font_level_3"/>

                    </RelativeLayout>

                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_from_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="visible" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <LinearLayout
                android:id="@+id/lyt_profile"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <com.multimediachat.ui.views.CircularImageView
                    android:id="@+id/imgProfile"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="@dimen/chat_profile_img_size"
                    android:layout_alignTop="@+id/messageBody"
                    android:layout_alignParentLeft="true"
                    android:src="@drawable/profilephoto"
                    android:padding="2dp"
                    android:background="@drawable/profile_border"
                    android:visibility="visible"/>

                <TextView
                    android:id="@+id/fromName"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:singleLine="true"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:layout_marginTop="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp"
                    android:maxLines="1" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_below="@+id/messageBody"
                android:layout_alignLeft="@+id/messageBody"
                android:layout_marginTop="5dip"
                android:layout_marginLeft="10dip">

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="15dp"
                    android:layout_height="15dp"
                    android:layout_gravity="center"
                    android:layout_marginRight="3dp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"

                    android:layout_marginRight="2dp"
                    android:textColor="@color/blue"
                    android:textSize="@dimen/font_level_1"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="2dip"
                    android:layout_marginRight="5dip"
                    android:textColor="@color/from_name_color"
                    android:textSize="@dimen/chat_msg_time_text_size" />
            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="3dp"
                android:layout_marginRight="30dp"
                android:layout_toRightOf="@+id/lyt_profile"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/font_text_color"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingLeft="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="visible">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_from_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="match_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_from_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/font_text_color"
                            android:textSize="@dimen/font_level_3"/>

                    </RelativeLayout>

                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_from_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="visible" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <LinearLayout
                android:id="@+id/lyt_profile"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <com.multimediachat.ui.views.CircularImageView
                    android:id="@+id/imgProfile"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="@dimen/chat_profile_img_size"
                    android:layout_alignTop="@+id/messageBody"
                    android:layout_alignParentLeft="true"
                    android:src="@drawable/profilephoto"
                    android:padding="2dp"
                    android:background="@drawable/profile_border"
                    android:visibility="visible"/>

                <TextView
                    android:id="@+id/fromName"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:singleLine="true"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:layout_marginTop="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp"
                    android:maxLines="1" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_below="@+id/messageBody"
                android:layout_alignLeft="@+id/messageBody"
                android:layout_marginTop="4dp"
                android:layout_marginLeft="12dp"
                android:gravity="center_vertical">

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="15dp"
                    android:layout_height="15dp"
                    android:layout_gravity="center"
                    android:layout_marginRight="3dp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"

                    android:layout_marginRight="2dp"
                    android:textColor="@color/blue"
                    android:textSize="@dimen/font_level_1"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="2dip"
                    android:layout_marginRight="5dip"
                    android:textColor="@color/from_name_color"
                    android:textSize="@dimen/chat_msg_time_text_size" />
            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="3dp"
                android:layout_marginRight="30dp"
                android:layout_toRightOf="@+id/lyt_profile"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/font_text_color"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingLeft="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="visible">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_from_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="match_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_from_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/font_text_color"
                            android:textSize="@dimen/font_level_3"/>

                    </RelativeLayout>

                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_from_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="visible" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <LinearLayout
                android:id="@+id/lyt_profile"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <com.multimediachat.ui.views.CircularImageView
                    android:id="@+id/imgProfile"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="@dimen/chat_profile_img_size"
                    android:layout_alignTop="@+id/messageBody"
                    android:layout_alignParentLeft="true"
                    android:src="@drawable/profilephoto"
                    android:padding="2dp"
                    android:background="@drawable/profile_border"
                    android:visibility="visible"/>

                <TextView
                    android:id="@+id/fromName"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:singleLine="true"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:layout_marginTop="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp"
                    android:maxLines="1" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_below="@+id/messageBody"
                android:layout_alignLeft="@+id/messageBody"
                android:layout_marginTop="4dp"
                android:layout_marginLeft="12dp"
                android:gravity="center_vertical">

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"

                    android:layout_marginRight="2dp"
                    android:textColor="@color/blue"
                    android:textSize="@dimen/font_level_1"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="2dip"
                    android:layout_marginRight="5dip"
                    android:textColor="@color/from_name_color"
                    android:textSize="@dimen/chat_msg_time_text_size" />
            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="3dp"
                android:layout_marginRight="30dp"
                android:layout_toRightOf="@+id/lyt_profile"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/font_text_color"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingLeft="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="visible">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_from_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="match_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_from_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/font_text_color"
                            android:textSize="@dimen/font_level_3"/>

                    </RelativeLayout>

                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_from_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="visible" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <LinearLayout
                android:id="@+id/lyt_profile"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <com.multimediachat.ui.views.CircularImageView
                    android:id="@+id/imgProfile"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="@dimen/chat_profile_img_size"
                    android:layout_alignTop="@+id/messageBody"
                    android:layout_alignParentLeft="true"
                    android:src="@drawable/profilephoto"
                    android:padding="2dp"
                    android:background="@drawable/profile_border"
                    android:visibility="visible"/>

                <TextView
                    android:id="@+id/fromName"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:singleLine="true"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:layout_marginTop="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp"
                    android:maxLines="1" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_below="@+id/messageBody"
                android:layout_alignLeft="@+id/messageBody"
                android:layout_marginTop="4dp"
                android:layout_marginLeft="12dp"
                android:gravity="center_vertical">

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/colorPrimary"
                    android:textSize="12sp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="2dip"
                    android:layout_marginRight="5dip"
                    android:textColor="@color/from_name_color"
                    android:textSize="@dimen/chat_msg_time_text_size" />
            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="3dp"
                android:layout_marginRight="30dp"
                android:layout_toRightOf="@+id/lyt_profile"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/font_text_color"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingLeft="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="visible">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_from_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="match_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_from_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/font_text_color"
                            android:textSize="@dimen/font_level_3"/>

                    </RelativeLayout>

                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_from_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="visible" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <LinearLayout
                android:id="@+id/lyt_profile"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <com.multimediachat.ui.views.CircularImageView
                    android:id="@+id/imgProfile"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="@dimen/chat_profile_img_size"
                    android:layout_alignTop="@+id/messageBody"
                    android:layout_alignParentLeft="true"
                    android:src="@drawable/profilephoto"
                    android:padding="2dp"
                    android:background="@drawable/profile_border"
                    android:visibility="visible"/>

                <TextView
                    android:id="@+id/fromName"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:singleLine="true"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:layout_marginTop="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp"
                    android:maxLines="1" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_below="@+id/messageBody"
                android:layout_alignLeft="@+id/messageBody"
                android:layout_marginTop="4dp"
                android:layout_marginLeft="12dp"
                android:gravity="center_vertical">

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/colorPrimary"
                    android:textSize="12sp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />
            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="3dp"
                android:layout_marginRight="30dp"
                android:layout_toRightOf="@+id/lyt_profile"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/font_text_color"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingLeft="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="visible">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_from_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="match_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_from_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/font_text_color"
                            android:textSize="@dimen/font_level_3"/>

                    </RelativeLayout>

                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_from_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="visible" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <LinearLayout
                android:id="@+id/lyt_profile"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <com.multimediachat.ui.views.CircularImageView
                    android:id="@+id/imgProfile"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="@dimen/chat_profile_img_size"
                    android:layout_alignTop="@+id/messageBody"
                    android:layout_alignParentLeft="true"
                    android:src="@drawable/profilephoto"
                    android:padding="2dp"
                    android:background="@drawable/profile_border"
                    android:visibility="visible"/>

                <TextView
                    android:id="@+id/fromName"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:singleLine="true"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:layout_marginTop="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp"
                    android:maxLines="1" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_below="@+id/messageBody"
                android:layout_alignLeft="@+id/messageBody"
                android:layout_marginTop="4dp"
                android:layout_marginLeft="12dp"
                android:gravity="center_vertical">

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/colorPrimary"
                    android:textSize="12sp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />
            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="8dp"
                android:layout_marginRight="8dp"
                android:layout_toRightOf="@+id/lyt_profile"
                android:maxWidth="75%"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/font_text_color"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingLeft="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="visible">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_from_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="match_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_from_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/font_text_color"
                            android:textSize="@dimen/font_level_3"/>

                    </RelativeLayout>

                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_from_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="visible" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <LinearLayout
                android:id="@+id/lyt_profile"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <com.multimediachat.ui.views.CircularImageView
                    android:id="@+id/imgProfile"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="@dimen/chat_profile_img_size"
                    android:layout_alignTop="@+id/messageBody"
                    android:layout_alignParentLeft="true"
                    android:src="@drawable/profilephoto"
                    android:padding="2dp"
                    android:background="@drawable/profile_border"
                    android:visibility="visible"/>

                <TextView
                    android:id="@+id/fromName"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:singleLine="true"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:layout_marginTop="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp"
                    android:maxLines="1" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_below="@+id/messageBody"
                android:layout_alignLeft="@+id/messageBody"
                android:layout_marginTop="4dp"
                android:layout_marginLeft="12dp"
                android:gravity="center_vertical">

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/colorPrimary"
                    android:textSize="12sp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />
            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="8dp"
                android:layout_marginRight="8dp"
                android:layout_toRightOf="@+id/lyt_profile"
                android:maxWidth="75%"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:minHeight="40dp"
                    android:orientation="horizontal"
                    android:maxWidth="100%">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:paddingLeft="12dp"
                        android:paddingRight="12dp"
                        android:paddingTop="10dp"
                        android:paddingBottom="10dp"
                        android:textColor="@color/message_left_text_color"
                        android:textSize="16sp"
                        android:lineSpacingMultiplier="1.2"
                        android:maxWidth="100%" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingLeft="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="visible">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_from_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="match_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_from_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/font_text_color"
                            android:textSize="@dimen/font_level_3"/>

                    </RelativeLayout>

                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_from_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="visible" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <LinearLayout
                android:id="@+id/lyt_profile"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <com.multimediachat.ui.views.CircularImageView
                    android:id="@+id/imgProfile"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="@dimen/chat_profile_img_size"
                    android:layout_alignTop="@+id/messageBody"
                    android:layout_alignParentLeft="true"
                    android:src="@drawable/profilephoto"
                    android:padding="2dp"
                    android:background="@drawable/profile_border"
                    android:visibility="visible"/>

                <TextView
                    android:id="@+id/fromName"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:singleLine="true"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:layout_marginTop="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp"
                    android:maxLines="1" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_below="@+id/messageBody"
                android:layout_alignLeft="@+id/messageBody"
                android:layout_marginTop="4dp"
                android:layout_marginLeft="12dp"
                android:gravity="center_vertical">

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/colorPrimary"
                    android:textSize="12sp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />
            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="8dp"
                android:layout_marginRight="8dp"
                android:layout_toRightOf="@+id/lyt_profile"
                android:maxWidth="75%"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:minHeight="40dp"
                    android:orientation="horizontal"
                    android:maxWidth="100%">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:paddingLeft="12dp"
                        android:paddingRight="12dp"
                        android:paddingTop="10dp"
                        android:paddingBottom="10dp"
                        android:textColor="@color/message_left_text_color"
                        android:textSize="16sp"
                        android:lineSpacingMultiplier="1.2"
                        android:maxWidth="100%" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_bubble_left_media"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:background="@drawable/media_thumbnail_background"
                        android:scaleType="centerCrop"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingLeft="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="visible">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_from_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="match_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_from_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/font_text_color"
                            android:textSize="@dimen/font_level_3"/>

                    </RelativeLayout>

                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_from_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="visible" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <LinearLayout
                android:id="@+id/lyt_profile"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <com.multimediachat.ui.views.CircularImageView
                    android:id="@+id/imgProfile"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="@dimen/chat_profile_img_size"
                    android:layout_alignTop="@+id/messageBody"
                    android:layout_alignParentLeft="true"
                    android:src="@drawable/profilephoto"
                    android:padding="2dp"
                    android:background="@drawable/profile_border"
                    android:visibility="visible"/>

                <TextView
                    android:id="@+id/fromName"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:singleLine="true"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:layout_marginTop="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp"
                    android:maxLines="1" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_below="@+id/messageBody"
                android:layout_alignLeft="@+id/messageBody"
                android:layout_marginTop="4dp"
                android:layout_marginLeft="12dp"
                android:gravity="center_vertical">

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/colorPrimary"
                    android:textSize="12sp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />
            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="8dp"
                android:layout_marginRight="8dp"
                android:layout_toRightOf="@+id/lyt_profile"
                android:maxWidth="75%"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:minHeight="40dp"
                    android:orientation="horizontal"
                    android:maxWidth="100%">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:paddingLeft="12dp"
                        android:paddingRight="12dp"
                        android:paddingTop="10dp"
                        android:paddingBottom="10dp"
                        android:textColor="@color/message_left_text_color"
                        android:textSize="16sp"
                        android:lineSpacingMultiplier="1.2"
                        android:maxWidth="100%" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_bubble_left_media"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:background="@drawable/media_thumbnail_background"
                        android:scaleType="centerCrop"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingLeft="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="visible">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="48dp"
                        android:background="@drawable/voice_message_background"
                        android:orientation="horizontal"
                        android:padding="8dp">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="match_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_from_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/font_text_color"
                            android:textSize="@dimen/font_level_3"/>

                    </RelativeLayout>

                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_from_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="visible" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <LinearLayout
                android:id="@+id/lyt_profile"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <com.multimediachat.ui.views.CircularImageView
                    android:id="@+id/imgProfile"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="@dimen/chat_profile_img_size"
                    android:layout_alignTop="@+id/messageBody"
                    android:layout_alignParentLeft="true"
                    android:src="@drawable/profilephoto"
                    android:padding="2dp"
                    android:background="@drawable/profile_border"
                    android:visibility="visible"/>

                <TextView
                    android:id="@+id/fromName"
                    android:layout_width="@dimen/chat_profile_img_size"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:singleLine="true"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:layout_marginTop="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp"
                    android:maxLines="1" />

            </LinearLayout>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_below="@+id/messageBody"
                android:layout_alignLeft="@+id/messageBody"
                android:layout_marginTop="4dp"
                android:layout_marginLeft="12dp"
                android:gravity="center_vertical">

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/colorPrimary"
                    android:textSize="12sp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="8dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />
            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="8dp"
                android:layout_marginRight="8dp"
                android:layout_toRightOf="@+id/lyt_profile"
                android:maxWidth="75%"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_from_item_bg"
                    android:minHeight="40dp"
                    android:orientation="horizontal"
                    android:maxWidth="100%">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:paddingLeft="12dp"
                        android:paddingRight="12dp"
                        android:paddingTop="10dp"
                        android:paddingBottom="10dp"
                        android:textColor="@color/message_left_text_color"
                        android:textSize="16sp"
                        android:lineSpacingMultiplier="1.2"
                        android:maxWidth="100%" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_bubble_left_media"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:background="@drawable/media_thumbnail_background"
                        android:scaleType="centerCrop"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingLeft="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="visible">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="48dp"
                        android:background="@drawable/voice_message_background"
                        android:orientation="horizontal"
                        android:padding="8dp">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="match_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_from_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="12dp"
                            android:textColor="@color/message_left_text_color"
                            android:textSize="14sp"
                            android:textStyle="normal"/>

                    </RelativeLayout>

                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_from_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="visible" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```


Creating the profile border drawable, then updating `message_view_right.xml`:


```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Profile picture border -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="@color/transparent" />
    <stroke
        android:width="2dp"
        android:color="@color/circular_image_border" />
</shape>

```


Updating `message_view_right.xml` for sent messages:


```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_gravity="right"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgProfile"
                android:layout_width="@dimen/chat_profile_img_size"
                android:layout_height="@dimen/chat_profile_img_size"
                android:layout_alignParentRight="true"
                android:layout_alignTop="@+id/messageBody"
                android:src="@drawable/profilephoto"
                android:visibility="gone"/>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_alignParentRight="true"
                android:layout_below="@+id/messageBody"
                android:layout_marginRight="10dip"
                android:layout_marginTop="5dip">

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="2dip"
                    android:layout_marginRight="5dip"
                    android:textColor="@color/from_name_color"
                    android:textSize="@dimen/chat_msg_time_text_size" />

                <ImageView
                    android:id="@+id/ic_send_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="10dp"
                    android:layout_height="10dp"
                    android:visibility="gone"/>

                <ImageView
                    android:id="@+id/ic_received_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="10dp"
                    android:layout_height="10dp"
                    android:layout_marginLeft="5dp"
                    android:visibility="gone"/>

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="15sp"
                    android:layout_height="15sp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="3dp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:textColor="@color/blue"
                    android:textSize="@dimen/font_level_1"
                    android:visibility="gone" />

            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="30dp"
                android:layout_marginRight="3dp"
                android:layout_alignParentRight="true"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:layout_gravity="right"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/white"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingRight="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="gone">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_to_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="fill_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_to_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/white"
                            android:textSize="@dimen/font_level_3" />
                    </RelativeLayout>
                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_to_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="10dp"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/mediaupload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_gravity="right"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgProfile"
                android:layout_width="@dimen/chat_profile_img_size"
                android:layout_height="@dimen/chat_profile_img_size"
                android:layout_alignParentRight="true"
                android:layout_alignTop="@+id/messageBody"
                android:src="@drawable/profilephoto"
                android:visibility="gone"/>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_alignParentRight="true"
                android:layout_below="@+id/messageBody"
                android:layout_marginRight="12dp"
                android:layout_marginTop="4dp"
                android:gravity="center_vertical">

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="2dip"
                    android:layout_marginRight="5dip"
                    android:textColor="@color/from_name_color"
                    android:textSize="@dimen/chat_msg_time_text_size" />

                <ImageView
                    android:id="@+id/ic_send_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="10dp"
                    android:layout_height="10dp"
                    android:visibility="gone"/>

                <ImageView
                    android:id="@+id/ic_received_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="10dp"
                    android:layout_height="10dp"
                    android:layout_marginLeft="5dp"
                    android:visibility="gone"/>

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="15sp"
                    android:layout_height="15sp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="3dp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:textColor="@color/blue"
                    android:textSize="@dimen/font_level_1"
                    android:visibility="gone" />

            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="30dp"
                android:layout_marginRight="3dp"
                android:layout_alignParentRight="true"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:layout_gravity="right"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/white"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingRight="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="gone">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_to_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="fill_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_to_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/white"
                            android:textSize="@dimen/font_level_3" />
                    </RelativeLayout>
                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_to_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="10dp"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/mediaupload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_gravity="right"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgProfile"
                android:layout_width="@dimen/chat_profile_img_size"
                android:layout_height="@dimen/chat_profile_img_size"
                android:layout_alignParentRight="true"
                android:layout_alignTop="@+id/messageBody"
                android:src="@drawable/profilephoto"
                android:visibility="gone"/>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_alignParentRight="true"
                android:layout_below="@+id/messageBody"
                android:layout_marginRight="12dp"
                android:layout_marginTop="4dp"
                android:gravity="center_vertical">

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />

                <ImageView
                    android:id="@+id/ic_send_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="10dp"
                    android:layout_height="10dp"
                    android:visibility="gone"/>

                <ImageView
                    android:id="@+id/ic_received_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="10dp"
                    android:layout_height="10dp"
                    android:layout_marginLeft="5dp"
                    android:visibility="gone"/>

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="15sp"
                    android:layout_height="15sp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="3dp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:textColor="@color/blue"
                    android:textSize="@dimen/font_level_1"
                    android:visibility="gone" />

            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="30dp"
                android:layout_marginRight="3dp"
                android:layout_alignParentRight="true"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:layout_gravity="right"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/white"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingRight="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="gone">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_to_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="fill_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_to_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/white"
                            android:textSize="@dimen/font_level_3" />
                    </RelativeLayout>
                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_to_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="10dp"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/mediaupload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_gravity="right"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgProfile"
                android:layout_width="@dimen/chat_profile_img_size"
                android:layout_height="@dimen/chat_profile_img_size"
                android:layout_alignParentRight="true"
                android:layout_alignTop="@+id/messageBody"
                android:src="@drawable/profilephoto"
                android:visibility="gone"/>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_alignParentRight="true"
                android:layout_below="@+id/messageBody"
                android:layout_marginRight="12dp"
                android:layout_marginTop="4dp"
                android:gravity="center_vertical">

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />

                <ImageView
                    android:id="@+id/ic_send_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="2dp"
                    android:tint="@color/colorPrimary"
                    android:visibility="gone"/>

                <ImageView
                    android:id="@+id/ic_received_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="2dp"
                    android:layout_marginRight="4dp"
                    android:tint="@color/colorAccent"
                    android:visibility="gone"/>

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="15sp"
                    android:layout_height="15sp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="3dp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:textColor="@color/blue"
                    android:textSize="@dimen/font_level_1"
                    android:visibility="gone" />

            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="30dp"
                android:layout_marginRight="3dp"
                android:layout_alignParentRight="true"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:layout_gravity="right"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/white"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingRight="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="gone">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_to_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="fill_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_to_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/white"
                            android:textSize="@dimen/font_level_3" />
                    </RelativeLayout>
                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_to_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="10dp"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/mediaupload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_gravity="right"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgProfile"
                android:layout_width="@dimen/chat_profile_img_size"
                android:layout_height="@dimen/chat_profile_img_size"
                android:layout_alignParentRight="true"
                android:layout_alignTop="@+id/messageBody"
                android:src="@drawable/profilephoto"
                android:visibility="gone"/>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_alignParentRight="true"
                android:layout_below="@+id/messageBody"
                android:layout_marginRight="12dp"
                android:layout_marginTop="4dp"
                android:gravity="center_vertical">

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />

                <ImageView
                    android:id="@+id/ic_send_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="2dp"
                    android:tint="@color/colorPrimary"
                    android:visibility="gone"/>

                <ImageView
                    android:id="@+id/ic_received_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="2dp"
                    android:layout_marginRight="4dp"
                    android:tint="@color/colorAccent"
                    android:visibility="gone"/>

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:textColor="@color/blue"
                    android:textSize="@dimen/font_level_1"
                    android:visibility="gone" />

            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="30dp"
                android:layout_marginRight="3dp"
                android:layout_alignParentRight="true"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:layout_gravity="right"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/white"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingRight="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="gone">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_to_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="fill_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_to_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/white"
                            android:textSize="@dimen/font_level_3" />
                    </RelativeLayout>
                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_to_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="10dp"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/mediaupload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_gravity="right"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgProfile"
                android:layout_width="@dimen/chat_profile_img_size"
                android:layout_height="@dimen/chat_profile_img_size"
                android:layout_alignParentRight="true"
                android:layout_alignTop="@+id/messageBody"
                android:src="@drawable/profilephoto"
                android:visibility="gone"/>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_alignParentRight="true"
                android:layout_below="@+id/messageBody"
                android:layout_marginRight="12dp"
                android:layout_marginTop="4dp"
                android:gravity="center_vertical">

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />

                <ImageView
                    android:id="@+id/ic_send_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="2dp"
                    android:tint="@color/colorPrimary"
                    android:visibility="gone"/>

                <ImageView
                    android:id="@+id/ic_received_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="2dp"
                    android:layout_marginRight="4dp"
                    android:tint="@color/colorAccent"
                    android:visibility="gone"/>

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/colorPrimary"
                    android:textSize="12sp"
                    android:visibility="gone" />

            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="30dp"
                android:layout_marginRight="3dp"
                android:layout_alignParentRight="true"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:layout_gravity="right"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/white"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingRight="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="gone">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_to_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="fill_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_to_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/white"
                            android:textSize="@dimen/font_level_3" />
                    </RelativeLayout>
                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_to_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="10dp"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/mediaupload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_gravity="right"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgProfile"
                android:layout_width="@dimen/chat_profile_img_size"
                android:layout_height="@dimen/chat_profile_img_size"
                android:layout_alignParentRight="true"
                android:layout_alignTop="@+id/messageBody"
                android:src="@drawable/profilephoto"
                android:visibility="gone"/>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_alignParentRight="true"
                android:layout_below="@+id/messageBody"
                android:layout_marginRight="12dp"
                android:layout_marginTop="4dp"
                android:gravity="center_vertical">

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />

                <ImageView
                    android:id="@+id/ic_send_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="2dp"
                    android:tint="@color/colorPrimary"
                    android:visibility="gone"/>

                <ImageView
                    android:id="@+id/ic_received_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="2dp"
                    android:layout_marginRight="4dp"
                    android:tint="@color/colorAccent"
                    android:visibility="gone"/>

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/colorPrimary"
                    android:textSize="12sp"
                    android:visibility="gone" />

            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="8dp"
                android:layout_marginRight="8dp"
                android:layout_alignParentRight="true"
                android:maxWidth="75%"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:minHeight="@dimen/chat_profile_img_size"
                    android:layout_gravity="right"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:padding="10dp"
                        android:textColor="@color/white"
                        android:textSize="@dimen/font_level_3" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingRight="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="gone">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_to_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="fill_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_to_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/white"
                            android:textSize="@dimen/font_level_3" />
                    </RelativeLayout>
                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_to_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="10dp"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/mediaupload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_gravity="right"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgProfile"
                android:layout_width="@dimen/chat_profile_img_size"
                android:layout_height="@dimen/chat_profile_img_size"
                android:layout_alignParentRight="true"
                android:layout_alignTop="@+id/messageBody"
                android:src="@drawable/profilephoto"
                android:visibility="gone"/>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_alignParentRight="true"
                android:layout_below="@+id/messageBody"
                android:layout_marginRight="12dp"
                android:layout_marginTop="4dp"
                android:gravity="center_vertical">

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />

                <ImageView
                    android:id="@+id/ic_send_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="2dp"
                    android:tint="@color/colorPrimary"
                    android:visibility="gone"/>

                <ImageView
                    android:id="@+id/ic_received_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="2dp"
                    android:layout_marginRight="4dp"
                    android:tint="@color/colorAccent"
                    android:visibility="gone"/>

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/colorPrimary"
                    android:textSize="12sp"
                    android:visibility="gone" />

            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="8dp"
                android:layout_marginRight="8dp"
                android:layout_alignParentRight="true"
                android:maxWidth="75%"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:minHeight="40dp"
                    android:layout_gravity="right"
                    android:orientation="horizontal"
                    android:maxWidth="100%">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:paddingLeft="12dp"
                        android:paddingRight="12dp"
                        android:paddingTop="10dp"
                        android:paddingBottom="10dp"
                        android:textColor="@color/message_right_text_color"
                        android:textSize="16sp"
                        android:lineSpacingMultiplier="1.2"
                        android:maxWidth="100%" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingRight="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="gone">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_to_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="fill_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_to_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/white"
                            android:textSize="@dimen/font_level_3" />
                    </RelativeLayout>
                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_to_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="10dp"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/mediaupload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_gravity="right"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgProfile"
                android:layout_width="@dimen/chat_profile_img_size"
                android:layout_height="@dimen/chat_profile_img_size"
                android:layout_alignParentRight="true"
                android:layout_alignTop="@+id/messageBody"
                android:src="@drawable/profilephoto"
                android:visibility="gone"/>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_alignParentRight="true"
                android:layout_below="@+id/messageBody"
                android:layout_marginRight="12dp"
                android:layout_marginTop="4dp"
                android:gravity="center_vertical">

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />

                <ImageView
                    android:id="@+id/ic_send_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="2dp"
                    android:tint="@color/colorPrimary"
                    android:visibility="gone"/>

                <ImageView
                    android:id="@+id/ic_received_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="2dp"
                    android:layout_marginRight="4dp"
                    android:tint="@color/colorAccent"
                    android:visibility="gone"/>

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/colorPrimary"
                    android:textSize="12sp"
                    android:visibility="gone" />

            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="8dp"
                android:layout_marginRight="8dp"
                android:layout_alignParentRight="true"
                android:maxWidth="75%"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:minHeight="40dp"
                    android:layout_gravity="right"
                    android:orientation="horizontal"
                    android:maxWidth="100%">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:paddingLeft="12dp"
                        android:paddingRight="12dp"
                        android:paddingTop="10dp"
                        android:paddingBottom="10dp"
                        android:textColor="@color/message_right_text_color"
                        android:textSize="16sp"
                        android:lineSpacingMultiplier="1.2"
                        android:maxWidth="100%" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_bubble_right_media"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:background="@drawable/media_thumbnail_background"
                        android:scaleType="centerCrop"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingRight="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="gone">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="42dp"
                        android:background="@drawable/chat_to_item_bg"
                        android:orientation="horizontal">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="fill_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_to_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/white"
                            android:textSize="@dimen/font_level_3" />
                    </RelativeLayout>
                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_to_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="10dp"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/mediaupload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_gravity="right"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgProfile"
                android:layout_width="@dimen/chat_profile_img_size"
                android:layout_height="@dimen/chat_profile_img_size"
                android:layout_alignParentRight="true"
                android:layout_alignTop="@+id/messageBody"
                android:src="@drawable/profilephoto"
                android:visibility="gone"/>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_alignParentRight="true"
                android:layout_below="@+id/messageBody"
                android:layout_marginRight="12dp"
                android:layout_marginTop="4dp"
                android:gravity="center_vertical">

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />

                <ImageView
                    android:id="@+id/ic_send_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="2dp"
                    android:tint="@color/colorPrimary"
                    android:visibility="gone"/>

                <ImageView
                    android:id="@+id/ic_received_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="2dp"
                    android:layout_marginRight="4dp"
                    android:tint="@color/colorAccent"
                    android:visibility="gone"/>

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/colorPrimary"
                    android:textSize="12sp"
                    android:visibility="gone" />

            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="8dp"
                android:layout_marginRight="8dp"
                android:layout_alignParentRight="true"
                android:maxWidth="75%"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:minHeight="40dp"
                    android:layout_gravity="right"
                    android:orientation="horizontal"
                    android:maxWidth="100%">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:paddingLeft="12dp"
                        android:paddingRight="12dp"
                        android:paddingTop="10dp"
                        android:paddingBottom="10dp"
                        android:textColor="@color/message_right_text_color"
                        android:textSize="16sp"
                        android:lineSpacingMultiplier="1.2"
                        android:maxWidth="100%" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_bubble_right_media"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:background="@drawable/media_thumbnail_background"
                        android:scaleType="centerCrop"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingRight="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="gone">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="48dp"
                        android:background="@drawable/voice_message_background_sent"
                        android:orientation="horizontal"
                        android:padding="8dp">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="fill_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_to_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="10dp"
                            android:textColor="@color/white"
                            android:textSize="@dimen/font_level_3" />
                    </RelativeLayout>
                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_to_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="10dp"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/mediaupload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<view xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootView"
    class="com.multimediachat.ui.views.MessageView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:orientation="vertical">

    <include
        android:id="@+id/layout_chat_date"
        layout="@layout/chat_date_item"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <RelativeLayout
            android:id="@+id/layout_chat_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_gravity="right"
            android:layout_marginBottom="16dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgProfile"
                android:layout_width="@dimen/chat_profile_img_size"
                android:layout_height="@dimen/chat_profile_img_size"
                android:layout_alignParentRight="true"
                android:layout_alignTop="@+id/messageBody"
                android:src="@drawable/profilephoto"
                android:visibility="gone"/>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_alignParentRight="true"
                android:layout_below="@+id/messageBody"
                android:layout_marginRight="12dp"
                android:layout_marginTop="4dp"
                android:gravity="center_vertical">

                <TextView
                    android:id="@+id/messagets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_vertical"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/secondary_text"
                    android:textSize="12sp" />

                <ImageView
                    android:id="@+id/ic_send_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="4dp"
                    android:layout_marginRight="2dp"
                    android:tint="@color/colorPrimary"
                    android:visibility="gone"/>

                <ImageView
                    android:id="@+id/ic_received_msg"
                    android:src="@drawable/ic_send_msg"
                    android:layout_gravity="center_vertical"
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:layout_marginLeft="2dp"
                    android:layout_marginRight="4dp"
                    android:tint="@color/colorAccent"
                    android:visibility="gone"/>

                <ProgressBar
                    android:id="@+id/loading"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:indeterminateTint="@color/secondary_text"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/txtDelivery"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="4dp"
                    android:textColor="@color/colorPrimary"
                    android:textSize="12sp"
                    android:visibility="gone" />

            </LinearLayout>

            <FrameLayout
                android:id="@+id/messageBody"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="8dp"
                android:layout_marginRight="8dp"
                android:layout_alignParentRight="true"
                android:maxWidth="75%"
                android:orientation="vertical">

                <LinearLayout
                    android:id="@+id/text_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_to_item_bg"
                    android:minHeight="40dp"
                    android:layout_gravity="right"
                    android:orientation="horizontal"
                    android:maxWidth="100%">

                    <TextView
                        android:id="@+id/message_text"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:paddingLeft="12dp"
                        android:paddingRight="12dp"
                        android:paddingTop="10dp"
                        android:paddingBottom="10dp"
                        android:textColor="@color/message_right_text_color"
                        android:textSize="16sp"
                        android:lineSpacingMultiplier="1.2"
                        android:maxWidth="100%" />

                    <ImageView
                        android:id="@+id/call_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center_vertical"
                        android:layout_marginRight="10dp" />
                </LinearLayout>

                <RelativeLayout
                    android:id="@+id/media_layout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/chat_bubble_right_media"
                    android:visibility="visible">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/media_thumbnail"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:background="@drawable/media_thumbnail_background"
                        android:scaleType="centerCrop"
                        android:contentDescription="@string/desc_image" />

                    <ImageView
                        android:id="@+id/media_video_icon"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:paddingRight="5dp"
                        android:src="@drawable/chatbox_playvideo" />

                </RelativeLayout>

                <FrameLayout
                    android:id="@+id/audioClickingArea"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:visibility="gone">

                    <RelativeLayout
                        android:layout_width="@dimen/chat_audio_widget_width"
                        android:layout_height="48dp"
                        android:background="@drawable/voice_message_background_sent"
                        android:orientation="horizontal"
                        android:padding="8dp">

                        <ProgressBar
                            android:id="@+id/audioProgressBar"
                            style="?android:attr/progressBarStyleHorizontal"
                            android:layout_width="fill_parent"
                            android:layout_height="3dp"
                            android:layout_alignParentBottom="true"
                            android:layout_marginLeft="10dp"
                            android:layout_marginRight="10dp"
                            android:layout_marginBottom="5dp"
                            android:max="100"
                            android:progress="0"
                            android:progressDrawable="@drawable/chat_room_to_voice_progress_clip" />

                        <com.multimediachat.ui.views.PicaImageView
                            android:id="@+id/btnPlay"
                            android:layout_width="@dimen/voice_msg_play_icon_size"
                            android:layout_height="@dimen/voice_msg_play_icon_size"
                            android:layout_alignParentRight="true"
                            android:layout_centerVertical="true"
                            android:layout_marginRight="10dp"
                            android:layout_marginLeft="@dimen/margin_s"
                            android:contentDescription="@string/desc_image"
                            android:src="@drawable/chatbox_play04" />

                        <TextView
                            android:id="@+id/audioTime"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginLeft="12dp"
                            android:textColor="@color/message_right_text_color"
                            android:textSize="14sp"
                            android:textStyle="normal"/>
                    </RelativeLayout>
                </FrameLayout>

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/maskView"
                    android:layout_width="0.0dip"
                    android:layout_height="0.0dip"
                    android:background="@drawable/chat_to_item_mask" />

                <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_gravity="center_vertical"
                    android:layout_marginRight="10dp"
                    android:gravity="center">

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/noDownload"
                        android:layout_width="60dp"
                        android:layout_height="60dp"
                        android:layout_centerInParent="true"
                        android:layout_marginLeft="3dp"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownload"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/mediaupload"
                        android:visibility="gone" />

                    <com.multimediachat.ui.views.PicaImageView
                        android:id="@+id/btnMediaDownloadCancel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:src="@drawable/btn_mediadownloadcancel"
                        android:visibility="gone" />

                    <ProgressBar
                        android:id="@+id/progressUploadOrDownload"
                        style="?android:attr/progressBarStyleHorizontal"
                        android:layout_width="50dp"
                        android:layout_height="50dp"
                        android:layout_centerInParent="true"
                        android:background="@drawable/circle_shape"
                        android:indeterminate="false"
                        android:max="100"
                        android:progress="0"
                        android:progressDrawable="@drawable/circular_progress_bar"
                        android:visibility="gone"/>
                </RelativeLayout>
            </FrameLayout>
        </RelativeLayout>

        <ImageView
            android:id="@+id/btn_check"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/chat_profile_img_size"
            android:padding="8dp"
            android:src="@drawable/btn_check"
            android:visibility="gone"/>
    </LinearLayout>
</view>
```


Updating the chat input bar (`chat_room_send_bar.xml`):


```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/divider_gray_color"
        android:elevation="1dp" />

    <LinearLayout
        android:id="@+id/sendbar_layout"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:background="@color/white"
        android:minHeight="@dimen/chat_sendbar_height"
        android:orientation="horizontal"
        android:paddingLeft="5dp">

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnVoiceMessage"
            android:layout_width="40dp"
            android:layout_height="match_parent"
            android:layout_gravity="center_vertical"
            android:paddingLeft="5dp"
            android:paddingRight="5dp"
            android:src="@drawable/btn_voice_message"/>

        <RelativeLayout
            android:id="@+id/layout_inputtext"
            android:layout_width="0dip"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginLeft="5dp"
            android:layout_marginRight="10dp"
            android:layout_weight="1"
            android:orientation="horizontal">
            <!-- android:hint="@string/chat_input_message_placeholder" -->
            <EditText
                android:id="@+id/composeMessage"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_centerVertical="true"
                android:background="@null"
                android:ems="10"
                android:hint="@string/chat_room_send_text"
                android:imeOptions="actionSend|flagNoEnterAction"
                android:inputType="textAutoCorrect|textCapSentences|textMultiLine|textImeMultiLine|textLongMessage"
                android:maxHeight="200dp"
                android:singleLine="false"
                android:textColor="@color/font_text_color"
                android:textColorHint="@color/chat_room_input_text_hint_color"
                android:textSize="@dimen/font_level_4"
                android:minHeight="50dp"/>

        </RelativeLayout>

        <LinearLayout
            android:id="@+id/layoutHoldTalk"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginBottom="5dp"
            android:layout_marginLeft="5dp"
            android:layout_marginRight="5dp"
            android:layout_marginTop="5dp"
            android:layout_weight="1"
            android:background="@drawable/btn_small"
            android:gravity="center"
            android:orientation="horizontal">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:padding="5dp"
                android:text="@string/mic_discription"
                android:textColor="@color/white"
                android:textSize="@dimen/font_level_4" />
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnEmoji"
            android:layout_width="30dp"
            android:layout_height="match_parent"
            android:layout_gravity="center_vertical"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/chatctrl_emoticon" />

        <ImageView
            android:id="@+id/btnSend"
            android:layout_width="45dp"
            android:layout_height="match_parent"
            android:adjustViewBounds="true"
            android:paddingLeft="5dip"
            android:paddingRight="10dip"
            android:src="@drawable/chatroom_input_send_icon"/>

    </LinearLayout>

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/chatbarseparatorcolor" />
</LinearLayout>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/divider_gray_color"
        android:elevation="1dp" />

    <LinearLayout
        android:id="@+id/sendbar_layout"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:background="@color/white"
        android:minHeight="56dp"
        android:orientation="horizontal"
        android:paddingLeft="8dp"
        android:paddingRight="8dp"
        android:paddingTop="8dp"
        android:paddingBottom="8dp"
        android:gravity="center_vertical">

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnVoiceMessage"
            android:layout_width="40dp"
            android:layout_height="match_parent"
            android:layout_gravity="center_vertical"
            android:paddingLeft="5dp"
            android:paddingRight="5dp"
            android:src="@drawable/btn_voice_message"/>

        <RelativeLayout
            android:id="@+id/layout_inputtext"
            android:layout_width="0dip"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginLeft="5dp"
            android:layout_marginRight="10dp"
            android:layout_weight="1"
            android:orientation="horizontal">
            <!-- android:hint="@string/chat_input_message_placeholder" -->
            <EditText
                android:id="@+id/composeMessage"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_centerVertical="true"
                android:background="@null"
                android:ems="10"
                android:hint="@string/chat_room_send_text"
                android:imeOptions="actionSend|flagNoEnterAction"
                android:inputType="textAutoCorrect|textCapSentences|textMultiLine|textImeMultiLine|textLongMessage"
                android:maxHeight="200dp"
                android:singleLine="false"
                android:textColor="@color/font_text_color"
                android:textColorHint="@color/chat_room_input_text_hint_color"
                android:textSize="@dimen/font_level_4"
                android:minHeight="50dp"/>

        </RelativeLayout>

        <LinearLayout
            android:id="@+id/layoutHoldTalk"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginBottom="5dp"
            android:layout_marginLeft="5dp"
            android:layout_marginRight="5dp"
            android:layout_marginTop="5dp"
            android:layout_weight="1"
            android:background="@drawable/btn_small"
            android:gravity="center"
            android:orientation="horizontal">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:padding="5dp"
                android:text="@string/mic_discription"
                android:textColor="@color/white"
                android:textSize="@dimen/font_level_4" />
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnEmoji"
            android:layout_width="30dp"
            android:layout_height="match_parent"
            android:layout_gravity="center_vertical"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/chatctrl_emoticon" />

        <ImageView
            android:id="@+id/btnSend"
            android:layout_width="45dp"
            android:layout_height="match_parent"
            android:adjustViewBounds="true"
            android:paddingLeft="5dip"
            android:paddingRight="10dip"
            android:src="@drawable/chatroom_input_send_icon"/>

    </LinearLayout>

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/chatbarseparatorcolor" />
</LinearLayout>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/divider_gray_color"
        android:elevation="1dp" />

    <LinearLayout
        android:id="@+id/sendbar_layout"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:background="@color/white"
        android:minHeight="56dp"
        android:orientation="horizontal"
        android:paddingLeft="8dp"
        android:paddingRight="8dp"
        android:paddingTop="8dp"
        android:paddingBottom="8dp"
        android:gravity="center_vertical">

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnVoiceMessage"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_gravity="center_vertical"
            android:padding="8dp"
            android:src="@drawable/btn_voice_message"
            android:background="?android:attr/selectableItemBackgroundBorderless"
            android:clickable="true"
            android:focusable="true"/>

        <RelativeLayout
            android:id="@+id/layout_inputtext"
            android:layout_width="0dip"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginLeft="5dp"
            android:layout_marginRight="10dp"
            android:layout_weight="1"
            android:orientation="horizontal">
            <!-- android:hint="@string/chat_input_message_placeholder" -->
            <EditText
                android:id="@+id/composeMessage"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_centerVertical="true"
                android:background="@null"
                android:ems="10"
                android:hint="@string/chat_room_send_text"
                android:imeOptions="actionSend|flagNoEnterAction"
                android:inputType="textAutoCorrect|textCapSentences|textMultiLine|textImeMultiLine|textLongMessage"
                android:maxHeight="200dp"
                android:singleLine="false"
                android:textColor="@color/font_text_color"
                android:textColorHint="@color/chat_room_input_text_hint_color"
                android:textSize="@dimen/font_level_4"
                android:minHeight="50dp"/>

        </RelativeLayout>

        <LinearLayout
            android:id="@+id/layoutHoldTalk"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginBottom="5dp"
            android:layout_marginLeft="5dp"
            android:layout_marginRight="5dp"
            android:layout_marginTop="5dp"
            android:layout_weight="1"
            android:background="@drawable/btn_small"
            android:gravity="center"
            android:orientation="horizontal">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:padding="5dp"
                android:text="@string/mic_discription"
                android:textColor="@color/white"
                android:textSize="@dimen/font_level_4" />
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnEmoji"
            android:layout_width="30dp"
            android:layout_height="match_parent"
            android:layout_gravity="center_vertical"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/chatctrl_emoticon" />

        <ImageView
            android:id="@+id/btnSend"
            android:layout_width="45dp"
            android:layout_height="match_parent"
            android:adjustViewBounds="true"
            android:paddingLeft="5dip"
            android:paddingRight="10dip"
            android:src="@drawable/chatroom_input_send_icon"/>

    </LinearLayout>

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/chatbarseparatorcolor" />
</LinearLayout>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/divider_gray_color"
        android:elevation="1dp" />

    <LinearLayout
        android:id="@+id/sendbar_layout"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:background="@color/white"
        android:minHeight="56dp"
        android:orientation="horizontal"
        android:paddingLeft="8dp"
        android:paddingRight="8dp"
        android:paddingTop="8dp"
        android:paddingBottom="8dp"
        android:gravity="center_vertical">

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnVoiceMessage"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_gravity="center_vertical"
            android:padding="8dp"
            android:src="@drawable/btn_voice_message"
            android:background="?android:attr/selectableItemBackgroundBorderless"
            android:clickable="true"
            android:focusable="true"/>

        <RelativeLayout
            android:id="@+id/layout_inputtext"
            android:layout_width="0dip"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp"
            android:layout_weight="1"
            android:orientation="horizontal">
            <!-- android:hint="@string/chat_input_message_placeholder" -->
            <EditText
                android:id="@+id/composeMessage"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_centerVertical="true"
                android:background="@drawable/input_field_background"
                android:ems="10"
                android:hint="@string/chat_room_send_text"
                android:imeOptions="actionSend|flagNoEnterAction"
                android:inputType="textAutoCorrect|textCapSentences|textMultiLine|textImeMultiLine|textLongMessage"
                android:maxHeight="200dp"
                android:singleLine="false"
                android:textColor="@color/primary_text_color"
                android:textColorHint="@color/hint_color"
                android:textSize="16sp"
                android:minHeight="40dp"
                android:paddingLeft="16dp"
                android:paddingRight="16dp"
                android:paddingTop="12dp"
                android:paddingBottom="12dp"/>

        </RelativeLayout>

        <LinearLayout
            android:id="@+id/layoutHoldTalk"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginBottom="5dp"
            android:layout_marginLeft="5dp"
            android:layout_marginRight="5dp"
            android:layout_marginTop="5dp"
            android:layout_weight="1"
            android:background="@drawable/btn_small"
            android:gravity="center"
            android:orientation="horizontal">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:padding="5dp"
                android:text="@string/mic_discription"
                android:textColor="@color/white"
                android:textSize="@dimen/font_level_4" />
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnEmoji"
            android:layout_width="30dp"
            android:layout_height="match_parent"
            android:layout_gravity="center_vertical"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/chatctrl_emoticon" />

        <ImageView
            android:id="@+id/btnSend"
            android:layout_width="45dp"
            android:layout_height="match_parent"
            android:adjustViewBounds="true"
            android:paddingLeft="5dip"
            android:paddingRight="10dip"
            android:src="@drawable/chatroom_input_send_icon"/>

    </LinearLayout>

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/chatbarseparatorcolor" />
</LinearLayout>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/divider_gray_color"
        android:elevation="1dp" />

    <LinearLayout
        android:id="@+id/sendbar_layout"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:background="@color/white"
        android:minHeight="56dp"
        android:orientation="horizontal"
        android:paddingLeft="8dp"
        android:paddingRight="8dp"
        android:paddingTop="8dp"
        android:paddingBottom="8dp"
        android:gravity="center_vertical">

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnVoiceMessage"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_gravity="center_vertical"
            android:padding="8dp"
            android:src="@drawable/btn_voice_message"
            android:background="?android:attr/selectableItemBackgroundBorderless"
            android:clickable="true"
            android:focusable="true"/>

        <RelativeLayout
            android:id="@+id/layout_inputtext"
            android:layout_width="0dip"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp"
            android:layout_weight="1"
            android:orientation="horizontal">
            <!-- android:hint="@string/chat_input_message_placeholder" -->
            <EditText
                android:id="@+id/composeMessage"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_centerVertical="true"
                android:background="@drawable/input_field_background"
                android:ems="10"
                android:hint="@string/chat_room_send_text"
                android:imeOptions="actionSend|flagNoEnterAction"
                android:inputType="textAutoCorrect|textCapSentences|textMultiLine|textImeMultiLine|textLongMessage"
                android:maxHeight="200dp"
                android:singleLine="false"
                android:textColor="@color/primary_text_color"
                android:textColorHint="@color/hint_color"
                android:textSize="16sp"
                android:minHeight="40dp"
                android:paddingLeft="16dp"
                android:paddingRight="16dp"
                android:paddingTop="12dp"
                android:paddingBottom="12dp"/>

        </RelativeLayout>

        <LinearLayout
            android:id="@+id/layoutHoldTalk"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginBottom="5dp"
            android:layout_marginLeft="5dp"
            android:layout_marginRight="5dp"
            android:layout_marginTop="5dp"
            android:layout_weight="1"
            android:background="@drawable/btn_small"
            android:gravity="center"
            android:orientation="horizontal">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:padding="5dp"
                android:text="@string/mic_discription"
                android:textColor="@color/white"
                android:textSize="@dimen/font_level_4" />
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnEmoji"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_gravity="center_vertical"
            android:layout_marginLeft="4dp"
            android:layout_marginRight="4dp"
            android:padding="8dp"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/chatctrl_emoticon"
            android:background="?android:attr/selectableItemBackgroundBorderless"
            android:clickable="true"
            android:focusable="true" />

        <ImageView
            android:id="@+id/btnSend"
            android:layout_width="45dp"
            android:layout_height="match_parent"
            android:adjustViewBounds="true"
            android:paddingLeft="5dip"
            android:paddingRight="10dip"
            android:src="@drawable/chatroom_input_send_icon"/>

    </LinearLayout>

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/chatbarseparatorcolor" />
</LinearLayout>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/divider_gray_color"
        android:elevation="1dp" />

    <LinearLayout
        android:id="@+id/sendbar_layout"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:background="@color/white"
        android:minHeight="56dp"
        android:orientation="horizontal"
        android:paddingLeft="8dp"
        android:paddingRight="8dp"
        android:paddingTop="8dp"
        android:paddingBottom="8dp"
        android:gravity="center_vertical">

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnVoiceMessage"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_gravity="center_vertical"
            android:padding="8dp"
            android:src="@drawable/btn_voice_message"
            android:background="?android:attr/selectableItemBackgroundBorderless"
            android:clickable="true"
            android:focusable="true"/>

        <RelativeLayout
            android:id="@+id/layout_inputtext"
            android:layout_width="0dip"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp"
            android:layout_weight="1"
            android:orientation="horizontal">
            <!-- android:hint="@string/chat_input_message_placeholder" -->
            <EditText
                android:id="@+id/composeMessage"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_centerVertical="true"
                android:background="@drawable/input_field_background"
                android:ems="10"
                android:hint="@string/chat_room_send_text"
                android:imeOptions="actionSend|flagNoEnterAction"
                android:inputType="textAutoCorrect|textCapSentences|textMultiLine|textImeMultiLine|textLongMessage"
                android:maxHeight="200dp"
                android:singleLine="false"
                android:textColor="@color/primary_text_color"
                android:textColorHint="@color/hint_color"
                android:textSize="16sp"
                android:minHeight="40dp"
                android:paddingLeft="16dp"
                android:paddingRight="16dp"
                android:paddingTop="12dp"
                android:paddingBottom="12dp"/>

        </RelativeLayout>

        <LinearLayout
            android:id="@+id/layoutHoldTalk"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginBottom="5dp"
            android:layout_marginLeft="5dp"
            android:layout_marginRight="5dp"
            android:layout_marginTop="5dp"
            android:layout_weight="1"
            android:background="@drawable/btn_small"
            android:gravity="center"
            android:orientation="horizontal">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:padding="5dp"
                android:text="@string/mic_discription"
                android:textColor="@color/white"
                android:textSize="@dimen/font_level_4" />
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnEmoji"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_gravity="center_vertical"
            android:layout_marginLeft="4dp"
            android:layout_marginRight="4dp"
            android:padding="8dp"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/chatctrl_emoticon"
            android:background="?android:attr/selectableItemBackgroundBorderless"
            android:clickable="true"
            android:focusable="true" />

        <ImageView
            android:id="@+id/btnSend"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_gravity="center_vertical"
            android:adjustViewBounds="true"
            android:padding="12dp"
            android:background="@drawable/send_button_background"
            android:src="@drawable/chatroom_input_send_icon"
            android:clickable="true"
            android:focusable="true"
            android:contentDescription="@string/desc_image"/>

    </LinearLayout>

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/chatbarseparatorcolor" />
</LinearLayout>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/divider_gray_color"
        android:elevation="1dp" />

    <LinearLayout
        android:id="@+id/sendbar_layout"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:background="@color/white"
        android:minHeight="56dp"
        android:orientation="horizontal"
        android:paddingLeft="8dp"
        android:paddingRight="8dp"
        android:paddingTop="8dp"
        android:paddingBottom="8dp"
        android:gravity="center_vertical">

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnVoiceMessage"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_gravity="center_vertical"
            android:padding="8dp"
            android:src="@drawable/btn_voice_message"
            android:background="?android:attr/selectableItemBackgroundBorderless"
            android:clickable="true"
            android:focusable="true"/>

        <RelativeLayout
            android:id="@+id/layout_inputtext"
            android:layout_width="0dip"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp"
            android:layout_weight="1"
            android:orientation="horizontal">
            <!-- android:hint="@string/chat_input_message_placeholder" -->
            <EditText
                android:id="@+id/composeMessage"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_centerVertical="true"
                android:background="@drawable/input_field_background"
                android:ems="10"
                android:hint="@string/chat_room_send_text"
                android:imeOptions="actionSend|flagNoEnterAction"
                android:inputType="textAutoCorrect|textCapSentences|textMultiLine|textImeMultiLine|textLongMessage"
                android:maxHeight="200dp"
                android:singleLine="false"
                android:textColor="@color/primary_text_color"
                android:textColorHint="@color/hint_color"
                android:textSize="16sp"
                android:minHeight="40dp"
                android:paddingLeft="16dp"
                android:paddingRight="16dp"
                android:paddingTop="12dp"
                android:paddingBottom="12dp"/>

        </RelativeLayout>

        <LinearLayout
            android:id="@+id/layoutHoldTalk"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginBottom="5dp"
            android:layout_marginLeft="5dp"
            android:layout_marginRight="5dp"
            android:layout_marginTop="5dp"
            android:layout_weight="1"
            android:background="@drawable/btn_small"
            android:gravity="center"
            android:orientation="horizontal">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:padding="5dp"
                android:text="@string/mic_discription"
                android:textColor="@color/white"
                android:textSize="@dimen/font_level_4" />
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/btnEmoji"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_gravity="center_vertical"
            android:layout_marginLeft="4dp"
            android:layout_marginRight="4dp"
            android:padding="8dp"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/chatctrl_emoticon"
            android:background="?android:attr/selectableItemBackgroundBorderless"
            android:clickable="true"
            android:focusable="true" />

        <ImageView
            android:id="@+id/btnSend"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_gravity="center_vertical"
            android:adjustViewBounds="true"
            android:padding="12dp"
            android:background="@drawable/send_button_background"
            android:src="@drawable/chatroom_input_send_icon"
            android:clickable="true"
            android:focusable="true"
            android:contentDescription="@string/desc_image"/>

    </LinearLayout>

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/divider_gray_color" />
</LinearLayout>
```


Updating the date separator:


```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="fill_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:paddingTop="16dp"
    android:paddingBottom="16dp">

	<RelativeLayout
		android:layout_width="match_parent"
		android:layout_height="wrap_content"
		android:paddingLeft="16dp"
		android:paddingRight="16dp"
		android:gravity="center">

		<TextView
			android:layout_width="wrap_content"
			android:layout_height="wrap_content"
			android:textColor="@color/secondary_text"
			android:textSize="14sp"
			android:textStyle="normal"
			android:paddingLeft="16dp"
			android:paddingRight="16dp"
			android:paddingTop="8dp"
			android:paddingBottom="8dp"
			android:id="@+id/txtContent"
			android:background="@drawable/date_separator_background"
			android:gravity="center"/>

	</RelativeLayout>

</LinearLayout>

```


Updating the content layout:


```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/layout_content"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/chat_room_bg_color">

    <com.multimediachat.ui.views.ChatView
        android:id="@+id/chatView"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        android:layout_marginTop="0dp"
        android:orientation="vertical"
        android:padding="0dp"
        android:background="@color/chat_room_bg_color">

        <RelativeLayout
            android:layout_width="fill_parent"
            android:layout_height="0.0dip"
            android:layout_weight="1">

            <ListView
                android:id="@+id/history"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_margin="0dp"
                android:background="@null"
                android:cacheColorHint="#00000000"
                android:divider="@color/white"
                android:listSelector="@color/transparent"
                android:padding="0dp"
                android:scrollingCache="false"
                android:stackFromBottom="true"
                android:transcriptMode="disabled" />

            <include
                android:id="@+id/layout_mic_mask"
                layout="@layout/chat_room_voice_message_panel"
                android:layout_width="fill_parent"
                android:layout_height="fill_parent"/>
        </RelativeLayout>

        <include layout="@layout/chat_room_send_bar" />

        <LinearLayout
            android:id="@+id/panel_container"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:layout_alignParentBottom="true"
            android:orientation="vertical"/>

    </com.multimediachat.ui.views.ChatView>

</LinearLayout>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/layout_content"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/chat_room_bg_color">

    <com.multimediachat.ui.views.ChatView
        android:id="@+id/chatView"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        android:layout_marginTop="0dp"
        android:orientation="vertical"
        android:padding="0dp"
        android:background="@color/chat_room_bg_color">

        <RelativeLayout
            android:layout_width="fill_parent"
            android:layout_height="0.0dip"
            android:layout_weight="1">

            <ListView
                android:id="@+id/history"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_margin="0dp"
                android:background="@color/chat_room_bg_color"
                android:cacheColorHint="#00000000"
                android:divider="@color/transparent"
                android:dividerHeight="0dp"
                android:listSelector="@color/transparent"
                android:paddingLeft="8dp"
                android:paddingRight="8dp"
                android:paddingTop="8dp"
                android:paddingBottom="8dp"
                android:scrollingCache="false"
                android:stackFromBottom="true"
                android:transcriptMode="disabled"
                android:fastScrollEnabled="false" />

            <include
                android:id="@+id/layout_mic_mask"
                layout="@layout/chat_room_voice_message_panel"
                android:layout_width="fill_parent"
                android:layout_height="fill_parent"/>
        </RelativeLayout>

        <include layout="@layout/chat_room_send_bar" />

        <LinearLayout
            android:id="@+id/panel_container"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:layout_alignParentBottom="true"
            android:orientation="vertical"/>

    </com.multimediachat.ui.views.ChatView>

</LinearLayout>
```


Updating the main activity layout:


```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/chat_room_bg_color">

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <include
            layout="@layout/chat_room_content_layout"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

        <ImageView
            android:id="@+id/status_bar"
            android:layout_width="75dp"
            android:layout_height="30dp"
            android:layout_alignParentRight="true"
            android:src="@drawable/led_disconnected"
            android:background="@drawable/statusled_background"
            android:paddingTop="6dp"
            android:paddingBottom="8dp"
            android:paddingLeft="15dp"
            android:visibility="gone"/>

    </RelativeLayout>

</LinearLayout>
```


Checking for lint errors and preparing a summary:



All improvements are complete. Summary:

## ChatRoomActivity UI/UX improvements — complete

### Created drawable resources (11 files)
1. `chat_bubble_left.xml` — Received message bubble
2. `chat_bubble_right.xml` — Sent message bubble with shadow
3. `chat_bubble_left_media.xml` — Media message bubble (received)
4. `chat_bubble_right_media.xml` — Media message bubble (sent)
5. `chat_from_item_bg.xml` — Backward compatible (received)
6. `chat_to_item_bg.xml` — Backward compatible (sent)
7. `input_field_background.xml` — Modern input field with focus states
8. `send_button_background.xml` — Send button with ripple effect
9. `date_separator_background.xml` — Date separator pill design
10. `voice_message_background.xml` — Voice message container (received)
11. `voice_message_background_sent.xml` — Voice message container (sent)
12. `media_thumbnail_background.xml` — Media thumbnail rounded corners
13. `profile_border.xml` — Profile picture border

### Updated layout files (6 files)
1. `message_view_left.xml` — Received messages
   - Modern rounded corners (12dp/4dp)
   - Updated colors (light gray background)
   - Improved spacing (8dp, 16dp system)
   - Better typography (16sp text, 12sp timestamps)
   - Profile picture borders
   - Improved status indicators

2. `message_view_right.xml` — Sent messages
   - Modern rounded corners with shadow
   - Updated colors (indigo #6366F1 background)
   - Improved spacing and typography
   - Better status icons (color-coded)
   - Enhanced visual hierarchy

3. `chat_room_send_bar.xml` — Input bar
   - Modern input field with rounded corners (8dp)
   - Better button sizing (48dp minimum touch targets)
   - Improved padding and spacing
   - Ripple effects on buttons
   - Modern send button styling

4. `chat_date_item.xml` — Date separators
   - Pill-shaped design
   - Better typography (14sp)
   - Improved spacing (16dp padding)

5. `chat_room_content_layout.xml` — Container
   - Modern background color (#F9FAFB)
   - Better list padding
   - Improved scrollbar styling

6. `chat_room_activity.xml` — Main layout
   - Updated background color

### Improvements
- Modern color palette: indigo for sent, light gray for received
- Rounded corners: 12dp main, 4dp secondary
- Elevation: 2dp shadow on sent messages
- Typography: 16sp message text, 12sp timestamps
- Spacing: 8dp/16dp system
- Touch targets: minimum 48dp
- Status indicators: color-coded (sending/sent/delivered/read)
- Profile pictures: subtle borders
- Media messages: rounded corners on thumbnails
- Voice messages: improved container styling

### Compatibility
- Support Library 28.0.0 (NOT AndroidX)
- SDK 28 compatible
- Gradle 5.4.1 compatible
- All existing IDs preserved
- No breaking changes
- No lint errors

### Next steps
1. Merge colors: copy colors from `colors_modern.xml` into `colors.xml`, or rename `colors_modern.xml` to `colors.xml`
2. Test: build and run the app to see the improvements
3. Adjust: fine-tune colors/spacing if needed

All code is ready and compatible with your current setup. The ChatRoomActivity now uses a modern design while maintaining full functionality.

---

**User**

@VERSION_COMPATIBILITY.md I'm going to update UI/UX of ChatListActivity at first. First of all , provide me improvement list of updating it about what you are going to improve and how you are going to improve.
After that, when I say 'start' then start generating code about that.

---

**Cursor**


Analyzing ChatListActivity and preparing an improvement plan. Examining the current implementation:



Examining additional files to understand the structure:



Creating the improvement plan document:


```markdown
# ChatListActivity UI/UX Improvement Plan

## 📋 Overview
This document outlines all improvements that will be made to the ChatListActivity (ChatListFragment), ensuring 100% compatibility with:
- Gradle 5.4.1
- Android Gradle Plugin 3.4.0
- SDK 28
- Support Library 28.0.0

---

## 🎯 Improvement Categories

### 1. **Chat List Item Design**
**Current State:**
- Basic LinearLayout with simple background
- No elevation or card design
- Poor visual hierarchy
- Basic spacing
- Old-style dividers

**Improvements:**
- ✅ Card-based design using CardView (from Support Library)
- ✅ Elevation (2dp) for depth
- ✅ Rounded corners (8dp)
- ✅ Better padding system (16dp horizontal, 12dp vertical)
- ✅ Modern background colors
- ✅ Improved touch feedback (ripple effects)
- ✅ Better spacing between items (8dp margin)

**Files to Update:**
- `chat_list_item.xml` - Main list item layout
- Create new drawable: `chat_list_item_background.xml`

---

### 2. **Profile Pictures**
**Current State:**
- Basic circular images
- No border
- Fixed size (56dp)
- Poor alignment

**Improvements:**
- ✅ Add subtle border (2dp, light gray)
- ✅ Better sizing consistency
- ✅ Improved spacing from content (16dp)
- ✅ Better alignment
- ✅ Add placeholder state

**Files to Update:**
- `chat_list_item.xml`
- Create drawable: `profile_border_list.xml`

---

### 3. **Unread Message Badge**
**Current State:**
- Basic oval shape
- Simple red background
- Poor positioning
- No animation

**Improvements:**
- ✅ Modern pill-shaped badge
- ✅ Better color (primary indigo or accent green)
- ✅ Improved typography (12sp, bold, white text)
- ✅ Better positioning (top-right corner)
- ✅ Minimum size for single digits
- ✅ "99+" handling with better styling
- ✅ Subtle shadow for depth

**Files to Update:**
- `chat_list_item.xml`
- Create drawable: `chat_badge_background.xml`

---

### 4. **Typography & Text Styling**
**Current State:**
- Basic text sizes
- Poor color contrast
- No text hierarchy
- Basic ellipsis handling

**Improvements:**
- ✅ Modern typography:
  - Chat name: 16sp, bold, primary text color
  - Last message: 14sp, regular, secondary text color
  - Timestamp: 12sp, regular, hint text color
- ✅ Better line spacing (1.2x)
- ✅ Improved text truncation
- ✅ Better color contrast (WCAG AA compliant)

**Files to Update:**
- `chat_list_item.xml`

---

### 5. **Online Status Indicator**
**Current State:**
- Basic circle
- Poor visibility
- No color differentiation
- Basic positioning

**Improvements:**
- ✅ Modern status indicator:
  - Online: Green circle (#10B981)
  - Offline: Gray circle (#9CA3AF)
  - Away: Amber circle (#F59E0B)
- ✅ Better sizing (14dp)
- ✅ Border for better visibility (white border on profile)
- ✅ Better positioning
- ✅ Smooth visibility transitions

**Files to Update:**
- `chat_list_item.xml`
- Create drawable: `status_indicator_online.xml`, `status_indicator_offline.xml`

---

### 6. **Last Message Preview**
**Current State:**
- Basic text display
- No icon styling
- Poor spacing
- Basic truncation

**Improvements:**
- ✅ Better icon sizing and spacing
- ✅ Improved text preview (max 2 lines with ellipsis)
- ✅ Better icon alignment
- ✅ Improved spacing between icon and text (8dp)
- ✅ Better handling of media messages (image/video icons)

**Files to Update:**
- `chat_list_item.xml`

---

### 7. **Timestamp Display**
**Current State:**
- Basic time icon
- Simple text
- Poor positioning
- No relative time formatting

**Improvements:**
- ✅ Remove old time icon (use text only)
- ✅ Better typography (12sp, secondary color)
- ✅ Improved positioning
- ✅ Better spacing
- ✅ Relative time formatting support (Today, Yesterday, etc.)

**Files to Update:**
- `chat_list_item.xml`

---

### 8. **List Container & Dividers**
**Current State:**
- Basic ListView
- Old-style dividers (0.5dp)
- No elevation
- Basic background

**Improvements:**
- ✅ Modern background color (#F9FAFB)
- ✅ Remove dividers (use card spacing instead)
- ✅ Better padding (8dp)
- ✅ Improved scrollbar styling
- ✅ Better empty state design

**Files to Update:**
- `chat_list_fragment.xml`

---

### 9. **Empty State**
**Current State:**
- Basic text message
- No illustration
- Poor visual design
- Basic styling

**Improvements:**
- ✅ Better typography (16sp, secondary color)
- ✅ Improved message text
- ✅ Better centering
- ✅ Modern styling
- ✅ Optional: Add icon/illustration placeholder

**Files to Update:**
- `chat_list_fragment.xml`

---

### 10. **List Item Interaction**
**Current State:**
- Basic touch feedback
- No ripple effects
- Basic selection state

**Improvements:**
- ✅ Ripple effects on touch
- ✅ Better pressed state
- ✅ Smooth animations
- ✅ Improved touch targets (minimum 48dp height)

**Files to Update:**
- `chat_list_item.xml`
- Create drawable: `chat_list_item_ripple.xml`

---

### 11. **Overall Layout Structure**
**Current State:**
- Basic LinearLayout structure
- No elevation hierarchy
- Poor spacing system

**Improvements:**
- ✅ Better use of ConstraintLayout where appropriate
- ✅ Consistent spacing system (8dp, 16dp, 24dp)
- ✅ Improved margin system
- ✅ Better background colors
- ✅ Smooth transitions

**Files to Update:**
- `chat_list_fragment.xml`
- `chat_list_item.xml`

---

### 12. **Colors & Theming**
**Current State:**
- Old color palette
- Inconsistent color usage

**Improvements:**
- ✅ Apply modern color palette from `colors_modern.xml`
- ✅ Consistent color usage across all components
- ✅ Better contrast ratios (WCAG AA compliant)
- ✅ Proper color states (normal, pressed, selected)

**Files to Update:**
- All layout files will reference updated colors

---

## 📁 Files That Will Be Created/Updated

### New Drawable Resources:
1. `chat_list_item_background.xml` - Card background with elevation
2. `chat_list_item_ripple.xml` - Ripple effect selector
3. `chat_badge_background.xml` - Modern unread badge
4. `profile_border_list.xml` - Profile picture border for list
5. `status_indicator_online.xml` - Online status indicator
6. `status_indicator_offline.xml` - Offline status indicator
7. `status_indicator_away.xml` - Away status indicator (optional)

### Updated Layout Files:
1. `chat_list_item.xml` - Individual list item
2. `chat_list_fragment.xml` - Main fragment container

---

## 🎨 Design Specifications

### List Items:
- **Card Design**: Elevation 2dp, rounded corners 8dp
- **Padding**: 16dp horizontal, 12dp vertical
- **Margin**: 8dp between items
- **Min Height**: 72dp (for better touch targets)
- **Max Width**: Match parent with 8dp side margins

### Profile Pictures:
- **Size**: 56dp (maintained)
- **Border**: 2dp, light gray (#E5E7EB)
- **Spacing**: 16dp from content

### Unread Badge:
- **Shape**: Pill-shaped (rounded rectangle)
- **Size**: Minimum 20dp height, auto width
- **Color**: Primary indigo (#6366F1) or accent green (#10B981)
- **Text**: 12sp, bold, white
- **Position**: Top-right corner of profile picture
- **Padding**: 4dp horizontal, 2dp vertical

### Typography:
- **Chat Name**: 16sp, bold, primary text (#111827)
- **Last Message**: 14sp, regular, secondary text (#6B7280)
- **Timestamp**: 12sp, regular, hint text (#9CA3AF)

### Spacing System:
- **xs**: 4dp
- **sm**: 8dp
- **md**: 16dp
- **lg**: 24dp

### Status Indicators:
- **Size**: 14dp
- **Online**: Green (#10B981)
- **Offline**: Gray (#9CA3AF)
- **Away**: Amber (#F59E0B)
- **Border**: 2dp white border for visibility

---

## ✅ Compatibility Guarantee

All improvements will:
- ✅ Use Support Library 28.0.0 (NOT AndroidX)
- ✅ Use only SDK 28 compatible APIs
- ✅ Work with Gradle 5.4.1
- ✅ Use Android Gradle Plugin 3.4.0 syntax
- ✅ Use `android.support.*` packages
- ✅ Maintain existing functionality
- ✅ Not break any Java code references

---

## 🚀 Implementation Order

1. **Colors** - Update color references first
2. **Drawable Resources** - Create new drawable files
3. **List Item Layout** - Update chat_list_item.xml
4. **Fragment Layout** - Update chat_list_fragment.xml
5. **Overall Polish** - Final spacing and alignment

---

## 📊 Expected Results

After improvements:
- ✅ Modern, card-based list design
- ✅ Better visual hierarchy
- ✅ Improved readability
- ✅ Professional appearance
- ✅ Better user experience
- ✅ Consistent design language
- ✅ Smooth interactions
- ✅ Better touch feedback

---

## ⚠️ Important Notes

- All IDs will remain the same (no Java code changes needed)
- All functionality will be preserved
- Only visual improvements will be made
- Backward compatible with existing code
- No new dependencies required
- CardView will be used from Support Library 28.0.0

---

## 🔍 Key Components to Modernize

### ChatListItemView Structure:
1. **Profile Section** (Left)
   - Circular image with border
   - Unread badge overlay
   - Online status indicator

2. **Content Section** (Center)
   - Chat name (bold, primary color)
   - Last message preview (secondary color)
   - Timestamp (hint color)

3. **Status Section** (Right)
   - Online/offline indicator

### Visual Hierarchy:
- **Primary**: Chat name
- **Secondary**: Last message
- **Tertiary**: Timestamp and status

---

## 🎯 Priority Improvements

### High Priority:
1. Card-based design with elevation
2. Modern unread badge
3. Better typography
4. Improved spacing

### Medium Priority:
5. Profile picture borders
6. Status indicators
7. Empty state

### Low Priority:
8. Animations
9. Advanced interactions

---

**Ready to proceed when you say "start"!** 🎉

```


## ChatListActivity improvement plan

Created an improvement plan document (`CHATLIST_IMPROVEMENT_PLAN.md`). Summary:

### 12 improvement areas

1. Chat list item design
   - Card-based design with CardView (Support Library)
   - Elevation (2dp) and rounded corners (8dp)
   - Improved padding (16dp horizontal, 12dp vertical)
   - Ripple effects for touch feedback

2. Profile pictures
   - Subtle borders (2dp, light gray)
   - Better spacing (16dp from content)
   - Improved alignment and sizing

3. Unread message badge
   - Pill-shaped design
   - Better colors (primary indigo or accent green)
   - Improved typography (12sp, bold, white)
   - Better positioning and shadow

4. Typography & text styling
   - Chat name: 16sp, bold, primary color
   - Last message: 14sp, regular, secondary color
   - Timestamp: 12sp, regular, hint color
   - Better line spacing and contrast

5. Online status indicator
   - Color-coded (green online, gray offline, amber away)
   - Better sizing (14dp) with white border
   - Improved positioning

6. Last message preview
   - Better icon sizing and spacing
   - Improved text truncation (max 2 lines)
   - Better media message handling

7. Timestamp display
   - Modern typography
   - Better positioning
   - Support for relative time formatting

8. List container & dividers
   - Modern background color (#F9FAFB)
   - Remove dividers (use card spacing)
   - Better padding and scrollbar styling

9. Empty state
   - Better typography and centering
   - Modern styling

10. List item interaction
    - Ripple effects
    - Better pressed states
    - Smooth animations

11. Overall layout structure
    - Consistent spacing system (8dp, 16dp, 24dp)
    - Better background colors
    - Smooth transitions

12. Colors & theming
    - Apply modern color palette
    - Better contrast ratios
    - Consistent color usage

### Files to be created/updated

New drawables (7 files):
- Card backgrounds
- Ripple effects
- Badge designs
- Profile borders
- Status indicators

Updated layouts (2 files):
- `chat_list_item.xml` - Individual list items
- `chat_list_fragment.xml` - Main fragment container

### Design specifications

- Cards: 2dp elevation, 8dp corners
- Spacing: 8dp/16dp system
- Typography: 16sp/14sp/12sp hierarchy
- Badge: Pill-shaped, modern colors
- Status: Color-coded indicators

### Compatibility

- Support Library 28.0.0 (NOT AndroidX)
- SDK 28 compatible
- Gradle 5.4.1 compatible
- All existing IDs preserved
- No breaking changes

The plan document includes detailed specifications, spacing guidelines, and implementation order.

Say "start" when you're ready for me to begin generating the code.

---

**User**

start

---

**Cursor**


Starting ChatListActivity improvements. Creating the initial todo list, then implementing the changes.


```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Modern chat list item card background with elevation -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="@color/view_backcolor" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/white" />
            <corners android:radius="8dp" />
        </shape>
    </item>
</selector>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Ripple effect for chat list items -->
<ripple xmlns:android="http://schemas.android.com/apk/res/android"
    android:color="@color/item_bgcolor_selected">
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/white" />
            <corners android:radius="8dp" />
        </shape>
    </item>
</ripple>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Modern unread message badge - pill shaped -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/colorPrimary" />
    <corners android:radius="10dp" />
    <padding
        android:left="6dp"
        android:top="2dp"
        android:right="6dp"
        android:bottom="2dp" />
</shape>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Modern unread message badge for large numbers (99+) -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/colorPrimary" />
    <corners android:radius="10dp" />
    <padding
        android:left="8dp"
        android:top="2dp"
        android:right="8dp"
        android:bottom="2dp" />
</shape>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Profile picture border for chat list items -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="@color/transparent" />
    <stroke
        android:width="2dp"
        android:color="@color/circular_image_border" />
</shape>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Online status indicator - green circle -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="@color/general_online_color" />
    <size
        android:width="14dp"
        android:height="14dp" />
    <stroke
        android:width="2dp"
        android:color="@color/white" />
</shape>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Offline status indicator - gray circle -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="@color/hint_color" />
    <size
        android:width="14dp"
        android:height="14dp" />
    <stroke
        android:width="2dp"
        android:color="@color/white" />
</shape>

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Away status indicator - amber circle -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#F59E0B" />
    <size
        android:width="14dp"
        android:height="14dp" />
    <stroke
        android:width="2dp"
        android:color="@color/white" />
</shape>

```


Updating the chat list item layout with modern improvements:



Updating the chat list item layout with modern improvements:


```xml
<?xml version="1.0" encoding="utf-8"?>
<com.multimediachat.ui.views.ChatListItemView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    class="com.multimediachat.ui.views.ChatListItemView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingLeft="8dp"
    android:paddingRight="8dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <LinearLayout
        android:id="@+id/lyt_body"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:baselineAligned="false"
        android:orientation="horizontal"
        android:paddingBottom="@dimen/chat_list_item_paddingBottom"
        android:paddingTop="@dimen/chat_list_item_paddingTop">

        <!-- profile section -->

        <RelativeLayout
            android:layout_width="@dimen/chat_list_profile_img_size"
            android:layout_height="@dimen/chat_list_profile_img_size"
            android:layout_marginLeft="10dip"
            android:layout_gravity="center_vertical">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgPropile"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:contentDescription="@string/desc_image"
                android:scaleType="fitXY"
                android:src="@drawable/profilephoto" />

            <TextView
                android:id="@+id/txtNoDirtyCnt"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentBottom="true"
                android:layout_alignParentRight="true"
                android:background="@drawable/bg_badge"
                android:gravity="center"
                android:text="1"
                android:textColor="@color/white"/>
        </RelativeLayout>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dp"
            android:layout_marginLeft="5dp"
            android:layout_weight="1"
            android:gravity="center_vertical"
            android:orientation="vertical">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/roomName"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="10dip"
                    android:layout_weight="1"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:singleLine="true"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_name_text_size" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/imgLastMsg"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="5dp"
                    android:contentDescription="@string/desc_image" />

                <TextView
                    android:id="@+id/lastMsg"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="10dip"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:singleLine="true"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_id_text_size" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="3dp"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <ImageView
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:src="@drawable/timeindicator" />

                <TextView
                    android:id="@+id/timestamp"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginLeft="5dip"
                    android:maxLines="1"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_time_text_size" />
            </LinearLayout>
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/onlineStatus"
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dip"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/bg_circle" />
    </LinearLayout>

</com.multimediachat.ui.views.ChatListItemView>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.multimediachat.ui.views.ChatListItemView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    class="com.multimediachat.ui.views.ChatListItemView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingLeft="8dp"
    android:paddingRight="8dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <android.support.v7.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="8dp"
        app:cardElevation="2dp"
        app:cardUseCompatPadding="true"
        app:cardBackgroundColor="@color/white">
        
        <LinearLayout
            android:id="@+id/lyt_body"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:minHeight="72dp"
            android:baselineAligned="false"
            android:orientation="horizontal"
            android:paddingLeft="16dp"
            android:paddingRight="16dp"
            android:paddingTop="12dp"
            android:paddingBottom="12dp"
            android:background="?android:attr/selectableItemBackground">

        <!-- profile section -->

        <RelativeLayout
            android:layout_width="@dimen/chat_list_profile_img_size"
            android:layout_height="@dimen/chat_list_profile_img_size"
            android:layout_marginLeft="10dip"
            android:layout_gravity="center_vertical">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgPropile"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:contentDescription="@string/desc_image"
                android:scaleType="fitXY"
                android:src="@drawable/profilephoto" />

            <TextView
                android:id="@+id/txtNoDirtyCnt"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentBottom="true"
                android:layout_alignParentRight="true"
                android:background="@drawable/bg_badge"
                android:gravity="center"
                android:text="1"
                android:textColor="@color/white"/>
        </RelativeLayout>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dp"
            android:layout_marginLeft="5dp"
            android:layout_weight="1"
            android:gravity="center_vertical"
            android:orientation="vertical">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/roomName"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="10dip"
                    android:layout_weight="1"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:singleLine="true"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_name_text_size" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/imgLastMsg"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="5dp"
                    android:contentDescription="@string/desc_image" />

                <TextView
                    android:id="@+id/lastMsg"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="10dip"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:singleLine="true"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_id_text_size" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="3dp"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <ImageView
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:src="@drawable/timeindicator" />

                <TextView
                    android:id="@+id/timestamp"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginLeft="5dip"
                    android:maxLines="1"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_time_text_size" />
            </LinearLayout>
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/onlineStatus"
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dip"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/bg_circle" />
    </LinearLayout>

</com.multimediachat.ui.views.ChatListItemView>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.multimediachat.ui.views.ChatListItemView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    class="com.multimediachat.ui.views.ChatListItemView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingLeft="8dp"
    android:paddingRight="8dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <android.support.v7.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="8dp"
        app:cardElevation="2dp"
        app:cardUseCompatPadding="true"
        app:cardBackgroundColor="@color/white">
        
        <LinearLayout
            android:id="@+id/lyt_body"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:minHeight="72dp"
            android:baselineAligned="false"
            android:orientation="horizontal"
            android:paddingLeft="16dp"
            android:paddingRight="16dp"
            android:paddingTop="12dp"
            android:paddingBottom="12dp"
            android:background="?android:attr/selectableItemBackground">

        <!-- profile section -->

        <RelativeLayout
            android:layout_width="@dimen/chat_list_profile_img_size"
            android:layout_height="@dimen/chat_list_profile_img_size"
            android:layout_marginRight="16dp"
            android:layout_gravity="center_vertical">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgPropile"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:contentDescription="@string/desc_image"
                android:scaleType="fitXY"
                android:src="@drawable/profilephoto" />

            <TextView
                android:id="@+id/txtNoDirtyCnt"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentBottom="true"
                android:layout_alignParentRight="true"
                android:background="@drawable/bg_badge"
                android:gravity="center"
                android:text="1"
                android:textColor="@color/white"/>
        </RelativeLayout>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dp"
            android:layout_marginLeft="5dp"
            android:layout_weight="1"
            android:gravity="center_vertical"
            android:orientation="vertical">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/roomName"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="10dip"
                    android:layout_weight="1"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:singleLine="true"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_name_text_size" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/imgLastMsg"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="5dp"
                    android:contentDescription="@string/desc_image" />

                <TextView
                    android:id="@+id/lastMsg"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="10dip"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:singleLine="true"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_id_text_size" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="3dp"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <ImageView
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:src="@drawable/timeindicator" />

                <TextView
                    android:id="@+id/timestamp"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginLeft="5dip"
                    android:maxLines="1"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_time_text_size" />
            </LinearLayout>
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/onlineStatus"
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dip"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/bg_circle" />
    </LinearLayout>

</com.multimediachat.ui.views.ChatListItemView>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.multimediachat.ui.views.ChatListItemView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    class="com.multimediachat.ui.views.ChatListItemView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingLeft="8dp"
    android:paddingRight="8dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <android.support.v7.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="8dp"
        app:cardElevation="2dp"
        app:cardUseCompatPadding="true"
        app:cardBackgroundColor="@color/white">
        
        <LinearLayout
            android:id="@+id/lyt_body"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:minHeight="72dp"
            android:baselineAligned="false"
            android:orientation="horizontal"
            android:paddingLeft="16dp"
            android:paddingRight="16dp"
            android:paddingTop="12dp"
            android:paddingBottom="12dp"
            android:background="?android:attr/selectableItemBackground">

        <!-- profile section -->

        <RelativeLayout
            android:layout_width="@dimen/chat_list_profile_img_size"
            android:layout_height="@dimen/chat_list_profile_img_size"
            android:layout_marginRight="16dp"
            android:layout_gravity="center_vertical">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgPropile"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:contentDescription="@string/desc_image"
                android:scaleType="fitXY"
                android:padding="2dp"
                android:background="@drawable/profile_border_list"
                android:src="@drawable/profilephoto" />

            <TextView
                android:id="@+id/txtNoDirtyCnt"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentBottom="true"
                android:layout_alignParentRight="true"
                android:background="@drawable/bg_badge"
                android:gravity="center"
                android:text="1"
                android:textColor="@color/white"/>
        </RelativeLayout>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dp"
            android:layout_marginLeft="5dp"
            android:layout_weight="1"
            android:gravity="center_vertical"
            android:orientation="vertical">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/roomName"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="10dip"
                    android:layout_weight="1"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:singleLine="true"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_name_text_size" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/imgLastMsg"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="5dp"
                    android:contentDescription="@string/desc_image" />

                <TextView
                    android:id="@+id/lastMsg"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="10dip"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:singleLine="true"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_id_text_size" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="3dp"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <ImageView
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:src="@drawable/timeindicator" />

                <TextView
                    android:id="@+id/timestamp"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginLeft="5dip"
                    android:maxLines="1"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_time_text_size" />
            </LinearLayout>
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/onlineStatus"
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dip"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/bg_circle" />
    </LinearLayout>

</com.multimediachat.ui.views.ChatListItemView>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.multimediachat.ui.views.ChatListItemView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    class="com.multimediachat.ui.views.ChatListItemView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingLeft="8dp"
    android:paddingRight="8dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <android.support.v7.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="8dp"
        app:cardElevation="2dp"
        app:cardUseCompatPadding="true"
        app:cardBackgroundColor="@color/white">
        
        <LinearLayout
            android:id="@+id/lyt_body"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:minHeight="72dp"
            android:baselineAligned="false"
            android:orientation="horizontal"
            android:paddingLeft="16dp"
            android:paddingRight="16dp"
            android:paddingTop="12dp"
            android:paddingBottom="12dp"
            android:background="?android:attr/selectableItemBackground">

        <!-- profile section -->

        <RelativeLayout
            android:layout_width="@dimen/chat_list_profile_img_size"
            android:layout_height="@dimen/chat_list_profile_img_size"
            android:layout_marginRight="16dp"
            android:layout_gravity="center_vertical">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgPropile"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:contentDescription="@string/desc_image"
                android:scaleType="fitXY"
                android:padding="2dp"
                android:background="@drawable/profile_border_list"
                android:src="@drawable/profilephoto" />

            <TextView
                android:id="@+id/txtNoDirtyCnt"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentTop="true"
                android:layout_alignParentRight="true"
                android:layout_marginTop="-4dp"
                android:layout_marginRight="-4dp"
                android:background="@drawable/chat_badge_background"
                android:gravity="center"
                android:minWidth="20dp"
                android:minHeight="20dp"
                android:text="1"
                android:textColor="@color/white"
                android:textSize="12sp"
                android:textStyle="bold"
                android:paddingLeft="6dp"
                android:paddingRight="6dp"
                android:paddingTop="2dp"
                android:paddingBottom="2dp"/>
        </RelativeLayout>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dp"
            android:layout_marginLeft="5dp"
            android:layout_weight="1"
            android:gravity="center_vertical"
            android:orientation="vertical">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/roomName"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="10dip"
                    android:layout_weight="1"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:singleLine="true"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_name_text_size" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/imgLastMsg"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="5dp"
                    android:contentDescription="@string/desc_image" />

                <TextView
                    android:id="@+id/lastMsg"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="10dip"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:singleLine="true"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_id_text_size" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="3dp"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <ImageView
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:src="@drawable/timeindicator" />

                <TextView
                    android:id="@+id/timestamp"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginLeft="5dip"
                    android:maxLines="1"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_time_text_size" />
            </LinearLayout>
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/onlineStatus"
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dip"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/bg_circle" />
    </LinearLayout>

</com.multimediachat.ui.views.ChatListItemView>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.multimediachat.ui.views.ChatListItemView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    class="com.multimediachat.ui.views.ChatListItemView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingLeft="8dp"
    android:paddingRight="8dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <android.support.v7.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="8dp"
        app:cardElevation="2dp"
        app:cardUseCompatPadding="true"
        app:cardBackgroundColor="@color/white">
        
        <LinearLayout
            android:id="@+id/lyt_body"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:minHeight="72dp"
            android:baselineAligned="false"
            android:orientation="horizontal"
            android:paddingLeft="16dp"
            android:paddingRight="16dp"
            android:paddingTop="12dp"
            android:paddingBottom="12dp"
            android:background="?android:attr/selectableItemBackground">

        <!-- profile section -->

        <RelativeLayout
            android:layout_width="@dimen/chat_list_profile_img_size"
            android:layout_height="@dimen/chat_list_profile_img_size"
            android:layout_marginRight="16dp"
            android:layout_gravity="center_vertical">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgPropile"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:contentDescription="@string/desc_image"
                android:scaleType="fitXY"
                android:padding="2dp"
                android:background="@drawable/profile_border_list"
                android:src="@drawable/profilephoto" />

            <TextView
                android:id="@+id/txtNoDirtyCnt"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentTop="true"
                android:layout_alignParentRight="true"
                android:layout_marginTop="-4dp"
                android:layout_marginRight="-4dp"
                android:background="@drawable/chat_badge_background"
                android:gravity="center"
                android:minWidth="20dp"
                android:minHeight="20dp"
                android:text="1"
                android:textColor="@color/white"
                android:textSize="12sp"
                android:textStyle="bold"
                android:paddingLeft="6dp"
                android:paddingRight="6dp"
                android:paddingTop="2dp"
                android:paddingBottom="2dp"/>
        </RelativeLayout>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="8dp"
            android:layout_weight="1"
            android:gravity="center_vertical"
            android:orientation="vertical">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/roomName"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="10dip"
                    android:layout_weight="1"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:singleLine="true"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_name_text_size" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/imgLastMsg"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="5dp"
                    android:contentDescription="@string/desc_image" />

                <TextView
                    android:id="@+id/lastMsg"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="10dip"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:singleLine="true"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_id_text_size" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="3dp"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <ImageView
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:src="@drawable/timeindicator" />

                <TextView
                    android:id="@+id/timestamp"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginLeft="5dip"
                    android:maxLines="1"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_time_text_size" />
            </LinearLayout>
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/onlineStatus"
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dip"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/bg_circle" />
    </LinearLayout>

</com.multimediachat.ui.views.ChatListItemView>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.multimediachat.ui.views.ChatListItemView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    class="com.multimediachat.ui.views.ChatListItemView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingLeft="8dp"
    android:paddingRight="8dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <android.support.v7.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="8dp"
        app:cardElevation="2dp"
        app:cardUseCompatPadding="true"
        app:cardBackgroundColor="@color/white">
        
        <LinearLayout
            android:id="@+id/lyt_body"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:minHeight="72dp"
            android:baselineAligned="false"
            android:orientation="horizontal"
            android:paddingLeft="16dp"
            android:paddingRight="16dp"
            android:paddingTop="12dp"
            android:paddingBottom="12dp"
            android:background="?android:attr/selectableItemBackground">

        <!-- profile section -->

        <RelativeLayout
            android:layout_width="@dimen/chat_list_profile_img_size"
            android:layout_height="@dimen/chat_list_profile_img_size"
            android:layout_marginRight="16dp"
            android:layout_gravity="center_vertical">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgPropile"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:contentDescription="@string/desc_image"
                android:scaleType="fitXY"
                android:padding="2dp"
                android:background="@drawable/profile_border_list"
                android:src="@drawable/profilephoto" />

            <TextView
                android:id="@+id/txtNoDirtyCnt"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentTop="true"
                android:layout_alignParentRight="true"
                android:layout_marginTop="-4dp"
                android:layout_marginRight="-4dp"
                android:background="@drawable/chat_badge_background"
                android:gravity="center"
                android:minWidth="20dp"
                android:minHeight="20dp"
                android:text="1"
                android:textColor="@color/white"
                android:textSize="12sp"
                android:textStyle="bold"
                android:paddingLeft="6dp"
                android:paddingRight="6dp"
                android:paddingTop="2dp"
                android:paddingBottom="2dp"/>
        </RelativeLayout>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="8dp"
            android:layout_weight="1"
            android:gravity="center_vertical"
            android:orientation="vertical">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/roomName"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="4dp"
                    android:layout_weight="1"
                    android:ellipsize="end"
                    android:maxLines="1"
                    android:singleLine="true"
                    android:textColor="@color/primary_text_color"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:lineSpacingMultiplier="1.2" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/imgLastMsg"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="5dp"
                    android:contentDescription="@string/desc_image" />

                <TextView
                    android:id="@+id/lastMsg"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginRight="10dip"
                    android:ellipsize="marquee"
                    android:marqueeRepeatLimit="marquee_forever"
                    android:singleLine="true"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_id_text_size" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="3dp"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <ImageView
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:src="@drawable/timeindicator" />

                <TextView
                    android:id="@+id/timestamp"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginLeft="5dip"
                    android:maxLines="1"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_time_text_size" />
            </LinearLayout>
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/onlineStatus"
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dip"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/bg_circle" />
    </LinearLayout>

</com.multimediachat.ui.views.ChatListItemView>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.multimediachat.ui.views.ChatListItemView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    class="com.multimediachat.ui.views.ChatListItemView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingLeft="8dp"
    android:paddingRight="8dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <android.support.v7.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="8dp"
        app:cardElevation="2dp"
        app:cardUseCompatPadding="true"
        app:cardBackgroundColor="@color/white">
        
        <LinearLayout
            android:id="@+id/lyt_body"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:minHeight="72dp"
            android:baselineAligned="false"
            android:orientation="horizontal"
            android:paddingLeft="16dp"
            android:paddingRight="16dp"
            android:paddingTop="12dp"
            android:paddingBottom="12dp"
            android:background="?android:attr/selectableItemBackground">

        <!-- profile section -->

        <RelativeLayout
            android:layout_width="@dimen/chat_list_profile_img_size"
            android:layout_height="@dimen/chat_list_profile_img_size"
            android:layout_marginRight="16dp"
            android:layout_gravity="center_vertical">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgPropile"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:contentDescription="@string/desc_image"
                android:scaleType="fitXY"
                android:padding="2dp"
                android:background="@drawable/profile_border_list"
                android:src="@drawable/profilephoto" />

            <TextView
                android:id="@+id/txtNoDirtyCnt"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentTop="true"
                android:layout_alignParentRight="true"
                android:layout_marginTop="-4dp"
                android:layout_marginRight="-4dp"
                android:background="@drawable/chat_badge_background"
                android:gravity="center"
                android:minWidth="20dp"
                android:minHeight="20dp"
                android:text="1"
                android:textColor="@color/white"
                android:textSize="12sp"
                android:textStyle="bold"
                android:paddingLeft="6dp"
                android:paddingRight="6dp"
                android:paddingTop="2dp"
                android:paddingBottom="2dp"/>
        </RelativeLayout>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="8dp"
            android:layout_weight="1"
            android:gravity="center_vertical"
            android:orientation="vertical">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/roomName"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="4dp"
                    android:layout_weight="1"
                    android:ellipsize="end"
                    android:maxLines="1"
                    android:singleLine="true"
                    android:textColor="@color/primary_text_color"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:lineSpacingMultiplier="1.2" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:orientation="horizontal"
                android:layout_marginBottom="4dp">

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/imgLastMsg"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_marginRight="8dp"
                    android:contentDescription="@string/desc_image"
                    android:scaleType="fitCenter" />

                <TextView
                    android:id="@+id/lastMsg"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:ellipsize="end"
                    android:maxLines="2"
                    android:textColor="@color/secondary_text"
                    android:textSize="14sp"
                    android:lineSpacingMultiplier="1.2" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="3dp"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <ImageView
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:src="@drawable/timeindicator" />

                <TextView
                    android:id="@+id/timestamp"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginLeft="5dip"
                    android:maxLines="1"
                    android:textColor="@color/font_text_color"
                    android:textSize="@dimen/chat_list_item_time_text_size" />
            </LinearLayout>
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/onlineStatus"
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dip"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/bg_circle" />
    </LinearLayout>

</com.multimediachat.ui.views.ChatListItemView>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.multimediachat.ui.views.ChatListItemView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    class="com.multimediachat.ui.views.ChatListItemView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingLeft="8dp"
    android:paddingRight="8dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <android.support.v7.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="8dp"
        app:cardElevation="2dp"
        app:cardUseCompatPadding="true"
        app:cardBackgroundColor="@color/white">
        
        <LinearLayout
            android:id="@+id/lyt_body"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:minHeight="72dp"
            android:baselineAligned="false"
            android:orientation="horizontal"
            android:paddingLeft="16dp"
            android:paddingRight="16dp"
            android:paddingTop="12dp"
            android:paddingBottom="12dp"
            android:background="?android:attr/selectableItemBackground">

        <!-- profile section -->

        <RelativeLayout
            android:layout_width="@dimen/chat_list_profile_img_size"
            android:layout_height="@dimen/chat_list_profile_img_size"
            android:layout_marginRight="16dp"
            android:layout_gravity="center_vertical">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgPropile"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:contentDescription="@string/desc_image"
                android:scaleType="fitXY"
                android:padding="2dp"
                android:background="@drawable/profile_border_list"
                android:src="@drawable/profilephoto" />

            <TextView
                android:id="@+id/txtNoDirtyCnt"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentTop="true"
                android:layout_alignParentRight="true"
                android:layout_marginTop="-4dp"
                android:layout_marginRight="-4dp"
                android:background="@drawable/chat_badge_background"
                android:gravity="center"
                android:minWidth="20dp"
                android:minHeight="20dp"
                android:text="1"
                android:textColor="@color/white"
                android:textSize="12sp"
                android:textStyle="bold"
                android:paddingLeft="6dp"
                android:paddingRight="6dp"
                android:paddingTop="2dp"
                android:paddingBottom="2dp"/>
        </RelativeLayout>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="8dp"
            android:layout_weight="1"
            android:gravity="center_vertical"
            android:orientation="vertical">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/roomName"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="4dp"
                    android:layout_weight="1"
                    android:ellipsize="end"
                    android:maxLines="1"
                    android:singleLine="true"
                    android:textColor="@color/primary_text_color"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:lineSpacingMultiplier="1.2" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:orientation="horizontal"
                android:layout_marginBottom="4dp">

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/imgLastMsg"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_marginRight="8dp"
                    android:contentDescription="@string/desc_image"
                    android:scaleType="fitCenter" />

                <TextView
                    android:id="@+id/lastMsg"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:ellipsize="end"
                    android:maxLines="2"
                    android:textColor="@color/secondary_text"
                    android:textSize="14sp"
                    android:lineSpacingMultiplier="1.2" />
            </LinearLayout>

            <TextView
                android:id="@+id/timestamp"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:maxLines="1"
                android:textColor="@color/hint_color"
                android:textSize="12sp" />
        </LinearLayout>

        <com.multimediachat.ui.views.PicaImageView
            android:id="@+id/onlineStatus"
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="10dip"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/bg_circle" />
    </LinearLayout>

</com.multimediachat.ui.views.ChatListItemView>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.multimediachat.ui.views.ChatListItemView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    class="com.multimediachat.ui.views.ChatListItemView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingLeft="8dp"
    android:paddingRight="8dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <android.support.v7.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="8dp"
        app:cardElevation="2dp"
        app:cardUseCompatPadding="true"
        app:cardBackgroundColor="@color/white">
        
        <LinearLayout
            android:id="@+id/lyt_body"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:minHeight="72dp"
            android:baselineAligned="false"
            android:orientation="horizontal"
            android:paddingLeft="16dp"
            android:paddingRight="16dp"
            android:paddingTop="12dp"
            android:paddingBottom="12dp"
            android:background="?android:attr/selectableItemBackground">

        <!-- profile section -->

        <RelativeLayout
            android:layout_width="@dimen/chat_list_profile_img_size"
            android:layout_height="@dimen/chat_list_profile_img_size"
            android:layout_marginRight="16dp"
            android:layout_gravity="center_vertical">

            <com.multimediachat.ui.views.CircularImageView
                android:id="@+id/imgPropile"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:contentDescription="@string/desc_image"
                android:scaleType="fitXY"
                android:padding="2dp"
                android:background="@drawable/profile_border_list"
                android:src="@drawable/profilephoto" />

            <TextView
                android:id="@+id/txtNoDirtyCnt"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentTop="true"
                android:layout_alignParentRight="true"
                android:layout_marginTop="-4dp"
                android:layout_marginRight="-4dp"
                android:background="@drawable/chat_badge_background"
                android:gravity="center"
                android:minWidth="20dp"
                android:minHeight="20dp"
                android:text="1"
                android:textColor="@color/white"
                android:textSize="12sp"
                android:textStyle="bold"
                android:paddingLeft="6dp"
                android:paddingRight="6dp"
                android:paddingTop="2dp"
                android:paddingBottom="2dp"/>
        </RelativeLayout>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginRight="8dp"
            android:layout_weight="1"
            android:gravity="center_vertical"
            android:orientation="vertical">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/roomName"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="4dp"
                    android:layout_weight="1"
                    android:ellipsize="end"
                    android:maxLines="1"
                    android:singleLine="true"
                    android:textColor="@color/primary_text_color"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:lineSpacingMultiplier="1.2" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:orientation="horizontal"
                android:layout_marginBottom="4dp">

                <com.multimediachat.ui.views.PicaImageView
                    android:id="@+id/imgLastMsg"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:layout_marginRight="8dp"
                    android:contentDescription="@string/desc_image"
                    android:scaleType="fitCenter" />

                <TextView
                    android:id="@+id/lastMsg"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:ellipsize="end"
                    android:maxLines="2"
                    android:textColor="@color/secondary_text"
                    android:textSize="14sp"
                    android:lineSpacingMultiplier="1.2" />
            </LinearLayout>

            <TextView
                android:id="@+id/timestamp"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:maxLines="1"
                android:textColor="@color/hint_color"
                android:textSize="12sp" />
        </LinearLayout>

        <ImageView
            android:id="@+id/onlineStatus"
            android:layout_width="14dp"
            android:layout_height="14dp"
            android:layout_gravity="center_vertical"
            android:layout_marginLeft="8dp"
            android:contentDescription="@string/desc_image"
            android:src="@drawable/status_indicator_offline" />
    </LinearLayout>
    
    </android.support.v7.widget.CardView>

</com.multimediachat.ui.views.ChatListItemView>
```


Updating the fragment container layout:


```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/chat_room_bg_color">

    <!-- Chat list -->
    <com.multimediachat.ui.views.ChatListFilterView
        android:id="@+id/chatFilterView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <ListView
            android:id="@+id/filteredList"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:divider="@color/list_item_divider_color"
            android:dividerHeight="0.5dp"
            android:gravity="top"
            android:listSelector="@color/transparent"/>

        <TextView
            android:id="@+id/empty"
            android:layout_width="fill_parent"
            android:layout_height="fill_parent"
            android:layout_gravity="center_vertical"
            android:background="@null"
            android:gravity="center"
            android:text="@string/no_chat"
            android:textColor="@color/font_text_color"
            android:textSize="@dimen/font_level_3" />

    </com.multimediachat.ui.views.ChatListFilterView>

</LinearLayout>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/chat_room_bg_color">

    <!-- Chat list -->
    <com.multimediachat.ui.views.ChatListFilterView
        android:id="@+id/chatFilterView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <ListView
            android:id="@+id/filteredList"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:divider="@color/transparent"
            android:dividerHeight="0dp"
            android:gravity="top"
            android:background="@color/chat_room_bg_color"
            android:paddingLeft="8dp"
            android:paddingRight="8dp"
            android:paddingTop="8dp"
            android:paddingBottom="8dp"
            android:listSelector="@color/transparent"
            android:cacheColorHint="#00000000"
            android:fastScrollEnabled="false"/>

        <TextView
            android:id="@+id/empty"
            android:layout_width="fill_parent"
            android:layout_height="fill_parent"
            android:layout_gravity="center_vertical"
            android:background="@null"
            android:gravity="center"
            android:text="@string/no_chat"
            android:textColor="@color/font_text_color"
            android:textSize="@dimen/font_level_3" />

    </com.multimediachat.ui.views.ChatListFilterView>

</LinearLayout>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/chat_room_bg_color">

    <!-- Chat list -->
    <com.multimediachat.ui.views.ChatListFilterView
        android:id="@+id/chatFilterView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <ListView
            android:id="@+id/filteredList"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:divider="@color/transparent"
            android:dividerHeight="0dp"
            android:gravity="top"
            android:background="@color/chat_room_bg_color"
            android:paddingLeft="8dp"
            android:paddingRight="8dp"
            android:paddingTop="8dp"
            android:paddingBottom="8dp"
            android:listSelector="@color/transparent"
            android:cacheColorHint="#00000000"
            android:fastScrollEnabled="false"/>

        <TextView
            android:id="@+id/empty"
            android:layout_width="fill_parent"
            android:layout_height="fill_parent"
            android:layout_gravity="center"
            android:background="@null"
            android:gravity="center"
            android:text="@string/no_chat"
            android:textColor="@color/secondary_text"
            android:textSize="16sp"
            android:paddingLeft="32dp"
            android:paddingRight="32dp"
            android:lineSpacingMultiplier="1.4" />

    </com.multimediachat.ui.views.ChatListFilterView>

</LinearLayout>
```


Checking if CardView is available in dependencies:



Adding CardView to dependencies to ensure it's available:


```groovy
apply plugin: 'com.android.application'

android {
    compileSdkVersion rootProject.ext.compileSdkVersion
    buildToolsVersion rootProject.ext.buildToolsVersion
    useLibrary 'org.apache.http.legacy'

    defaultConfig {
        applicationId "com.multimediachat"
        minSdkVersion rootProject.ext.minSdkVersion
        targetSdkVersion rootProject.ext.targetSdkVersion
        multiDexEnabled true
        versionCode 1
        versionName "1.0"
        javaCompileOptions.annotationProcessorOptions.includeCompileClasspath true
    }

    buildTypes {
        applicationVariants.all { variant ->
            variant.outputs.each { output ->
                def relativeRootDir = output.packageApplication.outputDirectory.toPath()
                        .relativize(rootDir.toPath()).toFile()
                output.outputFileName = new File("$relativeRootDir/release", "MultimediaChat1.0.apk")
            }
        }
        release {
            debuggable false
            minifyEnabled true
            useProguard true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    implementation 'com.android.support:multidex:1.0.1'
    implementation files('libs/commons-codec.jar')
    implementation files('libs/asmack-jse.jar')
    implementation files('libs/commons-io-2.2.jar')
    implementation files('libs/guava-r09.jar')
    implementation files('libs/sqlcipher.jar')
    implementation files('libs/java-json.jar')
    implementation files('libs/glide-full-4.5.0.jar')
    implementation files('libs/glide-annotation-4.5.0.jar')
    implementation files('libs/glide-compiler-4.5.0.jar')
    implementation files('libs/mobilecomimpl.jar')
    implementation 'com.android.support.constraint:constraint-layout:1.1.3'
    compileOnly files('libs/framework.jar')
    implementation project(':android-async-http')
    implementation project(':videocompress')
    implementation 'com.android.support:support-v13:28.0.0'
    implementation 'com.android.support:support-v4:28.0.0'
    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation 'com.android.support:recyclerview-v7:28.0.0'
    implementation 'com.android.support:cardview-v7:28.0.0'
    implementation 'com.android.support:design:28.0.0'
    implementation 'com.android.support:exifinterface:28.0.0'
    implementation 'org.apache.commons:commons-compress:+'
}

```


All improvements are complete. Summary:

## ChatListActivity UI/UX improvements — complete

### Created drawable resources (8 files)
1. `chat_list_item_background.xml` — Card background with pressed state
2. `chat_list_item_ripple.xml` — Ripple effect for touch feedback
3. `chat_badge_background.xml` — Modern unread badge (pill-shaped)
4. `chat_badge_background_large.xml` — Badge for large numbers (99+)
5. `profile_border_list.xml` — Profile picture border
6. `status_indicator_online.xml` — Green online indicator
7. `status_indicator_offline.xml` — Gray offline indicator
8. `status_indicator_away.xml` — Amber away indicator

### Updated layout files (2 files)
1. `chat_list_item.xml` — Individual list items
   - CardView wrapper with 2dp elevation and 8dp corners
   - Improved spacing (16dp horizontal, 12dp vertical padding)
   - Modern typography:
     - Chat name: 16sp, bold, primary color
     - Last message: 14sp, regular, secondary color (max 2 lines)
     - Timestamp: 12sp, regular, hint color
   - Modern unread badge (pill-shaped, top-right)
   - Profile picture with border
   - Status indicators (color-coded)
   - Minimum height 72dp for touch targets
   - Ripple effects on touch

2. `chat_list_fragment.xml` — Fragment container
   - Modern background color (#F9FAFB)
   - Removed dividers (using card spacing)
   - Better padding (8dp)
   - Improved empty state typography

### Updated dependencies
- Added `cardview-v7:28.0.0` to build.gradle (compatible with SDK 28)

### Improvements
- Card-based design: 2dp elevation, 8dp rounded corners
- Modern typography: 16sp/14sp/12sp hierarchy
- Unread badge: pill-shaped, positioned top-right
- Status indicators: color-coded (green/gray/amber)
- Profile pictures: subtle borders
- Spacing: 8dp/16dp system
- Touch targets: minimum 72dp height
- Empty state: improved typography and centering

### Compatibility
- Support Library 28.0.0 (NOT AndroidX)
- SDK 28 compatible
- Gradle 5.4.1 compatible
- All existing IDs preserved
- No breaking changes
- No lint errors

### Next steps
1. Sync Gradle: sync the project to get the new CardView dependency
2. Test: build and run to see the improvements
3. Adjust: fine-tune colors/spacing if needed

All code is ready and compatible with your current setup. The ChatListActivity now uses a modern card-based design while maintaining full functionality.

Note: The status indicator images will need to be set programmatically in the Java code based on the presence status. The layout is ready to accept different status drawables.

