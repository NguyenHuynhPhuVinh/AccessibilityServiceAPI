/**
 * Android Accessibility Service API Client
 */
import axios, { AxiosInstance, AxiosResponse } from "axios";
import { API_CONFIG } from "../config/config.js";
import type {
  ApiResponse,
  HealthResponse,
  FindElementsRequest,
  FindElementsResponse,
  ClickRequest,
  InputTextRequest,
  ScrollRequest,
  SwipeRequest,
  AppRequest,
  VolumeRequest,
  DeviceInfoResponse,
  RecentAppsResponse,
  KeyboardActionRequest,
} from "../types/api.js";

export class AccessibilityApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_CONFIG.BASE_URL,
      timeout: API_CONFIG.TIMEOUT,
      headers: API_CONFIG.DEFAULT_HEADERS,
    });

    // Request interceptor để log requests
    this.client.interceptors.request.use((config) => {
      console.error(`[API] ${config.method?.toUpperCase()} ${config.url}`);
      return config;
    });

    // Response interceptor để handle errors
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        console.error(`[API Error] ${error.message}`);
        if (error.code === "ECONNREFUSED") {
          throw new Error(
            `Không thể kết nối đến API server tại ${API_CONFIG.BASE_URL}. Vui lòng kiểm tra:\n` +
              `1. Thiết bị Android đã bật API server\n` +
              `2. IP address trong .env file đúng (hiện tại: ${API_CONFIG.HOST}:${API_CONFIG.PORT})\n` +
              `3. Thiết bị và máy tính trong cùng mạng`
          );
        }
        throw error;
      }
    );
  }

  // Health check
  async health(): Promise<any> {
    const response: AxiosResponse<ApiResponse<HealthResponse>> =
      await this.client.get("/health");
    if (!response.data.success) {
      throw new Error(response.data.error || "Health check failed");
    }
    return response.data;
  }

  // UI Operations
  async getUiTree(): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.get(
      "/ui-tree"
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Failed to get UI tree");
    }
    return response.data;
  }

  async findElements(request: FindElementsRequest): Promise<any> {
    const response: AxiosResponse<ApiResponse<FindElementsResponse>> =
      await this.client.post("/find-elements", request);
    if (!response.data.success) {
      throw new Error(response.data.error || "Failed to find elements");
    }
    return response.data;
  }

  // Interaction Operations
  async click(request: ClickRequest): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/click",
      request
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Click failed");
    }
    return response.data;
  }

  async longClick(request: ClickRequest): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/long-click",
      request
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Long click failed");
    }
    return response.data;
  }

  async doubleClick(request: ClickRequest): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/double-click",
      request
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Double click failed");
    }
    return response.data;
  }

  async inputText(request: InputTextRequest): Promise<any> {
    // Ensure UTF-8 encoding for Vietnamese text
    const encodedRequest = {
      ...request,
      text: Buffer.from(request.text, "utf8").toString("utf8"),
    };

    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/input-text",
      encodedRequest
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Input text failed");
    }
    return response.data;
  }

  async keyboardAction(request: KeyboardActionRequest): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/keyboard-action",
      request
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Keyboard action failed");
    }
    return response.data;
  }

  async scroll(request: ScrollRequest): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/scroll",
      request
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Scroll failed");
    }
    return response.data;
  }

  async swipe(request: SwipeRequest): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/swipe",
      request
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Swipe failed");
    }
    return response.data;
  }

  // Navigation Operations
  async home(): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/home"
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Home navigation failed");
    }
    return response.data;
  }

  async back(): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/back"
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Back navigation failed");
    }
    return response.data;
  }

  async recent(): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/recent"
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Recent apps navigation failed");
    }
    return response.data;
  }

  // App Management Operations
  async clickApp(request: AppRequest): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/click-app",
      request
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Click app failed");
    }
    return response.data;
  }

  async launchApp(request: AppRequest): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/launch-app",
      request
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Launch app failed");
    }
    return response.data;
  }

  async closeApp(request: AppRequest): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/close-app",
      request
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Close app failed");
    }
    return response.data;
  }

  async getRecentApps(): Promise<any> {
    const response: AxiosResponse<ApiResponse<RecentAppsResponse>> =
      await this.client.get("/recent-apps");
    if (!response.data.success) {
      throw new Error(response.data.error || "Failed to get recent apps");
    }
    return response.data;
  }

  // System Operations
  async getDeviceInfo(): Promise<any> {
    const response: AxiosResponse<ApiResponse<DeviceInfoResponse>> =
      await this.client.get("/device-info");
    if (!response.data.success) {
      throw new Error(response.data.error || "Failed to get device info");
    }
    return response.data;
  }

  async getScreenshot(): Promise<any> {
    const response: AxiosResponse<ApiResponse<{ base64Image: string }>> =
      await this.client.get("/screenshot");
    if (!response.data.success) {
      throw new Error(response.data.error || "Failed to get screenshot");
    }
    return response.data;
  }

  async setVolume(request: VolumeRequest): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/volume",
      request
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Volume control failed");
    }
    return response.data;
  }

  async openNotifications(): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/open-notifications"
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Open notifications failed");
    }
    return response.data;
  }

  async openQuickSettings(): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.post(
      "/open-quick-settings"
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Open quick settings failed");
    }
    return response.data;
  }
}

// Singleton instance
export const apiClient = new AccessibilityApiClient();
