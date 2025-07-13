/**
 * Cấu hình ứng dụng
 */

/**
 * Cấu hình môi trường
 */
export const ENV = {
  // Môi trường thực thi (development, production, test)
  NODE_ENV: process.env.NODE_ENV || "development",

  // Cổng máy chủ (nếu cần)
  PORT: process.env.PORT || 3000,

  // Cờ gỡ lỗi
  DEBUG: process.env.DEBUG === "true",
};

/**
 * Cấu hình ứng dụng
 */
export const APP_CONFIG = {
  // Tên ứng dụng
  NAME: "Android Accessibility Service MCP",

  // Phiên bản
  VERSION: "1.0.0",

  // Thời gian chờ mặc định (ms)
  DEFAULT_TIMEOUT: 10000,

  // Số lần thử lại tối đa
  MAX_RETRIES: 3,
};

/**
 * Cấu hình API Accessibility Service
 */
export const API_CONFIG = {
  // Host và Port từ environment variables
  HOST: process.env.ACCESSIBILITY_API_HOST || "192.168.1.5",
  PORT: process.env.ACCESSIBILITY_API_PORT || "8080",

  // Base URL được tạo từ host và port
  get BASE_URL() {
    return `http://${this.HOST}:${this.PORT}`;
  },

  // Timeout cho các request API
  TIMEOUT: parseInt(process.env.ACCESSIBILITY_API_TIMEOUT || "15000"),

  // Headers mặc định
  DEFAULT_HEADERS: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
};

/**
 * Cấu hình ghi log
 */
export const LOG_CONFIG = {
  // Cấp độ ghi log (debug, info, warn, error)
  LEVEL: process.env.LOG_LEVEL || "info",

  // Có ghi log vào tệp không
  FILE_LOGGING: process.env.FILE_LOGGING === "true",

  // Đường dẫn tệp ghi log
  LOG_FILE_PATH: process.env.LOG_FILE_PATH || "./logs/app.log",
};
