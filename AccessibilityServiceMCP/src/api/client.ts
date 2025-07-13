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
  async health(): Promise<HealthResponse> {
    const response: AxiosResponse<ApiResponse<HealthResponse>> = await this.client.get("/health");
    if (!response.data.success) {
      throw new Error(response.data.error || "Health check failed");
    }
    return response.data.data!;
  }

  // UI Operations
  async getUiTree(): Promise<any> {
    const response: AxiosResponse<ApiResponse> = await this.client.get("/ui-tree");
    if (!response.data.success) {
      throw new Error(response.data.error || "Failed to get UI tree");
    }
    return response.data.data;
  }

  async findElements(request: FindElementsRequest): Promise<FindElementsResponse> {
    const response: AxiosResponse<ApiResponse<FindElementsResponse>> = await this.client.post(
      "/find-elements",
      request
    );
    if (!response.data.success) {
      throw new Error(response.data.error || "Failed to find elements");
    }
    return response.data.data!;
  }

  // Interaction Operations
  async click(request: ClickRequest): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/click", request);
    if (!response.data.success) {
      throw new Error(response.data.error || "Click failed");
    }
  }

  async longClick(request: ClickRequest): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/long-click", request);
    if (!response.data.success) {
      throw new Error(response.data.error || "Long click failed");
    }
  }

  async doubleClick(request: ClickRequest): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/double-click", request);
    if (!response.data.success) {
      throw new Error(response.data.error || "Double click failed");
    }
  }

  async inputText(request: InputTextRequest): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/input-text", request);
    if (!response.data.success) {
      throw new Error(response.data.error || "Input text failed");
    }
  }

  async scroll(request: ScrollRequest): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/scroll", request);
    if (!response.data.success) {
      throw new Error(response.data.error || "Scroll failed");
    }
  }

  async swipe(request: SwipeRequest): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/swipe", request);
    if (!response.data.success) {
      throw new Error(response.data.error || "Swipe failed");
    }
  }

  // Navigation Operations
  async home(): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/home");
    if (!response.data.success) {
      throw new Error(response.data.error || "Home navigation failed");
    }
  }

  async back(): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/back");
    if (!response.data.success) {
      throw new Error(response.data.error || "Back navigation failed");
    }
  }

  async recent(): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/recent");
    if (!response.data.success) {
      throw new Error(response.data.error || "Recent apps navigation failed");
    }
  }

  // App Management Operations
  async clickApp(request: AppRequest): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/click-app", request);
    if (!response.data.success) {
      throw new Error(response.data.error || "Click app failed");
    }
  }

  async launchApp(request: AppRequest): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/launch-app", request);
    if (!response.data.success) {
      throw new Error(response.data.error || "Launch app failed");
    }
  }

  async closeApp(request: AppRequest): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/close-app", request);
    if (!response.data.success) {
      throw new Error(response.data.error || "Close app failed");
    }
  }

  async getRecentApps(): Promise<RecentAppsResponse> {
    const response: AxiosResponse<ApiResponse<RecentAppsResponse>> = await this.client.get("/recent-apps");
    if (!response.data.success) {
      throw new Error(response.data.error || "Failed to get recent apps");
    }
    return response.data.data!;
  }

  // System Operations
  async getDeviceInfo(): Promise<DeviceInfoResponse> {
    const response: AxiosResponse<ApiResponse<DeviceInfoResponse>> = await this.client.get("/device-info");
    if (!response.data.success) {
      throw new Error(response.data.error || "Failed to get device info");
    }
    return response.data.data!;
  }

  async getScreenshot(): Promise<string> {
    const response: AxiosResponse<ApiResponse<{ base64Image: string }>> = await this.client.get("/screenshot");
    if (!response.data.success) {
      throw new Error(response.data.error || "Failed to get screenshot");
    }
    return response.data.data!.base64Image;
  }

  async setVolume(request: VolumeRequest): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/volume", request);
    if (!response.data.success) {
      throw new Error(response.data.error || "Volume control failed");
    }
  }

  async openNotifications(): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/open-notifications");
    if (!response.data.success) {
      throw new Error(response.data.error || "Open notifications failed");
    }
  }

  async openQuickSettings(): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await this.client.post("/open-quick-settings");
    if (!response.data.success) {
      throw new Error(response.data.error || "Open quick settings failed");
    }
  }
}

// Singleton instance
export const apiClient = new AccessibilityApiClient();
