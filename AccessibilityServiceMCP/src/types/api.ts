/**
 * Types cho Android Accessibility Service API
 */

// Base response structure
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: string;
  timestamp: number;
}

// Health check response
export interface HealthResponse {
  status: string;
  uptime: number;
  timestamp: number;
}

// UI Element bounds
export interface ElementBounds {
  left: number;
  top: number;
  right: number;
  bottom: number;
  width: number;
  height: number;
  centerX: number;
  centerY: number;
}

// UI Element
export interface UiElement {
  id?: string;
  text?: string;
  contentDescription?: string;
  className?: string;
  bounds: ElementBounds;
  isClickable: boolean;
  isEditable: boolean;
  isScrollable?: boolean;
  isCheckable?: boolean;
  isEnabled?: boolean;
  isChecked?: boolean;
  isSelected?: boolean;
  isFocused?: boolean;
}

// Find elements request
export interface FindElementsRequest {
  text?: string;
  className?: string;
  packageName?: string;
  contentDescription?: string;
  resourceId?: string;
  clickable?: boolean;
  scrollable?: boolean;
  actionType?: "click" | "input" | "scroll" | "check" | "read";
}

// Find elements response
export interface FindElementsResponse {
  elements: UiElement[];
  count: number;
  fallback?: boolean;
  fallbackType?: string;
  message?: string;
  clickableElements?: UiElement[];
  editableElements?: UiElement[];
  scrollableElements?: UiElement[];
  checkableElements?: UiElement[];
  switchElements?: UiElement[];
  textElements?: UiElement[];
  totalClickable?: number;
  totalEditable?: number;
  totalScrollable?: number;
  totalCheckable?: number;
  totalSwitch?: number;
  totalText?: number;
  captureTime: number;
}

// Click request
export interface ClickRequest {
  x: number;
  y: number;
}

// Input text request
export interface InputTextRequest {
  text: string;
  clearFirst?: boolean;
}

export interface KeyboardActionRequest {
  action:
    | "ENTER"
    | "BACK"
    | "HOME"
    | "RECENT"
    | "SEARCH"
    | "SEND"
    | "GO"
    | "DONE";
}

// Scroll request
export interface ScrollRequest {
  direction: "UP" | "DOWN" | "LEFT" | "RIGHT";
  distance?: number;
}

// Swipe request
export interface SwipeRequest {
  startX: number;
  startY: number;
  endX: number;
  endY: number;
  duration?: number;
}

// App request
export interface AppRequest {
  appName?: string;
  packageName?: string;
}

// Volume request
export interface VolumeRequest {
  direction: "UP" | "DOWN";
  stream?: "MUSIC" | "RING" | "NOTIFICATION" | "ALARM";
}

// Device info response
export interface DeviceInfoResponse {
  deviceModel: string;
  androidVersion: string;
  screenWidth: number;
  screenHeight: number;
  density: number;
  orientation: string;
}

// Recent apps response
export interface RecentAppsResponse {
  apps: Array<{
    packageName: string;
    appName: string;
    position: number;
  }>;
  totalApps: number;
}
